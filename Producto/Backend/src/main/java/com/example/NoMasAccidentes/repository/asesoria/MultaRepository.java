package com.example.NoMasAccidentes.repository.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Multa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MultaRepository extends JpaRepository<Multa, Long> {

    List<Multa> findByFiscalizacionIdOrderByFechaEmisionDesc(Long idFiscalizacion);
}
