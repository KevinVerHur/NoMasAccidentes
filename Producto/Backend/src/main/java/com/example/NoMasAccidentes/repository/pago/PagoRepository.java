package com.example.NoMasAccidentes.repository.pago;

import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.pago.Pago;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByEstadoInAndAlertaEnviadaFalseAndFechaVencimientoLessThanEqual(
        Collection<EstadoPago> estados,
        LocalDate fechaLimite
    );

    List<Pago> findByEstadoAndFechaVencimientoBefore(
        EstadoPago estado,
        LocalDate fechaActual
    );
}