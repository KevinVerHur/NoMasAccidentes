package com.example.NoMasAccidentes.repository.actividad;

import com.example.NoMasAccidentes.model.actividad.ActividadPreventiva;
import com.example.NoMasAccidentes.model.actividad.EstadoActividadPreventiva;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ActividadPreventivaRepository extends JpaRepository<ActividadPreventiva, Long> {

    Page<ActividadPreventiva> findByEmpresaId(Long idEmpresa, Pageable pageable);

    Page<ActividadPreventiva> findByEstado(EstadoActividadPreventiva estado, Pageable pageable);

    Page<ActividadPreventiva> findByEmpresaIdAndEstado(
            Long idEmpresa,
            EstadoActividadPreventiva estado,
            Pageable pageable
    );

    List<ActividadPreventiva> findByEmpresaIdOrderByFechaCompromisoAsc(Long idEmpresa);

    long countByEstado(EstadoActividadPreventiva estado);

    @Query("""
            select a from ActividadPreventiva a
            join fetch a.empresa e
            where a.alertaEnviada = false
                and a.fechaCompromiso < :hoy
                and a.estado <> com.example.NoMasAccidentes.model.actividad.EstadoActividadPreventiva.CUMPLIDA
            """)
    List<ActividadPreventiva> findVencidasSinAlerta(LocalDate hoy);

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
