package com.example.NoMasAccidentes.repository.pago;

import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.pago.Pago;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    /** Historial de pagos de una empresa (RF10), a través de sus planes. */
    List<Pago> findByPlanEmpresaIdOrderByFechaVencimientoDesc(Long idEmpresa);

    List<Pago> findByPlanEmpresaIdAndEstadoPago(Long idEmpresa, EstadoPago estadoPago);

    /** Cuotas vencidas e impagas de una empresa (RF11), para cobranza por cliente. */
    List<Pago> findByPlanEmpresaIdAndEstadoPagoAndFechaVencimientoBefore(
            Long idEmpresa, EstadoPago estadoPago, LocalDate fecha);

    /** Cuotas vencidas e impagas (RF11). */
    List<Pago> findByEstadoPagoAndFechaVencimientoBefore(EstadoPago estadoPago, LocalDate fecha);

    List<Pago> findByEstadoPago(EstadoPago estadoPago);

    /** Total de cuotas en un estado (RF11), para informar el saldo de morosidad. */
    long countByEstadoPago(EstadoPago estadoPago);

    /** Cuota asociada a una transacción Webpay, para correlacionar el retorno (RF09). */
    Optional<Pago> findByWebpayToken(String webpayToken);

    /** Cuotas pendientes/atrasadas sin alerta enviada hasta hoy (RF31). */
    List<Pago> findByEstadoPagoInAndAlertaEnviadaFalseAndFechaVencimientoLessThanEqual(
            List<EstadoPago> estados, LocalDate fecha);
         
    Optional<Pago> findFirstByPlanIdAndEstadoPagoInOrderByFechaVencimientoAsc(
    Long idPlan,
    List<EstadoPago> estados
    );
    boolean existsByPlanIdAndFechaEmision(Long idPlan, LocalDate fechaEmision);

    int countByPlanId(Long idPlan);  
    

}
