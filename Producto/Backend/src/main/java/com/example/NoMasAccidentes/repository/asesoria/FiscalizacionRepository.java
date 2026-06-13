package com.example.NoMasAccidentes.repository.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Fiscalizacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalizacionRepository extends JpaRepository<Fiscalizacion, Long> {

    List<Fiscalizacion> findByAsesoriaIdOrderByFechaDesc(Long idAsesoria);
}
