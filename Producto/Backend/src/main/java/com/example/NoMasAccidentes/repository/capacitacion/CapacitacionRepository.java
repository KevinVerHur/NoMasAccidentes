package com.example.NoMasAccidentes.repository.capacitacion;

import com.example.NoMasAccidentes.model.capacitacion.Capacitacion;
import com.example.NoMasAccidentes.model.capacitacion.EstadoCapacitacion;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CapacitacionRepository extends JpaRepository<Capacitacion, Long> {

    Page<Capacitacion> findAll(Pageable pageable);

    List<Capacitacion> findByEmpresaId(Long idEmpresa);

    List<Capacitacion> findByEmpresaIdAndEstado(Long idEmpresa, EstadoCapacitacion estado);

    /** Capacitaciones extra de la empresa (generan costo adicional, RF-CAP4). */
    List<Capacitacion> findByEmpresaIdAndEsCapacitacionExtraTrue(Long idEmpresa);

    /** Capacitaciones que dicta un relator específico. */
    List<Capacitacion> findByRelatorId(Long idRelator);

    /** Capacitaciones realizadas por la empresa en el periodo (reporte mensual, RF39). */
    long countByEmpresaIdAndEstadoAndFechaRealizacionBetween(
            Long idEmpresa, EstadoCapacitacion estado, LocalDate desde, LocalDate hasta);

    /** Capacitaciones dictadas por un relator en el periodo (rendimiento, RF41). */
    long countByRelatorIdAndEstadoAndFechaRealizacionBetween(
            Long idRelator, EstadoCapacitacion estado, LocalDate desde, LocalDate hasta);

    /** KPI dashboard: capacitaciones programadas dentro del mes en curso. */
    long countByFechaProgramadaBetween(LocalDate desde, LocalDate hasta);

    /** Alertas dashboard: capacitaciones aún en un estado pese a que su fecha ya pasó. */
    List<Capacitacion> findByEstadoAndFechaProgramadaLessThan(EstadoCapacitacion estado, LocalDate fecha);

    /**
     * RF30: capacitaciones PROGRAMADAS para la fecha dada (hoy+3) a las que aún
     * no se les envió el recordatorio. La empresa se trae con join fetch para
     * leer su email/razón social fuera de la sesión Hibernate.
     */
    @Query("""
            select c from Capacitacion c
            join fetch c.empresa
            where c.estado = com.example.NoMasAccidentes.model.capacitacion.EstadoCapacitacion.PROGRAMADA
                and c.recordatorioEnviado = false
                and c.fechaProgramada = :fecha
            """)
    List<Capacitacion> findParaRecordatorio(LocalDate fecha);

    /**
     * RF32: capacitaciones que siguen PROGRAMADAS pese a que su fecha ya pasó
     * (no se realizaron) y aún no se notificó el incumplimiento al admin.
     */
    @Query("""
            select c from Capacitacion c
            join fetch c.empresa
            where c.estado = com.example.NoMasAccidentes.model.capacitacion.EstadoCapacitacion.PROGRAMADA
                and c.incumplimientoNotificado = false
                and c.fechaProgramada < :hoy
            """)
    List<Capacitacion> findIncumplidasSinNotificar(LocalDate hoy);
}
