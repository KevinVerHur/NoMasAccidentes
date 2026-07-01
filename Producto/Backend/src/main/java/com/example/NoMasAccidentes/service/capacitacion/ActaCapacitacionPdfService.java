package com.example.NoMasAccidentes.service.capacitacion;

import com.example.NoMasAccidentes.model.asistencia.Asistencia;
import com.example.NoMasAccidentes.model.capacitacion.Capacitacion;
import com.example.NoMasAccidentes.repository.capacitacion.CapacitacionRepository;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActaCapacitacionPdfService {

    private final CapacitacionRepository capacitacionRepository;

    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter F_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font CABECERA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    public ActaCapacitacionPdfService(CapacitacionRepository capacitacionRepository) {
        this.capacitacionRepository = capacitacionRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generarActaPdf(Long idCapacitacion) {
        try {
            Capacitacion c = capacitacionRepository.findById(idCapacitacion)
                    .orElseThrow(() -> new RuntimeException("Capacitación no encontrada"));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, baos);

            document.open();

            Paragraph encabezado = new Paragraph("ACTA DE CAPACITACIÓN", TITULO);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            document.add(encabezado);

            document.add(espacio());

            document.add(seccion("Datos generales"));
            document.add(linea("N° Capacitación: ", String.valueOf(c.getId())));
            document.add(linea("Empresa: ", c.getEmpresa().getRazonSocial()));
            document.add(linea("RUT Empresa: ", c.getEmpresa().getRut()));
            document.add(linea("Fecha programada: ", c.getFechaProgramada() != null ? c.getFechaProgramada().format(F_FECHA) : "—"));
            document.add(linea("Fecha realización: ", c.getFechaRealizacion() != null ? c.getFechaRealizacion().format(F_FECHA) : "—"));
            document.add(linea("Hora: ", c.getHoraProgramada() != null ? c.getHoraProgramada().format(F_HORA) : "—"));
            document.add(linea("Lugar: ", c.getLugar()));
            document.add(linea("Relator: ", obtenerNombreRelator(c)));
            document.add(linea("Estado: ", c.getEstado() != null ? c.getEstado().name() : "—"));

            document.add(espacio());

            document.add(seccion("Información de la capacitación"));
            document.add(linea("Curso: ", c.getCurso()));
            document.add(linea("Objetivo: ", c.getObjetivo()));
            document.add(linea("Cupos: ", c.getCupos() != null ? c.getCupos().toString() : "—"));

            document.add(espacio());

            document.add(seccion("Lista de asistentes"));
            document.add(tablaAsistentes(c));

            document.add(espacio());

            document.add(seccion("Observaciones"));
            String observacion = c.getObservacionActa();

            if (observacion != null && !observacion.isBlank()) {
                document.add(new Paragraph(observacion, NORMAL));
            } else {
                document.add(new Paragraph("Sin observaciones registradas.", NORMAL));
            }

            document.add(espacio());
            document.add(espacio());

            Paragraph firmas = new Paragraph(
                    "__________________________          __________________________\n" +
                    "        Relator                    Representante Empresa",
                    NORMAL
            );
            firmas.setAlignment(Element.ALIGN_CENTER);
            document.add(firmas);

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el acta de capacitación en PDF", e);
        }
    }

    private PdfPTable tablaAsistentes(Capacitacion c) throws Exception {
        PdfPTable tabla = new PdfPTable(new float[]{1f, 5f, 2f});
        tabla.setWidthPercentage(100);

        tabla.addCell(celdaCabecera("N°"));
        tabla.addCell(celdaCabecera("Nombre"));
        tabla.addCell(celdaCabecera("Asistió"));

        if (c.getAsistencias() == null || c.getAsistencias().isEmpty()) {
            PdfPCell vacia = new PdfPCell(new Phrase("Sin asistentes inscritos.", NORMAL));
            vacia.setColspan(3);
            tabla.addCell(vacia);
            return tabla;
        }

        int i = 1;
        for (Asistencia a : c.getAsistencias()) {
            tabla.addCell(new Phrase(String.valueOf(i++), NORMAL));
            tabla.addCell(new Phrase(obtenerNombreAsistente(a), NORMAL));
            tabla.addCell(new Phrase(a.isAsistio() ? "Sí" : "No", NORMAL));
        }

        return tabla;
    }

    private String obtenerNombreRelator(Capacitacion c) {
        if (c.getRelator() == null || c.getRelator().getUsuario() == null) {
            return "—";
        }

        String nombre = c.getRelator().getUsuario().getNombre();
        String apellido = c.getRelator().getUsuario().getApellido();

        return ((nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "")).trim();
    }

    private String obtenerNombreAsistente(Asistencia a) {
        if (a.getAsistente() == null) {
            return "—";
        }

        String nombre = a.getAsistente().getNombre();
        String apellidos = a.getAsistente().getApellidos();

        return ((nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "")).trim();
    }

    private PdfPCell celdaCabecera(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, CABECERA));
        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
        return celda;
    }

    private Paragraph seccion(String texto) {
        Paragraph p = new Paragraph(texto, SUBTITULO);
        p.setSpacingBefore(6);
        p.setSpacingAfter(4);
        return p;
    }

    private Paragraph linea(String etiqueta, String valor) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(etiqueta, CABECERA));
        p.add(new Phrase(valor != null && !valor.isBlank() ? valor : "—", NORMAL));
        return p;
    }

    private Paragraph espacio() {
        return new Paragraph(" ", NORMAL);
    }
}