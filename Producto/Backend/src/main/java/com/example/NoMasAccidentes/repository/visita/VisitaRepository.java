package com.example.NoMasAccidentes.repository.visita;

import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.model.visita.Visita;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    Page<Visita> findByClienteId(Long idCliente, Pageable pageable);

    /** Visitas de un cliente para el portal cliente (RF14). */
    java.util.List<Visita> findByClienteIdOrderByFechaProgramadaDesc(Long idCliente);

    /** Visitas asignadas al profesional autenticado (dashboard profesional). */
    java.util.List<Visita> findByProfesionalUsuarioEmailOrderByFechaProgramadaAsc(String email);

    Page<Visita> findByProfesionalId(Long idProfesional, Pageable pageable);

    /** Apoya RF13 (mínimo 2 visitas por mes por cliente). */
    long countByClienteIdAndFechaProgramadaBetween(Long idCliente, LocalDate desde, LocalDate hasta);

    boolean existsByClienteIdAndFechaProgramadaAndEstado(Long idCliente, LocalDate fecha, EstadoVisita estado);

    /** Visitas programadas para mañana sin recordatorio enviado (RF29). */
    java.util.List<Visita> findByEstadoAndRecordatorioEnviadoFalseAndFechaProgramada(
            EstadoVisita estado, LocalDate fechaProgramada);

    /** Visitas realizadas por el cliente en el periodo (reporte mensual, RF39). */
    long countByClienteIdAndEstadoAndFechaFinBetween(
            Long idCliente, EstadoVisita estado, LocalDateTime desde, LocalDateTime hasta);

    /** Visitas realizadas por un profesional en el periodo (rendimiento, RF41). */
    long countByProfesionalIdAndEstadoAndFechaFinBetween(
            Long idProfesional, EstadoVisita estado, LocalDateTime desde, LocalDateTime hasta);

    /** Visitas programadas a un profesional en el periodo (base de % de cumplimiento, RF41). */
    long countByProfesionalIdAndFechaProgramadaBetween(
            Long idProfesional, LocalDate desde, LocalDate hasta);
}
