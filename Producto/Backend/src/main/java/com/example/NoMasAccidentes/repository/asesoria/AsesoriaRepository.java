package com.example.NoMasAccidentes.repository.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Asesoria;
import com.example.NoMasAccidentes.model.asesoria.EstadoAsesoria;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsesoriaRepository extends JpaRepository<Asesoria, Long> {

    Page<Asesoria> findByEmpresaId(Long idEmpresa, Pageable pageable);

    List<Asesoria> findByProfesionalUsuarioEmailOrderByFechaSolicitudDesc(String email);

    /** Asesorías contabilizables de la empresa en el rango (excluye el estado dado). Apoya RF23. */
    long countByEmpresaIdAndFechaSolicitudBetweenAndEstadoNot(
            Long idEmpresa, LocalDate desde, LocalDate hasta, EstadoAsesoria estado);

    /** Asesorías atendidas (con fecha de atención) de la empresa en el periodo (reporte mensual, RF39). */
    long countByEmpresaIdAndFechaAtencionBetween(Long idEmpresa, LocalDate desde, LocalDate hasta);

    /** Asesorías atendidas por un profesional en el periodo (rendimiento, RF41). */
    long countByProfesionalIdAndFechaAtencionBetween(Long idProfesional, LocalDate desde, LocalDate hasta);
}
