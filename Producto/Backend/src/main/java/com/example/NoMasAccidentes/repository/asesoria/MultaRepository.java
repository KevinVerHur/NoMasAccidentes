package com.example.NoMasAccidentes.repository.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Multa;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MultaRepository extends JpaRepository<Multa, Long> {

    List<Multa> findByFiscalizacionIdOrderByFechaEmisionDesc(Long idFiscalizacion);

    /** Multas de la empresa (vía fiscalización → asesoría) emitidas en el periodo (reporte mensual, RF39). */
    long countByFiscalizacionAsesoriaEmpresaIdAndFechaEmisionBetween(
            Long idEmpresa, LocalDate desde, LocalDate hasta);
}
