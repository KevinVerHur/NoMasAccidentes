package com.example.NoMasAccidentes.repository.consulta;

import com.example.NoMasAccidentes.model.consulta.Consulta;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByClienteIdOrderByFechaHoraDesc(Long idCLiente);

    /** Consultas al centro de llamados del cliente en el periodo (reporte mensual, RF39). */
    long countByClienteIdAndFechaHoraBetween(Long idCliente, LocalDateTime desde, LocalDateTime hasta);
}
