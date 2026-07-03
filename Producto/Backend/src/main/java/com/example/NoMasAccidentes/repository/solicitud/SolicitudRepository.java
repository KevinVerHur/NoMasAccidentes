package com.example.NoMasAccidentes.repository.solicitud;

import com.example.NoMasAccidentes.model.solicitud.EstadoSolicitud;
import com.example.NoMasAccidentes.model.solicitud.Solicitud;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    /** Solicitudes de una empresa (tracking del cliente), más recientes primero. */
    List<Solicitud> findByEmpresaIdOrderByFechaCreacionDesc(Long idEmpresa);

    /** Todas las solicitudes (bandeja del admin), más recientes primero. */
    List<Solicitud> findAllByOrderByFechaCreacionDesc();

    /** Solicitudes filtradas por estado (bandeja del admin). */
    List<Solicitud> findByEstadoOrderByFechaCreacionDesc(EstadoSolicitud estado);

    /** Cantidad de solicitudes en un estado (badge de pendientes del admin). */
    long countByEstado(EstadoSolicitud estado);
}
