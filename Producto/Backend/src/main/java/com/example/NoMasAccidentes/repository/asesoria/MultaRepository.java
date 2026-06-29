package com.example.NoMasAccidentes.repository.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Multa;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MultaRepository extends JpaRepository<Multa, Long> {

    List<Multa> findByFiscalizacionIdOrderByFechaEmisionDesc(Long idFiscalizacion);

    /** Multas del cliente (vía fiscalización → asesoría) emitidas en el periodo (reporte mensual, RF39). */
    long countByFiscalizacionAsesoriaClienteIdAndFechaEmisionBetween(
            Long idCliente, LocalDate desde, LocalDate hasta);
}
