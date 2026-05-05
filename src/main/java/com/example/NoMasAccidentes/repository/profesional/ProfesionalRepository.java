package com.example.NoMasAccidentes.repository.profesional;

import com.example.NoMasAccidentes.model.profesional.Profesional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {

    Optional<Profesional> findByRut(String rut);

    Optional<Profesional> findByUsuarioId(Long idUsuario);


}
