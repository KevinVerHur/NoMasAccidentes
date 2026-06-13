package com.example.NoMasAccidentes.service.informe;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Almacena y recupera los PDFs de informes. Si hay un bucket S3 configurado
 * ({@code aws.s3.bucket-name}) usa S3; en caso contrario (dev) usa disco local
 * bajo {@code informes.local.dir}. La clave guardada en {@code Informe.urlPdf}
 * es el nombre de archivo, independiente del backend de almacenamiento.
 */
@Service
@Slf4j
public class AlmacenamientoInformeService {

    private static final String PREFIJO_S3 = "informes/";

    private final String bucket;
    private final String region;
    private final Path dirLocal;
    private S3Client s3;

    public AlmacenamientoInformeService(
            @Value("${aws.s3.bucket-name:}") String bucket,
            @Value("${aws.region:us-east-1}") String region,
            @Value("${informes.local.dir:./informes-pdf}") String dirLocal) {
        this.bucket = bucket;
        this.region = region;
        this.dirLocal = Paths.get(dirLocal);
    }

    public boolean usaS3() {
        return bucket != null && !bucket.isBlank();
    }

    /** Guarda el PDF y devuelve la clave para recuperarlo luego. */
    public String guardar(String nombreArchivo, byte[] pdf) {
        if (usaS3()) {
            s3().putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(PREFIJO_S3 + nombreArchivo)
                            .contentType("application/pdf")
                            .build(),
                    RequestBody.fromBytes(pdf));
            log.info("Informe almacenado en S3 bucket={} key={}", bucket, PREFIJO_S3 + nombreArchivo);
        } else {
            try {
                Files.createDirectories(dirLocal);
                Files.write(dirLocal.resolve(nombreArchivo), pdf);
                log.info("Informe almacenado en disco local: {}", dirLocal.resolve(nombreArchivo));
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo guardar el informe en disco", e);
            }
        }
        return nombreArchivo;
    }

    /** Recupera el PDF a partir de la clave guardada. */
    public byte[] descargar(String clave) {
        if (usaS3()) {
            ResponseBytes<GetObjectResponse> bytes = s3().getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(PREFIJO_S3 + clave).build());
            return bytes.asByteArray();
        }
        Path archivo = dirLocal.resolve(clave);
        if (!Files.exists(archivo)) {
            throw new RecursoNoEncontradoException("Archivo de informe no encontrado: " + clave);
        }
        try {
            return Files.readAllBytes(archivo);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el informe del disco", e);
        }
    }

    /** Cliente S3 perezoso: solo se crea si hay bucket configurado. */
    private S3Client s3() {
        if (s3 == null) {
            s3 = S3Client.builder().region(Region.of(region)).build();
        }
        return s3;
    }
}
