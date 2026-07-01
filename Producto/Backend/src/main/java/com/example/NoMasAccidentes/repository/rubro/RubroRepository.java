package com.example.NoMasAccidentes.repository.rubro;

import com.example.NoMasAccidentes.model.rubro.Rubro;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RubroRepository extends JpaRepository<Rubro, Long> {

    Optional<Rubro> findByNombre(String nombre);
}
