package com.example.NoMasAccidentes.repository.notificacion;

import com.example.NoMasAccidentes.model.notificacion.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /** Bandeja del usuario: más recientes primero. */
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long idUsuario);

    /** Cantidad de notificaciones sin leer del usuario (badge del sidebar). */
    long countByUsuarioIdAndLeidaFalse(Long idUsuario);

    /** No leídas del usuario (para marcarlas todas como leídas). */
    List<Notificacion> findByUsuarioIdAndLeidaFalse(Long idUsuario);
}
