package com.example.NoMasAccidentes.repository.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Accidente;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccidenteRepository extends JpaRepository<Accidente, Long> {

    List<Accidente> findByAsesoriaIdOrderByFechaOcurrenciaDesc(Long idAsesoria);

    /** Accidentes de la empresa (vía asesoría) ocurridos en el periodo (reporte mensual, RF39). */
    long countByAsesoriaEmpresaIdAndFechaOcurrenciaBetween(
            Long idEmpresa, LocalDate desde, LocalDate hasta);

    /** Días perdidos por accidentes de la empresa en el periodo (indicador accidentabilidad, RF40). */
    @Query("""
            select coalesce(sum(a.diasPerdidos), 0)
            from Accidente a
            where a.asesoria.empresa.id = :idEmpresa
                and a.fechaOcurrencia between :desde and :hasta
            """)
    long sumDiasPerdidosByEmpresaAndFechaOcurrenciaBetween(Long idEmpresa, LocalDate desde, LocalDate hasta);
}
