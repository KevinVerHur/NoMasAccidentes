package com.example.NoMasAccidentes.service.notificacion;

import com.example.NoMasAccidentes.model.actividad.ActividadPreventiva;
import com.example.NoMasAccidentes.model.asesoria.Asesoria;
import com.example.NoMasAccidentes.model.capacitacion.Capacitacion;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.notificacion.TipoNotificacion;
import com.example.NoMasAccidentes.model.pago.Pago;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.solicitud.EstadoSolicitud;
import com.example.NoMasAccidentes.model.solicitud.Solicitud;
import com.example.NoMasAccidentes.model.solicitud.TipoSolicitud;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.model.visita.Visita;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.usuario.UsuarioRepository;
import com.example.NoMasAccidentes.service.usuario.CorreoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestador de notificaciones por evento (Fase 1).
 * Por cada evento de negocio escribe la notificación in-app (bandeja) <b>y</b>
 * dispara el correo transaccional, para que ambos canales queden siempre en sync.
 * Se invoca dentro de la transacción del servicio de negocio; el correo es @Async
 * (fire-and-forget) y la escritura in-app participa en la misma transacción.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificacionEventoService {

    private final NotificacionService notificacionService;
    private final CorreoService correoService;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    /** Visita planificada → avisa al profesional asignado (RF13). */
    public void notificarVisitaPlanificada(Visita visita) {
        Profesional profesional = visita.getProfesional();
        Empresa empresa = visita.getEmpresa();
        Usuario destinatario = profesional.getUsuario();
        if (destinatario == null) {
            log.warn("Visita id={} sin usuario de profesional; no se notifica", visita.getId());
            return;
        }
        String fecha = String.valueOf(visita.getFechaProgramada());
        String titulo = "Nueva visita planificada";
        String mensaje = "Se te asignó una visita a %s para el %s.".formatted(empresa.getRazonSocial(), fecha);

        notificacionService.crear(destinatario, TipoNotificacion.VISITA_PLANIFICADA, titulo, mensaje, "/visitas");
        correoService.enviarAvisoVisitaPlanificada(
                destinatario.getEmail(), empresa.getRazonSocial(), fecha, empresa.getDireccion());
    }

    /**
     * Capacitación programada → avisa a los representantes con acceso de la empresa (RF-CAP1)
     * y al profesional (relator) asignado a dictarla.
     */
    public void notificarCapacitacionProgramada(Capacitacion capacitacion) {
        Empresa empresa = capacitacion.getEmpresa();
        String fecha = String.valueOf(capacitacion.getFechaProgramada());
        String hora = String.valueOf(capacitacion.getHoraProgramada());
        String titulo = "Nueva capacitación programada";
        String mensaje = "Se programó la capacitación \"%s\" para el %s a las %s."
                .formatted(capacitacion.getCurso(), fecha, hora);

        for (Cliente representante : representantesConAcceso(empresa)) {
            notificacionService.crear(representante.getUsuario(),
                    TipoNotificacion.CAPACITACION_PROGRAMADA, titulo, mensaje, "/mis-capacitaciones");
            correoService.enviarAvisoCapacitacionProgramada(
                    representante.getEmail(), empresa.getRazonSocial(),
                    capacitacion.getCurso(), fecha, hora, capacitacion.getLugar());
        }

        // Profesional que dicta (relator): avisa que fue asignado (bandeja + correo).
        Profesional relator = capacitacion.getRelator();
        if (relator != null && relator.getUsuario() != null) {
            String tituloProf = "Capacitación asignada";
            String mensajeProf = "Se te asignó como relator de \"%s\" en %s, el %s a las %s."
                    .formatted(capacitacion.getCurso(), empresa.getRazonSocial(), fecha, hora);
            notificacionService.crear(relator.getUsuario(),
                    TipoNotificacion.CAPACITACION_PROGRAMADA, tituloProf, mensajeProf, "/capacitaciones");
            correoService.enviarAvisoCapacitacionAsignada(
                    relator.getUsuario().getEmail(), empresa.getRazonSocial(),
                    capacitacion.getCurso(), fecha, hora, capacitacion.getLugar());
        }
    }

    /**
     * Asesoría registrada → avisa a los representantes con acceso de la empresa (RF22)
     * y al profesional asignado a atenderla.
     */
    public void notificarAsesoriaRegistrada(Asesoria asesoria) {
        Empresa empresa = asesoria.getEmpresa();
        String tipo = String.valueOf(asesoria.getTipo());
        String titulo = "Asesoría registrada";
        String mensaje = "Se registró una asesoría (%s) a nombre de tu empresa.".formatted(tipo);

        for (Cliente representante : representantesConAcceso(empresa)) {
            notificacionService.crear(representante.getUsuario(),
                    TipoNotificacion.ASESORIA_REGISTRADA, titulo, mensaje, "/mis-actividades");
            correoService.enviarAvisoAsesoriaRegistrada(
                    representante.getEmail(), empresa.getRazonSocial(), tipo, asesoria.getMotivo());
        }

        // Profesional asignado a atender la asesoría (bandeja + correo).
        Profesional profesional = asesoria.getProfesional();
        if (profesional != null && profesional.getUsuario() != null) {
            String tituloProf = "Asesoría asignada";
            String mensajeProf = "Se te asignó una asesoría (%s) para %s."
                    .formatted(tipo, empresa.getRazonSocial());
            notificacionService.crear(profesional.getUsuario(),
                    TipoNotificacion.ASESORIA_REGISTRADA, tituloProf, mensajeProf, "/asesorias");
            correoService.enviarAvisoAsesoriaAsignada(
                    profesional.getUsuario().getEmail(), empresa.getRazonSocial(), tipo, asesoria.getMotivo());
        }
    }

    /** Solicitud creada por el cliente → avisa a los administradores (bandeja + correo). */
    public void notificarSolicitudRecibida(Solicitud solicitud) {
        Empresa empresa = solicitud.getEmpresa();
        String tipo = String.valueOf(solicitud.getTipo());
        boolean urgente = solicitud.getTipo() == TipoSolicitud.ACCIDENTE;

        String titulo = urgente
                ? "🚨 Accidente reportado — atención urgente"
                : "Nueva solicitud de " + tipo.toLowerCase();
        String mensaje = urgente
                ? "%s reportó un ACCIDENTE y requiere atención inmediata.".formatted(empresa.getRazonSocial())
                : "%s solicitó %s.".formatted(empresa.getRazonSocial(), tipo.toLowerCase());

        for (Usuario admin : usuarioRepository.findByRolNombre("ADMIN")) {
            notificacionService.crear(admin, TipoNotificacion.SOLICITUD_RECIBIDA, titulo, mensaje, "/solicitudes");
        }
        correoService.enviarAvisoSolicitudRecibida(empresa.getRazonSocial(), tipo, solicitud.getDescripcion(), urgente);
    }

    /** Solicitud respondida por el admin → avisa al cliente (bandeja + correo). */
    public void notificarSolicitudRespondida(Solicitud solicitud) {
        Empresa empresa = solicitud.getEmpresa();
        String tipo = String.valueOf(solicitud.getTipo());
        boolean aprobada = solicitud.getEstado() == EstadoSolicitud.APROBADA;
        String estado = aprobada ? "aprobada" : "rechazada";
        String titulo = "Solicitud " + estado;
        String mensaje = "Tu solicitud de %s fue %s.".formatted(tipo.toLowerCase(), estado)
                + (aprobada && solicitud.isEsExtra() ? " Se cobrará como servicio adicional." : "");

        for (Cliente representante : representantesConAcceso(empresa)) {
            notificacionService.crear(representante.getUsuario(),
                    TipoNotificacion.SOLICITUD_RESPONDIDA, titulo, mensaje, "/mis-solicitudes");
            correoService.enviarAvisoSolicitudRespondida(
                    representante.getEmail(), tipo, aprobada, solicitud.isEsExtra(), solicitud.getRespuestaAdmin());
        }
    }

    /** Actividad preventiva vencida → avisa al cliente (bandeja + correo) (RF49). */
    public void notificarActividadVencidaAlCliente(ActividadPreventiva actividad) {
        Empresa empresa = actividad.getEmpresa();
        String titulo = "Actividad preventiva vencida";
        String mensaje = "La actividad \"%s\" venció el %s sin registrarse como cumplida."
                .formatted(actividad.getTitulo(), actividad.getFechaCompromiso());

        for (Cliente representante : representantesConAcceso(empresa)) {
            notificacionService.crear(representante.getUsuario(),
                    TipoNotificacion.ACTIVIDAD_VENCIDA, titulo, mensaje, "/mi-cumplimiento");
            correoService.enviarAvisoActividadVencidaCliente(
                    representante.getEmail(), actividad.getTitulo(),
                    actividad.getNormativa(), String.valueOf(actividad.getFechaCompromiso()));
        }
    }

    /** El cliente reportó cumplimiento → avisa a los admin y al profesional asignado (bandeja + correo). */
    public void notificarCumplimientoReportado(ActividadPreventiva actividad) {
        Empresa empresa = actividad.getEmpresa();
        String titulo = "Cliente reportó cumplimiento";
        String mensaje = "%s reportó haber cumplido la actividad \"%s\". Verifica y confírmala."
                .formatted(empresa.getRazonSocial(), actividad.getTitulo());

        for (Usuario admin : usuarioRepository.findByRolNombre("ADMIN")) {
            notificacionService.crear(admin, TipoNotificacion.CUMPLIMIENTO_REPORTADO, titulo, mensaje, "/seguimiento-preventivo");
        }
        Profesional profesional = empresa.getProfesional();
        if (profesional != null && profesional.getUsuario() != null) {
            notificacionService.crear(profesional.getUsuario(),
                    TipoNotificacion.CUMPLIMIENTO_REPORTADO, titulo, mensaje, "/seguimiento-preventivo");
        }
        correoService.enviarAvisoCumplimientoReportado(
                empresa.getRazonSocial(), actividad.getTitulo(), actividad.getComentarioCliente());
    }

    /** El cliente pagó una cuota → avisa a los representantes con acceso (bandeja + correo con comprobante) (RF09). */
    public void notificarPagoRealizado(Pago pago, byte[] comprobantePdf) {
        Empresa empresa = pago.getPlan().getEmpresa();
        String monto = formatoClp(pago.getMonto());
        String fecha = String.valueOf(pago.getFechaPago());
        String titulo = "Pago realizado";
        String mensaje = "Registramos el pago de tu cuota #%d por %s."
                .formatted(pago.getNumeroCuota(), monto);

        for (Cliente representante : representantesConAcceso(empresa)) {
            notificacionService.crear(representante.getUsuario(),
                    TipoNotificacion.PAGO_REALIZADO, titulo, mensaje, "/mis-pagos");
            correoService.enviarComprobantePago(
                    representante.getEmail(), empresa.getRazonSocial(),
                    pago.getNumeroCuota(), monto, fecha, comprobantePdf);
        }

        // Aviso a la consultora: bandeja a cada admin + correo a la casilla admin.
        String tituloAdmin = "Pago recibido";
        String mensajeAdmin = "%s pagó la cuota #%d por %s."
                .formatted(empresa.getRazonSocial(), pago.getNumeroCuota(), monto);
        for (Usuario admin : usuarioRepository.findByRolNombre("ADMIN")) {
            notificacionService.crear(admin, TipoNotificacion.PAGO_REALIZADO, tituloAdmin, mensajeAdmin, "/pagos");
        }
        correoService.enviarAvisoPagoRecibido(
                empresa.getRazonSocial(), pago.getNumeroCuota(), monto, fecha);
    }

    private String formatoClp(java.math.BigDecimal monto) {
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CL"));
        nf.setMaximumFractionDigits(0);
        return nf.format(monto);
    }

    /** Representantes de la empresa que tienen cuenta de acceso al portal (usuario != null). */
    private List<Cliente> representantesConAcceso(Empresa empresa) {
        return clienteRepository.findByEmpresaId(empresa.getId()).stream()
                .filter(c -> c.getUsuario() != null)
                .toList();
    }
}
