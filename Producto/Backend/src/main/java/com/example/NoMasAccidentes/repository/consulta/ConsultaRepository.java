package com.example.NoMasAccidentes.repository.consulta;

import com.example.NoMasAccidentes.model.consulta.Consulta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByClienteIdOrderByFechaHoraDesc(Long idCLiente);
}
