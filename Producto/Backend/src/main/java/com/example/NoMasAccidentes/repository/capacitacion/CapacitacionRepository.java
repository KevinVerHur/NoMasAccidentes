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

    List<Capacitacion> findByClienteId(Long idCliente);

    List<Capacitacion> findByClienteIdAndEstado(Long idCliente, EstadoCapacitacion estado);

    /** Capacitaciones extra del cliente (generan costo adicional, RF-CAP4). */
    List<Capacitacion> findByClienteIdAndEsCapacitacionExtraTrue(Long idCliente);

    /** Capacitaciones que dicta un relator específico. */
    List<Capacitacion> findByRelatorId(Long idRelator);

    /**
     * RF30: capacitaciones PROGRAMADAS para la fecha dada (hoy+3) a las que aún
     * no se les envió el recordatorio. El cliente se trae con join fetch para
     * leer su email/razón social fuera de la sesión Hibernate.
     */
    @Query("""
            select c from Capacitacion c
            join fetch c.cliente
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
            join fetch c.cliente
            where c.estado = com.example.NoMasAccidentes.model.capacitacion.EstadoCapacitacion.PROGRAMADA
                and c.incumplimientoNotificado = false
                and c.fechaProgramada < :hoy
            """)
    List<Capacitacion> findIncumplidasSinNotificar(LocalDate hoy);
}
