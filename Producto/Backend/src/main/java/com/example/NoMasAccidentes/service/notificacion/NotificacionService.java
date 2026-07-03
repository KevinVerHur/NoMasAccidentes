package com.example.NoMasAccidentes.service.notificacion;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.notificacion.NotificacionMapper;
import com.example.NoMasAccidentes.dto.notificacion.NotificacionResponse;
import com.example.NoMasAccidentes.model.notificacion.Notificacion;
import com.example.NoMasAccidentes.model.notificacion.TipoNotificacion;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.repository.notificacion.NotificacionRepository;
import com.example.NoMasAccidentes.repository.usuario.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bandeja de notificaciones in-app (Fase 4).
 * Escritura ({@link #crear}) usada por el orquestador de eventos; lectura y
 * marcado de leídas usados por el portal del usuario autenticado.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionMapper mapper;

    /** Crea y persiste una notificación para un usuario. Llamado dentro de la transacción del evento. */
    @Transactional
    public Notificacion crear(Usuario destinatario, TipoNotificacion tipo,
                              String titulo, String mensaje, String enlace) {
        Notificacion notificacion = Notificacion.builder()
                .usuario(destinatario)
                .tipo(tipo)
                .titulo(titulo)
                .mensaje(mensaje)
                .enlace(enlace)
                .leida(false)
                .build();
        Notificacion guardada = notificacionRepository.save(notificacion);
        log.info("Notificación creada id={} tipo={} usuario={}", guardada.getId(), tipo, destinatario.getEmail());
        return guardada;
    }

    /** Bandeja del usuario autenticado (más recientes primero). */
    public List<NotificacionResponse> misNotificaciones(String emailUsuario) {
        Usuario usuario = buscarUsuario(emailUsuario);
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream().map(mapper::toResponse).toList();
    }

    /** Cantidad de no leídas del usuario autenticado (badge del sidebar). */
    public long contarNoLeidas(String emailUsuario) {
        Usuario usuario = buscarUsuario(emailUsuario);
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId());
    }

    /** Marca una notificación como leída, verificando que pertenezca al usuario. */
    @Transactional
    public NotificacionResponse marcarLeida(Long id, String emailUsuario) {
        Usuario usuario = buscarUsuario(emailUsuario);
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación", id));
        if (!notificacion.getUsuario().getId().equals(usuario.getId())) {
            // No revelar existencia de notificaciones ajenas.
            throw new RecursoNoEncontradoException("Notificación", id);
        }
        if (!notificacion.isLeida()) {
            notificacion.setLeida(true);
            notificacion.setFechaLeida(LocalDateTime.now());
        }
        return mapper.toResponse(notificacion);
    }

    /** Marca todas las notificaciones no leídas del usuario como leídas. */
    @Transactional
    public void marcarTodasLeidas(String emailUsuario) {
        Usuario usuario = buscarUsuario(emailUsuario);
        List<Notificacion> noLeidas = notificacionRepository.findByUsuarioIdAndLeidaFalse(usuario.getId());
        LocalDateTime ahora = LocalDateTime.now();
        noLeidas.forEach(n -> {
            n.setLeida(true);
            n.setFechaLeida(ahora);
        });
        log.info("Notificaciones marcadas como leídas para {}: {}", emailUsuario, noLeidas.size());
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", email));
    }
}
