package com.example.NoMasAccidentes.repository.capacitacion;

import com.example.NoMasAccidentes.model.capacitacion.Capacitacion;
import com.example.NoMasAccidentes.model.capacitacion.EstadoCapacitacion;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapacitacionRepository extends JpaRepository<Capacitacion, Long> {

    Page<Capacitacion> findAll(Pageable pageable);

    List<Capacitacion> findByClienteId(Long idCliente);

    List<Capacitacion> findByClienteIdAndEstado(Long idCliente, EstadoCapacitacion estado);

    /** Capacitaciones extra del cliente (generan costo adicional, RF-CAP4). */
    List<Capacitacion> findByClienteIdAndEsCapacitacionExtraTrue(Long idCliente);

    /** Capacitaciones que dicta un relator específico. */
    List<Capacitacion> findByRelatorId(Long idRelator);
}
