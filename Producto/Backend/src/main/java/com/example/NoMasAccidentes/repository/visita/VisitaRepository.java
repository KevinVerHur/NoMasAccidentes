package com.example.NoMasAccidentes.repository.visita;

import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.model.visita.Visita;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    List<Visita> findByEstadoAndRecordatorioEnviadoFalseAndFechaProgramadaBetween(
            EstadoVisita estado,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    List<Visita> findByProfesionalUsuarioEmailOrderByFechaProgramadaAsc(String email);

    List<Visita> findByProfesionalUsuarioEmailAndEstadoOrderByFechaProgramadaAsc(
            String email,
            EstadoVisita estado
    );
}