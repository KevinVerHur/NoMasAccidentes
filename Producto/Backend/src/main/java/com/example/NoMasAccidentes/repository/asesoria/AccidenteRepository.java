package com.example.NoMasAccidentes.repository.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Accidente;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccidenteRepository extends JpaRepository<Accidente, Long> {

    List<Accidente> findByAsesoriaIdOrderByFechaOcurrenciaDesc(Long idAsesoria);

    /** Accidentes del cliente (vía asesoría) ocurridos en el periodo (reporte mensual, RF39). */
    long countByAsesoriaClienteIdAndFechaOcurrenciaBetween(
            Long idCliente, LocalDate desde, LocalDate hasta);

    /** Días perdidos por accidentes del cliente en el periodo (indicador accidentabilidad, RF40). */
    @Query("""
            select coalesce(sum(a.diasPerdidos), 0)
            from Accidente a
            where a.asesoria.cliente.id = :idCliente
                and a.fechaOcurrencia between :desde and :hasta
            """)
    long sumDiasPerdidosByClienteAndFechaOcurrenciaBetween(Long idCliente, LocalDate desde, LocalDate hasta);
}
