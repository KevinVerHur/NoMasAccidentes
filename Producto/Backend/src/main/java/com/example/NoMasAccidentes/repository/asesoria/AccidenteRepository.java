package com.example.NoMasAccidentes.repository.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Accidente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccidenteRepository extends JpaRepository<Accidente, Long> {

    List<Accidente> findByAsesoriaIdOrderByFechaOcurrenciaDesc(Long idAsesoria);
}
