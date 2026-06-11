package com.example.NoMasAccidentes.repository.actividad;

import com.example.NoMasAccidentes.model.actividad.ActividadPreventiva;
import com.example.NoMasAccidentes.model.actividad.EstadoActividadPreventiva;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ActividadPreventivaRepository extends JpaRepository<ActividadPreventiva, Long> {

    Page<ActividadPreventiva> findByClienteId(Long idCliente, Pageable pageable);

    Page<ActividadPreventiva> findByEstado(EstadoActividadPreventiva estado, Pageable pageable);

    Page<ActividadPreventiva> findByClienteIdAndEstado(
            Long idCliente,
            EstadoActividadPreventiva estado,
            Pageable pageable
    );

    List<ActividadPreventiva> findByClienteUsuarioEmailOrderByFechaCompromisoAsc(String email);

    long countByEstado(EstadoActividadPreventiva estado);

    @Modifying
    @Query("""
            update ActividadPreventiva a
            set a.estado = com.example.NoMasAccidentes.model.actividad.EstadoActividadPreventiva.VENCIDA
            where a.estado <> com.example.NoMasAccidentes.model.actividad.EstadoActividadPreventiva.CUMPLIDA
                and a.estado <> com.example.NoMasAccidentes.model.actividad.EstadoActividadPreventiva.VENCIDA
                and a.fechaCompromiso < :hoy
            """)
    int marcarVencidas(LocalDate hoy);
}