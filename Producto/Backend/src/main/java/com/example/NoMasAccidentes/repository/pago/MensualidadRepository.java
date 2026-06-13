package com.example.NoMasAccidentes.repository.pago;

import com.example.NoMasAccidentes.model.pago.Mensualidad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensualidadRepository extends JpaRepository<Mensualidad, Long> {

    Optional<Mensualidad> findByNombrePlan(String nombrePlan);

    boolean existsByNombrePlan(String nombrePlan);
}
