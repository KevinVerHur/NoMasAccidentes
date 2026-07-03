package com.example.NoMasAccidentes.repository.consulta;

import com.example.NoMasAccidentes.model.consulta.Consulta;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByEmpresaIdOrderByFechaHoraDesc(Long idEmpresa);

    /** Consultas al centro de llamados de la empresa en el periodo (reporte mensual, RF39). */
    long countByEmpresaIdAndFechaHoraBetween(Long idEmpresa, LocalDateTime desde, LocalDateTime hasta);
}
