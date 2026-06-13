package com.example.NoMasAccidentes.service.informe;

import com.example.NoMasAccidentes.model.visita.ItemChequeo;
import com.example.NoMasAccidentes.model.visita.Visita;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
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
 * Genera el PDF del informe posterior a una visita (RF15) con OpenPDF.
 */
@Service
public class InformePdfService {

    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter F_FECHA_HORA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static final Font TITULO    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font NORMAL    = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font CABECERA  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    public byte[] generar(Visita visita) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter.getInstance(doc, out);
        doc.open();

        doc.add(new Paragraph("Informe de Visita", TITULO));
        doc.add(new Paragraph("No Más Accidentes — Prevención de riesgos laborales", NORMAL));
        doc.add(espacio());

        doc.add(seccion("Datos de la visita"));
        doc.add(linea("Cliente: ", visita.getCliente().getRazonSocial()));
        doc.add(linea("RUT: ", visita.getCliente().getRut()));
        doc.add(linea("Profesional: ",
                visita.getProfesional().getUsuario().getNombre() + " "
                        + visita.getProfesional().getUsuario().getApellido()));
        doc.add(linea("Fecha programada: ", visita.getFechaProgramada().format(F_FECHA)));
        if (visita.getFechaInicio() != null) {
            doc.add(linea("Inicio: ", visita.getFechaInicio().format(F_FECHA_HORA)));
        }
        if (visita.getFechaFin() != null) {
            doc.add(linea("Término: ", visita.getFechaFin().format(F_FECHA_HORA)));
        }
        doc.add(linea("Estado: ", visita.getEstado().name()));
        if (visita.getTipoRevision() != null) {
            doc.add(linea("Tipo de revisión: ", visita.getTipoRevision()));
        }
        doc.add(espacio());

        doc.add(seccion("Lista de chequeo aplicada"));
        doc.add(tablaItems(visita));
        doc.add(espacio());

        doc.add(seccion("Observaciones"));
        String obs = visita.getObservaciones() != null && !visita.getObservaciones().isBlank()
                ? visita.getObservaciones()
                : "Sin observaciones registradas.";
        doc.add(new Paragraph(obs, NORMAL));

        doc.close();
        return out.toByteArray();
    }

    private PdfPTable tablaItems(Visita visita) {
        PdfPTable tabla = new PdfPTable(new float[]{1f, 4f, 2f});
        tabla.setWidthPercentage(100);
        tabla.addCell(celdaCabecera("#"));
        tabla.addCell(celdaCabecera("Ítem"));
        tabla.addCell(celdaCabecera("Categoría"));

        var items = visita.getListaChequeo() != null ? visita.getListaChequeo().getItems() : null;
        if (items == null || items.isEmpty()) {
            PdfPCell vacio = new PdfPCell(new Phrase("La lista de chequeo no tiene ítems.", NORMAL));
            vacio.setColspan(3);
            tabla.addCell(vacio);
            return tabla;
        }
        int i = 1;
        for (ItemChequeo item : items) {
            tabla.addCell(new Phrase(String.valueOf(i++), NORMAL));
            tabla.addCell(new Phrase(item.getDescripcion(), NORMAL));
            tabla.addCell(new Phrase(item.getCategoria() != null ? item.getCategoria() : "—", NORMAL));
        }
        return tabla;
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
        p.add(new Phrase(valor != null ? valor : "—", NORMAL));
        return p;
    }

    private Paragraph espacio() {
        return new Paragraph(" ", NORMAL);
    }
}
