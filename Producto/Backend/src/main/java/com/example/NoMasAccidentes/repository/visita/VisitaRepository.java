package com.example.NoMasAccidentes.repository.visita;

import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.model.visita.Visita;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    Page<Visita> findByEmpresaId(Long idEmpresa, Pageable pageable);

    /** Visitas de una empresa para el portal cliente (RF14). */
    java.util.List<Visita> findByEmpresaIdOrderByFechaProgramadaDesc(Long idEmpresa);

    /** Visitas asignadas al profesional autenticado (dashboard profesional). */
    java.util.List<Visita> findByProfesionalUsuarioEmailOrderByFechaProgramadaAsc(String email);

    Page<Visita> findByProfesionalId(Long idProfesional, Pageable pageable);

    /** Apoya RF13 (mínimo 2 visitas por mes por empresa). */
    long countByEmpresaIdAndFechaProgramadaBetween(Long idEmpresa, LocalDate desde, LocalDate hasta);

    boolean existsByEmpresaIdAndFechaProgramadaAndEstado(Long idEmpresa, LocalDate fecha, EstadoVisita estado);

    /** Visitas programadas para mañana sin recordatorio enviado (RF29). */
    java.util.List<Visita> findByEstadoAndRecordatorioEnviadoFalseAndFechaProgramada(
            EstadoVisita estado, LocalDate fechaProgramada);

    /** Visitas realizadas por la empresa en el periodo (reporte mensual, RF39). */
    long countByEmpresaIdAndEstadoAndFechaFinBetween(
            Long idEmpresa, EstadoVisita estado, LocalDateTime desde, LocalDateTime hasta);

    /** Visitas realizadas por un profesional en el periodo (rendimiento, RF41). */
    long countByProfesionalIdAndEstadoAndFechaFinBetween(
            Long idProfesional, EstadoVisita estado, LocalDateTime desde, LocalDateTime hasta);

    /** Visitas programadas a un profesional en el periodo (base de % de cumplimiento, RF41). */
    long countByProfesionalIdAndFechaProgramadaBetween(
            Long idProfesional, LocalDate desde, LocalDate hasta);

    /** KPI dashboard: visitas en un estado programadas dentro del rango (semana en curso). */
    long countByEstadoAndFechaProgramadaBetween(EstadoVisita estado, LocalDate desde, LocalDate hasta);

    /** Dashboard: últimas visitas registradas, de la más reciente a la más antigua. */
    java.util.List<Visita> findTop6ByOrderByFechaProgramadaDesc();

    /** Alertas dashboard: visitas aún en un estado pese a que su fecha ya pasó (incumplidas). */
    java.util.List<Visita> findByEstadoAndFechaProgramadaLessThan(EstadoVisita estado, LocalDate fecha);
}
