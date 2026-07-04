package com.example.NoMasAccidentes.service.pago;

import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.pago.Pago;
import com.example.NoMasAccidentes.service.pdf.PdfMarca;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
 * Genera el comprobante de pago (boleta) en PDF de una cuota pagada por el
 * cliente vía Webpay (RF09), con la identidad visual institucional {@link PdfMarca}.
 */
@Service
public class BoletaPagoPdfService {

    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Locale ES = new Locale("es", "CL");

    private static final Font TITULO    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, PdfMarca.NAVY);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PdfMarca.NAVY);
    private static final Font NORMAL    = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font CABECERA  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font OK        = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(0x1E, 0x8E, 0x3E));
    private static final Font PIE       = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

    public byte[] generar(Pago pago) {
        Empresa empresa = pago.getPlan().getEmpresa();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        PdfMarca.aplicar(doc, writer);
        doc.open();

        Paragraph titulo = new Paragraph("Comprobante de Pago", TITULO);
        titulo.setSpacingAfter(2);
        doc.add(titulo);
        doc.add(new Paragraph("Nº comprobante: " + numeroComprobante(pago), NORMAL));
        doc.add(espacio());

        doc.add(seccion("Datos de la empresa"));
        doc.add(linea("Razón social: ", empresa.getRazonSocial()));
        doc.add(linea("RUT: ", empresa.getRut()));
        doc.add(espacio());

        doc.add(seccion("Detalle del pago"));
        doc.add(tablaDetalle(pago));
        doc.add(espacio());

        Paragraph estado = new Paragraph("✓ Transacción autorizada", OK);
        estado.setSpacingBefore(6);
        doc.add(estado);

        doc.add(espacio());
        doc.add(cierre());

        doc.close();
        return out.toByteArray();
    }

    private PdfPTable tablaDetalle(Pago pago) {
        PdfPTable tabla = new PdfPTable(new float[]{2f, 3f});
        tabla.setWidthPercentage(100);
        fila(tabla, "Cuota N°", "#" + pago.getNumeroCuota());
        fila(tabla, "Monto pagado", clp(pago.getMonto().doubleValue()));
        fila(tabla, "Fecha de pago", pago.getFechaPago() != null ? pago.getFechaPago().format(F_FECHA) : "—");
        fila(tabla, "Medio de pago", pago.getMedioPago() != null ? pago.getMedioPago() : "—");
        if (pago.getWebpayOrdenCompra() != null) {
            fila(tabla, "Orden de compra", pago.getWebpayOrdenCompra());
        }
        return tabla;
    }

    private String numeroComprobante(Pago pago) {
        return pago.getWebpayOrdenCompra() != null
                ? pago.getWebpayOrdenCompra()
                : "NMA-" + pago.getId();
    }

    private String clp(double monto) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(ES);
        nf.setMaximumFractionDigits(0);
        return nf.format(monto);
    }

    private Paragraph cierre() {
        Paragraph p = new Paragraph();
        p.setSpacingBefore(18);
        p.add(new Phrase("No Más Accidentes — Prevención de Riesgos Laborales\n", CABECERA));
        p.add(new Phrase("contacto@nomasaccidentes.cl · +56 2 2345 6789\n", NORMAL));
        p.add(new Phrase("Documento generado automáticamente; no requiere firma manuscrita.", PIE));
        return p;
    }

    private void fila(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, CABECERA));
        c1.setBorder(0);
        c1.setPaddingBottom(4);
        PdfPCell c2 = new PdfPCell(new Phrase(valor, NORMAL));
        c2.setBorder(0);
        c2.setPaddingBottom(4);
        tabla.addCell(c1);
        tabla.addCell(c2);
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
