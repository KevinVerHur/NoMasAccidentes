package com.example.NoMasAccidentes.service.informe;

import com.example.NoMasAccidentes.model.visita.EstadoCumplimiento;
import com.example.NoMasAccidentes.model.visita.ItemChequeo;
import com.example.NoMasAccidentes.model.visita.ResultadoChequeo;
import com.example.NoMasAccidentes.model.visita.Visita;
import com.example.NoMasAccidentes.service.pdf.PdfMarca;
import java.awt.Color;
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
 * Genera el PDF del informe posterior a una visita (RF19) con OpenPDF: datos de
 * la visita, lista de chequeo marcada (Cumple/No cumple/No aplica) con su norma
 * legal, observaciones y firma del profesional que la realizó.
 */
@Service
public class InformePdfService {

    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter F_FECHA_HORA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static final Font TITULO    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, PdfMarca.NAVY);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PdfMarca.NAVY);
    private static final Font NORMAL    = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font CABECERA  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

    private static final Color VERDE = new Color(0x15, 0x80, 0x3D);
    private static final Color ROJO  = new Color(0xB9, 0x1C, 0x1C);
    private static final Color GRIS  = new Color(0x6B, 0x72, 0x80);

    public byte[] generar(Visita visita) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        PdfMarca.aplicar(doc, writer);
        doc.open();

        Paragraph titulo = new Paragraph("Informe de Visita", TITULO);
        titulo.setSpacingAfter(2);
        doc.add(titulo);
        doc.add(espacio());

        doc.add(seccion("Datos de la visita"));
        doc.add(linea("Empresa: ", visita.getEmpresa().getRazonSocial()));
        doc.add(linea("RUT: ", visita.getEmpresa().getRut()));
        doc.add(linea("Profesional: ", nombreProfesional(visita)));
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
        doc.add(tablaChequeo(visita));
        doc.add(espacio());

        doc.add(seccion("Observaciones"));
        String obs = visita.getObservaciones() != null && !visita.getObservaciones().isBlank()
                ? visita.getObservaciones()
                : "Sin observaciones registradas.";
        doc.add(new Paragraph(obs, NORMAL));

        doc.add(PdfMarca.firmaProfesional(nombreProfesional(visita)));

        doc.close();
        return out.toByteArray();
    }

    private PdfPTable tablaChequeo(Visita visita) {
        PdfPTable tabla = new PdfPTable(new float[]{0.5f, 3.4f, 2f, 1.3f, 2.4f});
        tabla.setWidthPercentage(100);
        tabla.addCell(celdaCabecera("#"));
        tabla.addCell(celdaCabecera("Ítem"));
        tabla.addCell(celdaCabecera("Norma legal"));
        tabla.addCell(celdaCabecera("Resultado"));
        tabla.addCell(celdaCabecera("Observación"));

        List<ResultadoChequeo> resultados = visita.getResultados();
        if (resultados != null && !resultados.isEmpty()) {
            int i = 1;
            for (ResultadoChequeo r : resultados) {
                ItemChequeo item = r.getItem();
                tabla.addCell(new Phrase(String.valueOf(i++), NORMAL));
                tabla.addCell(new Phrase(item.getDescripcion(), NORMAL));
                tabla.addCell(new Phrase(valor(item.getNormaLegal()), NORMAL));
                tabla.addCell(celdaResultado(r.getEstado()));
                tabla.addCell(new Phrase(valor(r.getObservacion()), NORMAL));
            }
            return tabla;
        }

        // Sin marcado (visita antigua o no evaluada): se lista la plantilla.
        var items = visita.getListaChequeo() != null ? visita.getListaChequeo().getItems() : null;
        if (items == null || items.isEmpty()) {
            PdfPCell vacio = new PdfPCell(new Phrase("La lista de chequeo no tiene ítems.", NORMAL));
            vacio.setColspan(5);
            tabla.addCell(vacio);
            return tabla;
        }
        int i = 1;
        for (ItemChequeo item : items) {
            tabla.addCell(new Phrase(String.valueOf(i++), NORMAL));
            tabla.addCell(new Phrase(item.getDescripcion(), NORMAL));
            tabla.addCell(new Phrase(valor(item.getNormaLegal()), NORMAL));
            tabla.addCell(new Phrase("Sin evaluar", NORMAL));
            tabla.addCell(new Phrase("—", NORMAL));
        }
        return tabla;
    }

    private PdfPCell celdaResultado(EstadoCumplimiento estado) {
        String texto;
        Color color;
        switch (estado) {
            case CUMPLE -> { texto = "Cumple"; color = VERDE; }
            case NO_CUMPLE -> { texto = "No cumple"; color = ROJO; }
            default -> { texto = "No aplica"; color = GRIS; }
        }
        return new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, color)));
    }

    private PdfPCell celdaCabecera(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, CABECERA));
        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
        celda.setBackgroundColor(PdfMarca.NAVY);
        celda.setPadding(4);
        return celda;
    }

    private String nombreProfesional(Visita visita) {
        return visita.getProfesional().getUsuario().getNombre() + " "
                + visita.getProfesional().getUsuario().getApellido();
    }

    private String valor(String v) {
        return v != null && !v.isBlank() ? v : "—";
    }

    private Paragraph seccion(String texto) {
        Paragraph p = new Paragraph(texto, SUBTITULO);
        p.setSpacingBefore(6);
        p.setSpacingAfter(4);
        return p;
    }

    private Paragraph linea(String etiqueta, String valor) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(etiqueta, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        p.add(new Phrase(valor != null ? valor : "—", NORMAL));
        return p;
    }

    private Paragraph espacio() {
        return new Paragraph(" ", NORMAL);
    }
}
