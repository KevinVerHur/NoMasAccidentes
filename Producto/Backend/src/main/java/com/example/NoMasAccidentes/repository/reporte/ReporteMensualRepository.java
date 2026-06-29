package com.example.NoMasAccidentes.repository.reporte;

import com.example.NoMasAccidentes.model.reporte.ReporteMensual;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteMensualRepository extends JpaRepository<ReporteMensual, Long> {

    /** Reporte de un periodo concreto (clave natural cliente/mes/año). Apoya el upsert idempotente. */
    Optional<ReporteMensual> findByClienteIdAndMesAndAnio(Long idCliente, int mes, int anio);

    /** Reportes históricos de un cliente (RF42). */
    List<ReporteMensual> findByClienteIdOrderByAnioDescMesDesc(Long idCliente);

    /** Listado paginado para el panel admin/profesional (RF38, RF42). */
    Page<ReporteMensual> findByClienteId(Long idCliente, Pageable pageable);
}
