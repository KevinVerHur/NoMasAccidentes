package com.example.NoMasAccidentes.service.capacitacion;

import com.example.NoMasAccidentes.model.asistencia.Asistencia;
import com.example.NoMasAccidentes.model.capacitacion.Capacitacion;
import com.example.NoMasAccidentes.repository.capacitacion.CapacitacionRepository;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfWriter;
import com.example.NoMasAccidentes.service.pdf.PdfMarca;
import com.example.NoMasAccidentes.service.informe.AlmacenamientoInformeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertificadoCapacitacionPdfService {

    private final CapacitacionRepository capacitacionRepository;
    private final AlmacenamientoInformeService almacenamiento;

    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, PdfMarca.NAVY);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, PdfMarca.NAVY);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 12);

    public CertificadoCapacitacionPdfService(CapacitacionRepository capacitacionRepository,
                                             AlmacenamientoInformeService almacenamiento) {
        this.capacitacionRepository = capacitacionRepository;
        this.almacenamiento = almacenamiento;
    }

    @Transactional(readOnly = true)
    public byte[] generarCertificadosPdf(Long idCapacitacion) {
        try {
            Capacitacion c = capacitacionRepository.findById(idCapacitacion)
                    .orElseThrow(() -> new RuntimeException("Capacitación no encontrada"));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            PdfMarca.aplicar(document, writer);

            document.open();

            var presentes = c.getAsistencias().stream()
                    .filter(Asistencia::isAsistio)
                    .toList();

            if (presentes.isEmpty()) {
                document.add(new Paragraph("No hay asistentes presentes para certificar.", NORMAL));
            }

            for (int i = 0; i < presentes.size(); i++) {
                Asistencia a = presentes.get(i);

                Paragraph titulo = new Paragraph("CERTIFICADO DE CAPACITACIÓN", TITULO);
                titulo.setAlignment(Element.ALIGN_CENTER);
                document.add(titulo);

                document.add(new Paragraph(" "));

                Paragraph cuerpo = new Paragraph(
                        "No Más Accidentes certifica que:\n\n" +
                        obtenerNombreAsistente(a) + "\n\n" +
                        "participó y aprobó la capacitación:\n\n" +
                        c.getCurso() + "\n\n" +
                        "realizada para la empresa " + c.getEmpresa().getRazonSocial() +
                        ", con fecha " + (c.getFechaRealizacion() != null
                            ? c.getFechaRealizacion().format(F_FECHA)
                            : c.getFechaProgramada().format(F_FECHA)) +
                        ", en " + c.getLugar() + ".",
                        SUBTITULO
                );

                cuerpo.setAlignment(Element.ALIGN_CENTER);
                cuerpo.setLeading(22);
                document.add(cuerpo);

                document.add(new Paragraph(" "));

                Paragraph relator = new Paragraph("Relator: " + obtenerNombreRelator(c), NORMAL);
                relator.setAlignment(Element.ALIGN_CENTER);
                document.add(relator);

                document.add(new Paragraph("\n\n\n"));

                Paragraph firma = new Paragraph(
                        "______________________________\nFirma relator",
                        NORMAL
                );
                firma.setAlignment(Element.ALIGN_CENTER);
                document.add(firma);

                if (i < presentes.size() - 1) {
                    document.newPage();
                }
            }

            document.close();
            byte[] pdf = baos.toByteArray();
            almacenamiento.guardar("certificados", c.getEmpresa().getId(),
                    "certificados-capacitacion-" + idCapacitacion + ".pdf", pdf);
            return pdf;

        } catch (Exception e) {
            throw new RuntimeException("Error al generar certificados de capacitación", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generarCertificadoPdf(Long idCapacitacion, Long idAsistente) {
        try {
            Capacitacion c = capacitacionRepository.findById(idCapacitacion)
                .orElseThrow(() -> new RuntimeException("Capacitación no encontrada"));

            Asistencia asistencia = c.getAsistencias().stream()
                .filter(a -> a.getAsistente() != null)
                .filter(a -> a.getAsistente().getId().equals(idAsistente))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El asistente no pertenece a esta capacitación"));

        if (!asistencia.isAsistio()) {
            throw new RuntimeException("El asistente no registra asistencia, por lo tanto no puede certificarse.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        PdfMarca.aplicar(document, writer);

        document.open();

        Paragraph titulo = new Paragraph("CERTIFICADO DE CAPACITACIÓN", TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        document.add(new Paragraph(" "));

        Paragraph cuerpo = new Paragraph(
                "No Más Accidentes certifica que:\n\n" +
                obtenerNombreAsistente(asistencia) + "\n\n" +
                "participó satisfactoriamente en la capacitación:\n\n" +
                c.getCurso() + "\n\n" +
                "realizada para la empresa " + c.getEmpresa().getRazonSocial() +
                ", con fecha " + (c.getFechaRealizacion() != null
                        ? c.getFechaRealizacion().format(F_FECHA)
                        : c.getFechaProgramada().format(F_FECHA)) +
                ", en " + c.getLugar() + ".",
                SUBTITULO
        );

        cuerpo.setAlignment(Element.ALIGN_CENTER);
        cuerpo.setLeading(22);
        document.add(cuerpo);

        document.add(new Paragraph(" "));

        Paragraph relator = new Paragraph("Relator: " + obtenerNombreRelator(c), NORMAL);
        relator.setAlignment(Element.ALIGN_CENTER);
        document.add(relator);

        document.add(new Paragraph("\n\n\n"));

        Paragraph firma = new Paragraph(
                "______________________________\nFirma relator",
                NORMAL
        );
        firma.setAlignment(Element.ALIGN_CENTER);
        document.add(firma);

        document.close();

        byte[] pdf = baos.toByteArray();
        almacenamiento.guardar("certificados", c.getEmpresa().getId(),
                "certificado-capacitacion-" + idCapacitacion + "-asistente-" + idAsistente + ".pdf", pdf);
        return pdf;

    } catch (Exception e) {
        throw new RuntimeException("Error al generar certificado individual", e);
    }
}
    private String obtenerNombreAsistente(Asistencia a) {
        if (a.getAsistente() == null) return "—";
        return (a.getAsistente().getNombre() + " " + a.getAsistente().getApellidos()).trim();
    }

    private String obtenerNombreRelator(Capacitacion c) {
        if (c.getRelator() == null || c.getRelator().getUsuario() == null) return "—";
        return (c.getRelator().getUsuario().getNombre() + " " + c.getRelator().getUsuario().getApellido()).trim();
    }
}