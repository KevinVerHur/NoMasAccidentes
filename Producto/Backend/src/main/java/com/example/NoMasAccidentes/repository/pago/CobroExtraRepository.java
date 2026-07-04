package com.example.NoMasAccidentes.repository.pago;

import com.example.NoMasAccidentes.model.pago.CobroExtra;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.NoMasAccidentes.model.pago.TipoCobro;

public interface CobroExtraRepository extends JpaRepository<CobroExtra, Long> {

    List<CobroExtra> findByPagoId(Long idPago);

    /** Suma de costos extra de la empresa (vía pago → plan) generados en el periodo (reporte mensual, RF39). */
    @Query("""
            select coalesce(sum(c.monto), 0)
            from CobroExtra c
            where c.pago.plan.empresa.id = :idEmpresa
                and c.fechaGeneracion between :desde and :hasta
            """)
    BigDecimal sumMontoByEmpresaAndFechaGeneracionBetween(Long idEmpresa, LocalDate desde, LocalDate hasta);

    boolean existsByTipoCobroAndIdOrigen(TipoCobro tipoCobro, Long idOrigen);
}
