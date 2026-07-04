package com.example.NoMasAccidentes.service.usuario;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Envío de correos transaccionales del sistema.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CorreoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.admin.email:${spring.mail.username}}")
    private String adminEmail;

    /**
     * Envía el correo de recuperación de contraseña de forma asíncrona.
     * El enlace tiene validez de 1 hora.
     */
    @Async
    public void enviarRecuperacionPassword(String destinatario, String token) {
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Más Accidentes — Recuperación de contraseña");
            helper.setText(construirHtml(token), true);

            mailSender.send(mensaje);
            log.info("Correo de recuperación enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar correo de recuperación a {}: {}", destinatario, e.getMessage());
        }
    }

    /**
     * Envía la invitación a un cliente recién dado de alta para que defina su
     * contraseña y acceda al portal. Reutiliza el flujo de restablecimiento; el
     * enlace tiene validez de 48 horas (ver ClienteService).
     */
    @Async
    public void enviarInvitacionCliente(String destinatario, String token) {
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Más Accidentes — Activa tu cuenta de cliente");
            helper.setText(construirHtmlInvitacion(token), true);

            mailSender.send(mensaje);
            log.info("Correo de invitación enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar correo de invitación a {}: {}", destinatario, e.getMessage());
        }
    }

    @Async
    public void enviarRecordatorioVisita(String destinatario, String cliente, String fecha, String direccion){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Mas Accidentes - Recordatorio de visita");
            helper.setText(construirHtmlRecordatorioVisita(cliente, fecha, direccion), true);

            mailSender.send(mensaje);
            log.info("Recordatorio de visita enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar recordatorio de visita a {}: {}", destinatario, e.getMessage());
        }
    }

    @Async
    public void enviarAlertaPagoPendiente(String destinatario, String cliente, String vencimiento, String monto){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Mas Accidentes - Pago pendiente");
            helper.setText(construirHtmlPagoPendiente(cliente, vencimiento, monto), true);

            mailSender.send(mensaje);
            log.info("Alerta de pago pendiente enviada a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar alerta de pago pendiente a {}: {}", destinatario, e.getMessage());
        }
    }



    /**
     * RF30: recuerda al cliente una capacitación programada (se envía 3 días antes).
     */
    @Async
    public void enviarRecordatorioCapacitacion(String destinatario, String cliente, String curso, String fecha, String hora){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Mas Accidentes - Recordatorio de capacitacion");
            helper.setText(construirHtmlRecordatorioCapacitacion(cliente, curso, fecha, hora), true);

            mailSender.send(mensaje);
            log.info("Recordatorio de capacitacion enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar recordatorio de capacitacion a {}: {}", destinatario, e.getMessage());
        }
    }

    /**
     * RF32: avisa al administrador que una capacitación programada no se realizó.
     */
    @Async
    public void enviarAlertaCapacitacionNoRealizada(String cliente, String curso, String fechaProgramada){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(adminEmail);
            helper.setSubject("No Mas Accidentes - Capacitacion no realizada");
            helper.setText(construirHtmlCapacitacionNoRealizada(cliente, curso, fechaProgramada), true);

            mailSender.send(mensaje);
            log.info("Alerta de capacitacion no realizada enviada al administrador por cliente {}", cliente);
        } catch (Exception e) {
            log.error("Error al enviar alerta de capacitacion no realizada al administrador: {}", e.getMessage());
        }
    }

    @Async
    public void enviarAlertaActividadPreventivaVencida(String cliente, String actividad, String normativa, String responsable, String vencimiento){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(adminEmail);
            helper.setSubject("No Mas Accidentes - Actividad preventiva vencida");
            helper.setText(construirHtmlActividadPreventivaVencida(cliente, actividad, normativa, responsable, vencimiento), true);

            mailSender.send(mensaje);
            log.info("Alerta de actividad preventiva vencida enviada al administrador por cliente {}", cliente);
        } catch (Exception e) {
            log.error("Error al enviar alerta de actividad preventiva vencida al administrador: {}", e.getMessage());
        }
    }

    /**
     * RF46/RF38: envía al cliente su reporte mensual de gestión con el PDF adjunto,
     * tras el cierre mensual automático.
     */
    @Async
    public void enviarReporteMensual(String destinatario, String cliente, int mes, int anio, byte[] pdf){
        String periodo = nombrePeriodo(mes, anio);
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Mas Accidentes - Reporte mensual " + periodo);
            helper.setText(construirHtmlReporteMensual(cliente, periodo), true);
            helper.addAttachment(
                    String.format("reporte-%d-%02d.pdf", anio, mes),
                    new ByteArrayResource(pdf),
                    "application/pdf");

            mailSender.send(mensaje);
            log.info("Reporte mensual {} enviado a {}", periodo, destinatario);
        } catch (Exception e) {
            log.error("Error al enviar reporte mensual a {}: {}", destinatario, e.getMessage());
        }
    }

    /**
     * RF09: envía al representante el comprobante de un pago realizado, con el
     * PDF adjunto.
     */
    @Async
    public void enviarComprobantePago(String destinatario, String empresa, int numeroCuota, String monto, String fecha, byte[] pdf){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Mas Accidentes - Comprobante de pago cuota #" + numeroCuota);
            helper.setText(construirHtmlComprobantePago(empresa, numeroCuota, monto, fecha), true);
            helper.addAttachment(
                    "comprobante-cuota-" + numeroCuota + ".pdf",
                    new ByteArrayResource(pdf),
                    "application/pdf");

            mailSender.send(mensaje);
            log.info("Comprobante de pago (cuota {}) enviado a {}", numeroCuota, destinatario);
        } catch (Exception e) {
            log.error("Error al enviar comprobante de pago a {}: {}", destinatario, e.getMessage());
        }
    }

    /** RF09: avisa al administrador que un cliente pagó una cuota. */
    @Async
    public void enviarAvisoPagoRecibido(String empresa, int numeroCuota, String monto, String fecha){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(adminEmail);
            helper.setSubject("No Mas Accidentes - Pago recibido de " + empresa);
            helper.setText(construirHtmlAvisoPagoRecibido(empresa, numeroCuota, monto, fecha), true);

            mailSender.send(mensaje);
            log.info("Aviso de pago recibido enviado al administrador (empresa {}, cuota {})", empresa, numeroCuota);
        } catch (Exception e) {
            log.error("Error al enviar aviso de pago recibido al administrador: {}", e.getMessage());
        }
    }

    private String construirHtmlAvisoPagoRecibido(String empresa, int numeroCuota, String monto, String fecha){
        return """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:28px;background:#f5f7fa;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Un cliente registró el pago de una cuota.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Empresa:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Cuota:</strong> #%d</p>
              <p style="color:#3d4856;font-size:14px"><strong>Monto:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Fecha de pago:</strong> %s</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(empresa, numeroCuota, monto, fecha);
    }

    private String construirHtmlComprobantePago(String empresa, int numeroCuota, String monto, String fecha){
        return """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:28px;background:#f5f7fa;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Hola %s, registramos correctamente el pago de tu cuota. Adjuntamos el comprobante en PDF.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Cuota:</strong> #%d</p>
              <p style="color:#3d4856;font-size:14px"><strong>Monto:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Fecha de pago:</strong> %s</p>
              <div style="margin-top:24px;padding-top:14px;border-top:1px solid #e2e8f0;color:#3d4856;font-size:13px">
                <p style="margin:0">Gracias por tu pago,</p>
                <p style="margin:4px 0 0;font-weight:bold;color:#18395a">No Más Accidentes — Prevención de Riesgos Laborales</p>
                <p style="margin:2px 0 0;color:#6b7280">contacto@nomasaccidentes.cl · +56 2 2345 6789</p>
              </div>
              <p style="color:#8b95a1;font-size:12px;margin-top:12px">Este comprobante es una constancia interna de pago y no constituye un documento tributario (boleta/factura electronica del SII).</p>
            </div>
        """.formatted(empresa, numeroCuota, monto, fecha);
    }

    private String nombrePeriodo(int mes, int anio){
        if (mes < 1 || mes > 12) {
            return mes + "/" + anio;
        }
        String nombre = Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es", "CL"));
        return nombre.substring(0, 1).toUpperCase() + nombre.substring(1) + " " + anio;
    }

    private String construirHtmlReporteMensual(String cliente, String periodo){
        return """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:28px;background:#f5f7fa;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Hola %s, tu reporte mensual de gestion ya esta disponible.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Periodo:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px">Adjuntamos el documento PDF con el resumen de las actividades realizadas durante el periodo (visitas, capacitaciones, asesorias, llamados, accidentes, multas y costos extra).</p>
              <div style="margin-top:24px;padding-top:14px;border-top:1px solid #e2e8f0;color:#3d4856;font-size:13px">
                <p style="margin:0">Atentamente,</p>
                <p style="margin:4px 0 0;font-weight:bold;color:#18395a">No Más Accidentes — Prevención de Riesgos Laborales</p>
                <p style="margin:2px 0 0;color:#6b7280">contacto@nomasaccidentes.cl · +56 2 2345 6789</p>
              </div>
              <p style="color:#8b95a1;font-size:12px;margin-top:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(cliente, periodo);
    }

    private String construirHtmlInvitacion(String token) {
        String enlace = frontendUrl + "/restablecer-contrasena?token=" + token;
        return """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:32px;background:#f5f7fa;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px">🦺 <span style="color:#f0a500">No Más</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Tu consultora de prevención de riesgos te creó una cuenta para acceder al portal de clientes.</p>
              <p style="color:#3d4856;font-size:14px">Haz clic en el botón para definir tu contraseña y activar tu acceso. El enlace es válido por <strong>48 horas</strong>.</p>
              <div style="text-align:center;margin:28px 0">
                <a href="%s"
                   style="background:#18395a;color:white;padding:12px 28px;border-radius:8px;
                          text-decoration:none;font-weight:bold;font-size:14px;display:inline-block">
                  Activar mi cuenta
                </a>
              </div>
              <p style="color:#8b95a1;font-size:12px">Si no esperabas este correo, puedes ignorarlo.</p>
            </div>
            """.formatted(enlace);
    }

    private String construirHtml(String token) {
        String enlace = frontendUrl + "/restablecer-contrasena?token=" + token;
        return """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:32px;background:#f5f7fa;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px">🦺 <span style="color:#f0a500">No Más</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Recibimos una solicitud para restablecer tu contraseña.</p>
              <p style="color:#3d4856;font-size:14px">Haz clic en el botón para crear una nueva. El enlace es válido por <strong>1 hora</strong>.</p>
              <div style="text-align:center;margin:28px 0">
                <a href="%s"
                   style="background:#18395a;color:white;padding:12px 28px;border-radius:8px;
                          text-decoration:none;font-weight:bold;font-size:14px;display:inline-block">
                  Restablecer contraseña
                </a>
              </div>
              <p style="color:#8b95a1;font-size:12px">Si no solicitaste este cambio, ignora este mensaje.</p>
            </div>
            """.formatted(enlace);
    }

    private String construirHtmlRecordatorioVisita(String cliente, String fecha, String direccion){
        return """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:28px;background:#f5f7fa;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Hola, te recordamos que tienes una visita programada en las proximas 24 horas.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Cliente:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Fecha:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Direccion:</strong> %s</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(cliente, fecha, direccion);
    }

    private String construirHtmlPagoPendiente(String cliente, String vencimiento, String monto){
        return """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:28px;background:#fff7ed;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Registramos un pago pendiente asociado a tu cuenta.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Cliente:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Fecha de vencimiento:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Monto:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px">Por favor regulariza el pago para mantener el servicio activo.</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(cliente, vencimiento, monto);
    }




    private String construirHtmlRecordatorioCapacitacion(String cliente, String curso, String fecha, String hora){
        return """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:28px;background:#f5f7fa;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Hola, te recordamos que tienes una capacitacion programada en los proximos 3 dias.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Cliente:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Curso:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Fecha:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Hora:</strong> %s</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(cliente, curso, fecha, hora);
    }

    private String construirHtmlCapacitacionNoRealizada(String cliente, String curso, String fechaProgramada){
        return """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;padding:28px;background:#fff7ed;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Se detecto una capacitacion programada que no se realizo en la fecha prevista.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Cliente:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Curso:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Fecha programada:</strong> %s</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(cliente, curso, fechaProgramada);
    }

    private String construirHtmlActividadPreventivaVencida(String cliente, String actividad, String normativa, String responsable, String vencimiento){
        String normativaTexto = normativa == null || normativa.isBlank() ? "No especificada" : normativa;
        String responsableTexto = responsable == null || responsable.isBlank() ? "No especificado" : responsable;
        return """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;padding:28px;background:#fff7ed;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Se detecto una actividad preventiva vencida.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Cliente:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Actividad:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Normativa:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Responsable:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Fecha compromiso:</strong> %s</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(cliente, actividad, normativaTexto, responsableTexto, vencimiento);
    }

    // ─────────────────────────────────────────────
    //  Notificaciones por evento (Fase 1) — envío inmediato al crear el registro
    // ─────────────────────────────────────────────

    /** Avisa al profesional que se le planificó una nueva visita. */
    @Async
    public void enviarAvisoVisitaPlanificada(String destinatario, String empresa, String fecha, String direccion){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Mas Accidentes - Nueva visita planificada");
            helper.setText(construirHtmlAvisoVisitaPlanificada(empresa, fecha, direccion), true);

            mailSender.send(mensaje);
            log.info("Aviso de visita planificada enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar aviso de visita planificada a {}: {}", destinatario, e.getMessage());
        }
    }

    /** Avisa al cliente que se le programó una capacitación. */
    @Async
    public void enviarAvisoCapacitacionProgramada(String destinatario, String empresa, String curso, String fecha, String hora, String lugar){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Mas Accidentes - Nueva capacitacion programada");
            helper.setText(construirHtmlAvisoCapacitacionProgramada(empresa, curso, fecha, hora, lugar), true);

            mailSender.send(mensaje);
            log.info("Aviso de capacitacion programada enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar aviso de capacitacion programada a {}: {}", destinatario, e.getMessage());
        }
    }

    /** Avisa al cliente que se registró una asesoría a su nombre. */
    @Async
    public void enviarAvisoAsesoriaRegistrada(String destinatario, String empresa, String tipo, String motivo){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Mas Accidentes - Asesoria registrada");
            helper.setText(construirHtmlAvisoAsesoriaRegistrada(empresa, tipo, motivo), true);

            mailSender.send(mensaje);
            log.info("Aviso de asesoria registrada enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar aviso de asesoria registrada a {}: {}", destinatario, e.getMessage());
        }
    }

    private String construirHtmlAvisoVisitaPlanificada(String empresa, String fecha, String direccion){
        String direccionTexto = direccion == null || direccion.isBlank() ? "Por confirmar" : direccion;
        return """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:28px;background:#f5f7fa;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Se te asigno una nueva visita en terreno.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Empresa:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Fecha programada:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Direccion:</strong> %s</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(empresa, fecha, direccionTexto);
    }

    private String construirHtmlAvisoCapacitacionProgramada(String empresa, String curso, String fecha, String hora, String lugar){
        String lugarTexto = lugar == null || lugar.isBlank() ? "Por confirmar" : lugar;
        return """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:28px;background:#f5f7fa;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Hola %s, se programo una nueva capacitacion para tu empresa.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Curso:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Fecha:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Hora:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Lugar:</strong> %s</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(empresa, curso, fecha, hora, lugarTexto);
    }

    /** Avisa al administrador que un cliente envió una solicitud de servicio. */
    @Async
    public void enviarAvisoSolicitudRecibida(String empresa, String tipo, String descripcion, boolean urgente){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(adminEmail);
            helper.setSubject(urgente
                    ? "🚨 URGENTE - Accidente reportado por " + empresa
                    : "No Mas Accidentes - Nueva solicitud de " + tipo.toLowerCase());
            helper.setText(construirHtmlSolicitudRecibida(empresa, tipo, descripcion, urgente), true);

            mailSender.send(mensaje);
            log.info("Aviso de solicitud recibida enviado al administrador (empresa {}, urgente={})", empresa, urgente);
        } catch (Exception e) {
            log.error("Error al enviar aviso de solicitud recibida al administrador: {}", e.getMessage());
        }
    }

    /** Avisa al cliente la respuesta del admin a su solicitud (aprobada/rechazada). */
    @Async
    public void enviarAvisoSolicitudRespondida(String destinatario, String tipo, boolean aprobada, boolean esExtra, String comentario){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Mas Accidentes - Respuesta a tu solicitud de " + tipo.toLowerCase());
            helper.setText(construirHtmlSolicitudRespondida(tipo, aprobada, esExtra, comentario), true);

            mailSender.send(mensaje);
            log.info("Aviso de solicitud respondida enviado a {} (aprobada={})", destinatario, aprobada);
        } catch (Exception e) {
            log.error("Error al enviar aviso de solicitud respondida a {}: {}", destinatario, e.getMessage());
        }
    }

    /** Avisa al cliente que una de sus actividades preventivas venció sin cumplirse (RF49). */
    @Async
    public void enviarAvisoActividadVencidaCliente(String destinatario, String actividad, String normativa, String vencimiento){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("No Mas Accidentes - Actividad preventiva vencida");
            helper.setText(construirHtmlActividadVencidaCliente(actividad, normativa, vencimiento), true);

            mailSender.send(mensaje);
            log.info("Aviso de actividad vencida enviado al cliente {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar aviso de actividad vencida al cliente {}: {}", destinatario, e.getMessage());
        }
    }

    /** Avisa a la consultora que un cliente reportó haber cumplido su parte de una actividad. */
    @Async
    public void enviarAvisoCumplimientoReportado(String empresa, String actividad, String comentario){
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(adminEmail);
            helper.setSubject("No Mas Accidentes - Cliente reporto cumplimiento");
            helper.setText(construirHtmlCumplimientoReportado(empresa, actividad, comentario), true);

            mailSender.send(mensaje);
            log.info("Aviso de cumplimiento reportado enviado al administrador (empresa {})", empresa);
        } catch (Exception e) {
            log.error("Error al enviar aviso de cumplimiento reportado al administrador: {}", e.getMessage());
        }
    }

    private String construirHtmlActividadVencidaCliente(String actividad, String normativa, String vencimiento){
        String normativaTexto = normativa == null || normativa.isBlank() ? "No especificada" : normativa;
        return """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;padding:28px;background:#fff7ed;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Una actividad preventiva de tu empresa venció sin registrarse como cumplida.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Actividad:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Normativa:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Fecha limite:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px">Por favor regularizala a la brevedad para mantener el cumplimiento normativo.</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(actividad, normativaTexto, vencimiento);
    }

    private String construirHtmlCumplimientoReportado(String empresa, String actividad, String comentario){
        String comentarioTexto = comentario == null || comentario.isBlank() ? "Sin comentario" : comentario;
        return """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;padding:28px;background:#f5f7fa;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Un cliente reportó haber cumplido su parte de una actividad preventiva.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Empresa:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Actividad:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Comentario del cliente:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px">Verifica en terreno y marca la actividad como cumplida cuando corresponda.</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(empresa, actividad, comentarioTexto);
    }

    private String construirHtmlSolicitudRecibida(String empresa, String tipo, String descripcion, boolean urgente){
        String descripcionTexto = descripcion == null || descripcion.isBlank() ? "Sin detalle" : descripcion;
        String fondo = urgente ? "#fdecec" : "#fff7ed";
        String banner = urgente
                ? "<div style=\"background:#c0392b;color:#fff;font-size:14px;font-weight:bold;"
                    + "padding:10px 14px;border-radius:8px;margin-bottom:14px\">🚨 ACCIDENTE REPORTADO — ATENCIÓN URGENTE</div>"
                : "";
        String intro = urgente
                ? "Un cliente reportó un <strong>accidente</strong> y requiere atención inmediata."
                : "Un cliente envió una nueva solicitud de servicio.";
        String cierre = urgente
                ? "Ingresa al panel de solicitudes cuanto antes para asignar un profesional y coordinar la visita en terreno."
                : "Revisa la solicitud en el panel para aprobarla o rechazarla.";
        return """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;padding:28px;background:%s;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              %s
              <p style="color:#3d4856;font-size:14px">%s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Empresa:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Tipo:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Detalle:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px">%s</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(fondo, banner, intro, empresa, tipo, descripcionTexto, cierre);
    }

    private String construirHtmlSolicitudRespondida(String tipo, boolean aprobada, boolean esExtra, String comentario){
        String estado = aprobada ? "APROBADA" : "RECHAZADA";
        String color = aprobada ? "#f5f7fa" : "#fff7ed";
        String extraTexto = aprobada
                ? (esExtra
                    ? "<p style=\"color:#b45309;font-size:14px\"><strong>Nota:</strong> este servicio queda fuera de tu plan, por lo que se cobrará como adicional.</p>"
                    : "<p style=\"color:#3d4856;font-size:14px\">Este servicio está incluido en tu plan.</p>")
                : "";
        String comentarioTexto = comentario == null || comentario.isBlank() ? "" :
                "<p style=\"color:#3d4856;font-size:14px\"><strong>Comentario:</strong> %s</p>".formatted(comentario);
        return """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;padding:28px;background:%s;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Tu solicitud de <strong>%s</strong> fue <strong>%s</strong>.</p>
              %s
              %s
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(color, tipo.toLowerCase(), estado, extraTexto, comentarioTexto);
    }

    private String construirHtmlAvisoAsesoriaRegistrada(String empresa, String tipo, String motivo){
        String motivoTexto = motivo == null || motivo.isBlank() ? "Sin detalle" : motivo;
        return """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:28px;background:#f5f7fa;border-radius:12px">
              <h2 style="color:#18395a;margin-bottom:8px"><span style="color:#f0a500">No Mas</span> Accidentes</h2>
              <p style="color:#3d4856;font-size:14px">Hola %s, se registro una asesoria a nombre de tu empresa.</p>
              <p style="color:#3d4856;font-size:14px"><strong>Tipo:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px"><strong>Motivo:</strong> %s</p>
              <p style="color:#3d4856;font-size:14px">Un profesional de prevencion se pondra en contacto para dar seguimiento.</p>
              <p style="color:#8b95a1;font-size:12px">Este es un mensaje automatico del sistema.</p>
            </div>
        """.formatted(empresa, tipo, motivoTexto);
    }

}
