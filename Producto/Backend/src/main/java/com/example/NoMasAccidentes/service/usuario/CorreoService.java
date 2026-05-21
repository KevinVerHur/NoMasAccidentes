package com.example.NoMasAccidentes.service.usuario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
}