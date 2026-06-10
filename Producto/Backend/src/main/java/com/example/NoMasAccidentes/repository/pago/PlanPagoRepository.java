package com.example.NoMasAccidentes.repository.pago;

import com.example.NoMasAccidentes.model.pago.PlanPago;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanPagoRepository extends JpaRepository<PlanPago, Long> {

    List<PlanPago> findByClienteId(Long idCliente);
}
