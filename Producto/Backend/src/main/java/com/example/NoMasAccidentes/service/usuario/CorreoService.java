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

}
