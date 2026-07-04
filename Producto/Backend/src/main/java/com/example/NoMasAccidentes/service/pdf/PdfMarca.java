package com.example.NoMasAccidentes.service.pdf;

import java.awt.Color;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Identidad visual común de los PDF institucionales de No Más Accidentes:
 * banda superior con wordmark, pie con datos de contacto y numeración de
 * página. Se aplica igual en todos los documentos para dar consistencia.
 */
public final class PdfMarca {

    /** Azul institucional (mismo que la app: --color-azul #1a3a5c). */
    public static final Color NAVY = new Color(0x1A, 0x3A, 0x5C);
    private static final Color NAVY_SUAVE = new Color(0xC9, 0xD8, 0xE8);
    private static final Color GRIS = new Color(0x77, 0x77, 0x77);
    private static final Color LINEA = new Color(0xCC, 0xCC, 0xCC);

    private static final float ALTURA_BANDA = 46f;
    private static final String CONTACTO =
            "No Más Accidentes · contacto@nomasaccidentes.cl · +56 2 2345 6789";

    private PdfMarca() {
    }

    /** Prepara márgenes y registra el encabezado/pie de marca. Llamar antes de {@code doc.open()}. */
    public static void aplicar(Document doc, PdfWriter writer) {
        doc.setMargins(40, 40, 64, 48);
        writer.setPageEvent(new Evento());
    }

    /** Bloque de firma del profesional que realizó el documento. */
    public static Paragraph firmaProfesional(String nombre) {
        Font fLinea  = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fNombre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fCargo  = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(0x55, 0x55, 0x55));

        Paragraph p = new Paragraph();
        p.setSpacingBefore(40);
        p.setAlignment(Element.ALIGN_RIGHT);
        p.add(new Phrase("__________________________________\n", fLinea));
        p.add(new Phrase((nombre != null && !nombre.isBlank() ? nombre : "—") + "\n", fNombre));
        p.add(new Phrase("Profesional de Prevención de Riesgos\n", fCargo));
        p.add(new Phrase("No Más Accidentes", fCargo));
        return p;
    }

    private static final class Evento extends PdfPageEventHelper {

        private final Font wordmark = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.WHITE);
        private final Font tagline  = FontFactory.getFont(FontFactory.HELVETICA, 8, NAVY_SUAVE);
        private final Font pie      = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, GRIS);

        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            Rectangle pagina = doc.getPageSize();
            float izq = doc.leftMargin();
            float der = pagina.getWidth() - doc.rightMargin();

            // Banda superior con wordmark.
            PdfContentByte fondo = writer.getDirectContentUnder();
            fondo.setColorFill(NAVY);
            fondo.rectangle(0, pagina.getTop() - ALTURA_BANDA, pagina.getWidth(), ALTURA_BANDA);
            fondo.fill();

            PdfContentByte cb = writer.getDirectContent();
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("NO MÁS ACCIDENTES", wordmark), izq, pagina.getTop() - 22, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("Prevención de Riesgos Laborales", tagline), izq, pagina.getTop() - 36, 0);

            // Pie: línea + contacto + numeración.
            float yPie = pagina.getBottom() + 26;
            cb.setColorStroke(LINEA);
            cb.setLineWidth(0.5f);
            cb.moveTo(izq, yPie + 9);
            cb.lineTo(der, yPie + 9);
            cb.stroke();
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(CONTACTO, pie), izq, yPie, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), pie), der, yPie, 0);
        }
    }
}
