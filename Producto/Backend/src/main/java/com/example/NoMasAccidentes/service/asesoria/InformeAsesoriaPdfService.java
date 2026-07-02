package com.example.NoMasAccidentes.service.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Accidente;
import com.example.NoMasAccidentes.model.asesoria.Asesoria;
import com.example.NoMasAccidentes.model.asesoria.Fiscalizacion;
import com.example.NoMasAccidentes.model.asesoria.PropuestaMejora;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

/**
 * Genera el PDF del informe de una asesoría (RF15 para asesorías) con OpenPDF.
 * Incluye los datos de la asesoría, accidentes, fiscalizaciones y propuestas de
 * mejora (RF25).
 */
@Service
public class InformeAsesoriaPdfService {

    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final Font TITULO    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font NORMAL    = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font CABECERA  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    public byte[] generar(Asesoria asesoria,
                          List<Accidente> accidentes,
                          List<Fiscalizacion> fiscalizaciones,
                          List<PropuestaMejora> propuestas) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter.getInstance(doc, out);
        doc.open();

        doc.add(new Paragraph("Informe de Asesoría", TITULO));
        doc.add(new Paragraph("No Más Accidentes — Prevención de riesgos laborales", NORMAL));
        doc.add(espacio());

        doc.add(seccion("Datos de la asesoría"));
        doc.add(linea("Empresa: ", asesoria.getEmpresa().getRazonSocial()));
        doc.add(linea("RUT: ", asesoria.getEmpresa().getRut()));
        doc.add(linea("Profesional: ",
                asesoria.getProfesional().getUsuario().getNombre() + " "
                        + asesoria.getProfesional().getUsuario().getApellido()));
        doc.add(linea("Tipo: ", asesoria.getTipo().name()));
        doc.add(linea("Estado: ", asesoria.getEstado().name()));
        doc.add(linea("Fecha de solicitud: ", asesoria.getFechaSolicitud().format(F_FECHA)));
        if (asesoria.getFechaAtencion() != null) {
            doc.add(linea("Fecha de atención: ", asesoria.getFechaAtencion().format(F_FECHA)));
        }
        doc.add(linea("Asesoría extra: ", asesoria.isEsAsesoriaExtra() ? "Sí" : "No"));
        doc.add(linea("Motivo: ", asesoria.getMotivo()));
        doc.add(espacio());

        doc.add(seccion("Accidentes registrados"));
        doc.add(tablaAccidentes(accidentes));
        doc.add(espacio());

        doc.add(seccion("Fiscalizaciones registradas"));
        doc.add(tablaFiscalizaciones(fiscalizaciones));
        doc.add(espacio());

        doc.add(seccion("Propuestas de mejora"));
        doc.add(tablaPropuestas(propuestas));

        doc.close();
        return out.toByteArray();
    }

    private PdfPTable tablaAccidentes(List<Accidente> accidentes) {
        PdfPTable tabla = new PdfPTable(new float[]{2f, 2f, 3f, 2f});
        tabla.setWidthPercentage(100);
        tabla.addCell(celdaCabecera("Fecha"));
        tabla.addCell(celdaCabecera("Gravedad"));
        tabla.addCell(celdaCabecera("Trabajador"));
        tabla.addCell(celdaCabecera("Días perdidos"));
        if (accidentes.isEmpty()) {
            tabla.addCell(celdaVacia("Sin accidentes registrados.", 4));
            return tabla;
        }
        for (Accidente a : accidentes) {
            tabla.addCell(new Phrase(a.getFechaOcurrencia().format(F_FECHA), NORMAL));
            tabla.addCell(new Phrase(a.getGravedad().name(), NORMAL));
            tabla.addCell(new Phrase(a.getTrabajadorAfectado() != null ? a.getTrabajadorAfectado() : "—", NORMAL));
            tabla.addCell(new Phrase(a.getDiasPerdidos() != null ? a.getDiasPerdidos().toString() : "—", NORMAL));
        }
        return tabla;
    }

    private PdfPTable tablaFiscalizaciones(List<Fiscalizacion> fiscalizaciones) {
        PdfPTable tabla = new PdfPTable(new float[]{2f, 3f, 2f, 3f});
        tabla.setWidthPercentage(100);
        tabla.addCell(celdaCabecera("Fecha"));
        tabla.addCell(celdaCabecera("Entidad"));
        tabla.addCell(celdaCabecera("Resultado"));
        tabla.addCell(celdaCabecera("Motivo"));
        if (fiscalizaciones.isEmpty()) {
            tabla.addCell(celdaVacia("Sin fiscalizaciones registradas.", 4));
            return tabla;
        }
        for (Fiscalizacion f : fiscalizaciones) {
            tabla.addCell(new Phrase(f.getFecha().format(F_FECHA), NORMAL));
            tabla.addCell(new Phrase(f.getEntidadFiscalizadora().name(), NORMAL));
            tabla.addCell(new Phrase(f.getResultado() != null ? f.getResultado().name() : "—", NORMAL));
            tabla.addCell(new Phrase(f.getMotivo() != null ? f.getMotivo() : "—", NORMAL));
        }
        return tabla;
    }

    private PdfPTable tablaPropuestas(List<PropuestaMejora> propuestas) {
        PdfPTable tabla = new PdfPTable(new float[]{4f, 2f, 2f, 2f});
        tabla.setWidthPercentage(100);
        tabla.addCell(celdaCabecera("Descripción"));
        tabla.addCell(celdaCabecera("Responsable"));
        tabla.addCell(celdaCabecera("Fecha límite"));
        tabla.addCell(celdaCabecera("Estado"));
        if (propuestas.isEmpty()) {
            tabla.addCell(celdaVacia("Sin propuestas de mejora registradas.", 4));
            return tabla;
        }
        for (PropuestaMejora p : propuestas) {
            tabla.addCell(new Phrase(p.getDescripcion(), NORMAL));
            tabla.addCell(new Phrase(p.getResponsable() != null ? p.getResponsable() : "—", NORMAL));
            tabla.addCell(new Phrase(p.getFechaLimite() != null ? p.getFechaLimite().format(F_FECHA) : "—", NORMAL));
            tabla.addCell(new Phrase(p.getEstado().name(), NORMAL));
        }
        return tabla;
    }

    private PdfPCell celdaCabecera(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, CABECERA));
        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
        return celda;
    }

    private PdfPCell celdaVacia(String texto, int colspan) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, NORMAL));
        celda.setColspan(colspan);
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
        p.add(new Phrase(valor != null ? valor : "—", NORMAL));
        return p;
    }

    private Paragraph espacio() {
        return new Paragraph(" ", NORMAL);
    }
}
