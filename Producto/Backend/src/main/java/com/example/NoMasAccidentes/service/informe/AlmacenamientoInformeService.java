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
 * Almacena y recupera los PDFs generados por la aplicación. Guarda SIEMPRE una
 * copia en disco local bajo {@code informes.local.dir} (por defecto
 * {@code ../librerias}, es decir {@code Producto/librerias}) y, si además hay un
 * bucket S3 configurado ({@code aws.s3.bucket-name}), sube la misma clave a S3.
 * Cada documento se guarda dentro de una carpeta por tipo (p. ej. {@code informes/},
 * {@code reportes/}, {@code actas-capacitacion/}, {@code certificados/},
 * {@code comprobantes/}) y, dentro de ella, una subcarpeta por empresa
 * (p. ej. {@code informes/1/}) para agrupar los documentos de cada cliente. La clave
 * devuelta —y guardada en {@code Informe.urlPdf}— es la ruta completa
 * {@code carpeta/idEmpresa/archivo}, idéntica en disco y en S3.
 */
@Service
@Slf4j
public class AlmacenamientoInformeService {

    private static final String CARPETA_POR_DEFECTO = "informes";

    private final String bucket;
    private final String region;
    private final Path dirLocal;
    private S3Client s3;

    public AlmacenamientoInformeService(
            @Value("${aws.s3.bucket-name:}") String bucket,
            @Value("${aws.region:us-east-1}") String region,
            @Value("${informes.local.dir:../librerias}") String dirLocal) {
        this.bucket = bucket;
        this.region = region;
        this.dirLocal = Paths.get(dirLocal);
    }

    public boolean usaS3() {
        return bucket != null && !bucket.isBlank();
    }

    /**
     * Guarda el PDF en la carpeta indicada y devuelve la clave completa
     * ({@code carpeta/archivo}) para recuperarlo luego.
     */
    public String guardar(String carpeta, String nombreArchivo, byte[] pdf) {
        String clave = carpeta + "/" + nombreArchivo;
        // 1) Siempre deja una copia en disco local (misma estructura carpeta/idEmpresa/archivo).
        guardarEnDisco(clave, pdf);
        // 2) Además, si hay un bucket configurado, sube la MISMA clave a S3.
        if (usaS3()) {
            s3().putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(clave)
                            .contentType("application/pdf")
                            .build(),
                    RequestBody.fromBytes(pdf));
            log.info("PDF almacenado en S3 bucket={} key={}", bucket, clave);
        }
        return clave;
    }

    /** Escribe el PDF en disco bajo {@code dirLocal/clave}, creando las carpetas necesarias. */
    private void guardarEnDisco(String clave, byte[] pdf) {
        try {
            Path destino = dirLocal.resolve(clave);
            Files.createDirectories(destino.getParent());
            Files.write(destino, pdf);
            log.info("PDF guardado en disco local: {}", destino);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el PDF en disco", e);
        }
    }

    /** Atajo que guarda bajo la carpeta por defecto ({@code informes/}). */
    public String guardar(String nombreArchivo, byte[] pdf) {
        return guardar(CARPETA_POR_DEFECTO, nombreArchivo, pdf);
    }

    /**
     * Guarda el PDF agrupándolo por empresa: {@code carpeta/idEmpresa/archivo}
     * (p. ej. {@code comprobantes/1/comprobante-5.pdf}). Si {@code idEmpresa} es
     * nulo, cae en {@code carpeta/archivo}.
     */
    public String guardar(String carpeta, Long idEmpresa, String nombreArchivo, byte[] pdf) {
        String carpetaEmpresa = idEmpresa != null ? carpeta + "/" + idEmpresa : carpeta;
        return guardar(carpetaEmpresa, nombreArchivo, pdf);
    }

    /**
     * Recupera el PDF a partir de la clave completa guardada ({@code carpeta/archivo}).
     * Prefiere la copia en disco local; si no existe, la baja de S3 (si hay bucket).
     */
    public byte[] descargar(String clave) {
        Path archivo = dirLocal.resolve(clave);
        if (Files.exists(archivo)) {
            try {
                return Files.readAllBytes(archivo);
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer el informe del disco", e);
            }
        }
        if (usaS3()) {
            ResponseBytes<GetObjectResponse> bytes = s3().getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(clave).build());
            return bytes.asByteArray();
        }
        throw new RecursoNoEncontradoException("Archivo de informe no encontrado: " + clave);
    }

    /** Cliente S3 perezoso: solo se crea si hay bucket configurado. */
    private S3Client s3() {
        if (s3 == null) {
            s3 = S3Client.builder().region(Region.of(region)).build();
        }
        return s3;
    }
}
