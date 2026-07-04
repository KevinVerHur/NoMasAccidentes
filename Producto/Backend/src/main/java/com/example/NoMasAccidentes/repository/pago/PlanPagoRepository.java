package com.example.NoMasAccidentes.repository.pago;

import com.example.NoMasAccidentes.model.pago.PlanPago;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanPagoRepository extends JpaRepository<PlanPago, Long> {

    List<PlanPago> findByEmpresaId(Long idEmpresa);
    Optional<PlanPago> findFirstByEmpresa_IdAndFechaInicioLessThanEqualOrderByFechaInicioDesc(
    Long idEmpresa,
    LocalDate fecha);
    List<PlanPago> findByActivoTrue();
    List<PlanPago> findByEmpresaIdAndActivoTrue(Long idEmpresa);
}
