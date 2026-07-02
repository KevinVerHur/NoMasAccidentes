package com.example.NoMasAccidentes.repository.reporte;

import com.example.NoMasAccidentes.model.reporte.ReporteMensual;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteMensualRepository extends JpaRepository<ReporteMensual, Long> {

    /** Reporte de un periodo concreto (clave natural empresa/mes/año). Apoya el upsert idempotente. */
    Optional<ReporteMensual> findByEmpresaIdAndMesAndAnio(Long idEmpresa, int mes, int anio);

    /** Reportes históricos de una empresa (RF42). */
    List<ReporteMensual> findByEmpresaIdOrderByAnioDescMesDesc(Long idEmpresa);

    /** Listado paginado para el panel admin/profesional (RF38, RF42). */
    Page<ReporteMensual> findByEmpresaId(Long idEmpresa, Pageable pageable);
}
