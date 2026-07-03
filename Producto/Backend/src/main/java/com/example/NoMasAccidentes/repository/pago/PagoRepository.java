package com.example.NoMasAccidentes.repository.pago;

import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.pago.Pago;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    /** Historial de pagos de una empresa (RF10), a través de sus planes. */
    List<Pago> findByPlanEmpresaIdOrderByFechaVencimientoDesc(Long idEmpresa);

    List<Pago> findByPlanEmpresaIdAndEstadoPago(Long idEmpresa, EstadoPago estadoPago);

    /** Cuotas vencidas e impagas (RF11). */
    List<Pago> findByEstadoPagoAndFechaVencimientoBefore(EstadoPago estadoPago, LocalDate fecha);

    List<Pago> findByEstadoPago(EstadoPago estadoPago);

    /** Cuotas pendientes/atrasadas sin alerta enviada hasta hoy (RF31). */
    List<Pago> findByEstadoPagoInAndAlertaEnviadaFalseAndFechaVencimientoLessThanEqual(
            List<EstadoPago> estados, LocalDate fecha);
}
