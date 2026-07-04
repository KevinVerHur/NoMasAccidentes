package com.example.NoMasAccidentes.service.reporte;

import com.example.NoMasAccidentes.model.reporte.ReporteMensual;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfTemplate;
import org.openpdf.text.pdf.PdfWriter;
import com.example.NoMasAccidentes.service.pdf.PdfMarca;
import org.springframework.stereotype.Service;

/**
 * Genera el PDF del reporte mensual de gestión por cliente (RF38, RF39) con
 * OpenPDF. Incluye un gráfico de barras de la composición del periodo (dibujado
 * de forma nativa) y una firma institucional al final.
 */
@Service
public class ReporteMensualPdfService {

    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Locale ES = new Locale("es", "CL");

    private static final Font TITULO    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, PdfMarca.NAVY);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PdfMarca.NAVY);
    private static final Font NORMAL    = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font CABECERA  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font PIE       = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

    private static final String[] CATEGORIAS = {"Visitas", "Capacit.", "Asesorías", "Llamados", "Accid.", "Multas"};
    private static final Color[] COLORES = {
            new Color(0x1a5fb4), new Color(0x2a9d8f), new Color(0xe9c46a),
            new Color(0xf0a500), new Color(0xe23d3d), new Color(0x8e44ad),
    };

    public byte[] generar(ReporteMensual r) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        PdfMarca.aplicar(doc, writer);
        doc.open();

        Paragraph titulo = new Paragraph("Reporte Mensual de Gestión", TITULO);
        titulo.setSpacingAfter(2);
        doc.add(titulo);
        doc.add(espacio());

        doc.add(seccion("Datos del reporte"));
        doc.add(linea("Empresa: ", r.getEmpresa().getRazonSocial()));
        doc.add(linea("RUT: ", r.getEmpresa().getRut()));
        doc.add(linea("Periodo: ", nombreMes(r.getMes()) + " " + r.getAnio()));
        doc.add(linea("Fecha de emisión: ", r.getFechaEmision().format(F_FECHA)));
        doc.add(espacio());

        doc.add(seccion("Resumen de actividades del periodo"));
        doc.add(tablaTotales(r));
        doc.add(espacio());

        doc.add(seccion("Resumen de composición por periodo"));
        int[] valores = {
                r.getTotalVisitas(), r.getTotalCapacitaciones(), r.getTotalAsesorias(),
                r.getTotalLlamados(), r.getTotalAccidentes(), r.getTotalMultas(),
        };
        int maximo = 0;
        for (int v : valores) {
            maximo = Math.max(maximo, v);
        }
        if (maximo == 0) {
            doc.add(new Paragraph("Sin actividad registrada en el periodo.", NORMAL));
        } else {
            doc.add(graficoBarras(writer, valores, maximo));
        }

        doc.add(espacio());
        doc.add(firma());

        doc.close();
        return out.toByteArray();
    }

    private PdfPTable tablaTotales(ReporteMensual r) {
        PdfPTable tabla = new PdfPTable(new float[]{3f, 1f});
        tabla.setWidthPercentage(100);
        tabla.addCell(celdaCabecera("Concepto"));
        tabla.addCell(celdaCabecera("Total"));

        fila(tabla, "Visitas realizadas", r.getTotalVisitas());
        fila(tabla, "Capacitaciones realizadas", r.getTotalCapacitaciones());
        fila(tabla, "Asesorías atendidas", r.getTotalAsesorias());
        fila(tabla, "Llamados al centro de atención", r.getTotalLlamados());
        fila(tabla, "Accidentes registrados", r.getTotalAccidentes());
        fila(tabla, "Multas emitidas", r.getTotalMultas());
        fila(tabla, "Costos extra del periodo ($)", r.getCostosExtra().toPlainString());
        return tabla;
    }

    /** Dibuja un gráfico de barras de la composición del periodo en un template embebido como imagen. */
    private Image graficoBarras(PdfWriter writer, int[] valores, int maximo) {
        final float ancho = 520f, alto = 200f;
        final float izq = 30f, abajo = 28f, arriba = 16f, der = 10f;
        final float plotAncho = ancho - izq - der;
        final float plotAlto = alto - abajo - arriba;
        final float slot = plotAncho / valores.length;
        final float barAncho = slot * 0.55f;

        PdfTemplate tpl = writer.getDirectContent().createTemplate(ancho, alto);
        BaseFont bf = FontFactory.getFont(FontFactory.HELVETICA, 7).getBaseFont();

        // Eje base
        tpl.setColorStroke(new Color(0xCCCCCC));
        tpl.moveTo(izq, abajo);
        tpl.lineTo(ancho - der, abajo);
        tpl.stroke();

        for (int i = 0; i < valores.length; i++) {
            float barAlto = (valores[i] / (float) maximo) * plotAlto;
            float x = izq + i * slot + (slot - barAncho) / 2f;

            tpl.setColorFill(COLORES[i % COLORES.length]);
            tpl.rectangle(x, abajo, barAncho, barAlto);
            tpl.fill();

            float centro = x + barAncho / 2f;
            texto(tpl, bf, String.valueOf(valores[i]), centro, abajo + barAlto + 3f, new Color(0x333333));
            texto(tpl, bf, CATEGORIAS[i], centro, abajo - 11f, new Color(0x666666));
        }

        Image img = Image.getInstance(tpl);
        img.scaleToFit(500f, 200f);
        return img;
    }

    private void texto(PdfTemplate tpl, BaseFont bf, String t, float x, float y, Color color) {
        tpl.beginText();
        tpl.setColorFill(color);
        tpl.setFontAndSize(bf, 7);
        tpl.showTextAligned(Element.ALIGN_CENTER, t, x, y, 0);
        tpl.endText();
    }

    private Paragraph firma() {
        Paragraph p = new Paragraph();
        p.setSpacingBefore(18);
        p.add(new Phrase("Atentamente,\n", NORMAL));
        p.add(new Phrase("No Más Accidentes — Prevención de Riesgos Laborales\n", CABECERA));
        p.add(new Phrase("contacto@nomasaccidentes.cl · +56 2 2345 6789\n", NORMAL));
        p.add(new Phrase("Documento generado automáticamente; no requiere firma manuscrita.", PIE));
        return p;
    }

    private void fila(PdfPTable tabla, String concepto, Object valor) {
        tabla.addCell(new Phrase(concepto, NORMAL));
        PdfPCell celda = new PdfPCell(new Phrase(String.valueOf(valor), NORMAL));
        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(celda);
    }

    private PdfPCell celdaCabecera(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, CABECERA));
        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
        return celda;
    }

    private String nombreMes(int mes) {
        if (mes < 1 || mes > 12) {
            return String.valueOf(mes);
        }
        String nombre = java.time.Month.of(mes).getDisplayName(TextStyle.FULL, ES);
        return nombre.substring(0, 1).toUpperCase(ES) + nombre.substring(1);
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
