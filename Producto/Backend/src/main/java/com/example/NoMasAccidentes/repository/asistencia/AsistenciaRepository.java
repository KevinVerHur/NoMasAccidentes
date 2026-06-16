package com.example.NoMasAccidentes.repository.asistencia;

import com.example.NoMasAccidentes.model.asistencia.Asistencia;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    Optional<Asistencia> findByCapacitacionIdAndAsistenteId(Long idCapacitacion, Long idAsistente);

    boolean existsByCapacitacionIdAndAsistenteId(Long idCapacitacion, Long idAsistente);
}
