package com.example.NoMasAccidentes.repository.visita;

import com.example.NoMasAccidentes.model.visita.ResultadoChequeo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultadoChequeoRepository extends JpaRepository<ResultadoChequeo, Long> {

    List<ResultadoChequeo> findByVisitaIdOrderByIdAsc(Long idVisita);

    Optional<ResultadoChequeo> findByVisitaIdAndItemId(Long idVisita, Long idItem);
}
