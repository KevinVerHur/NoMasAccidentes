package com.example.NoMasAccidentes.repository.informe;

import com.example.NoMasAccidentes.model.informe.Informe;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformeRepository extends JpaRepository<Informe, Long> {

    Optional<Informe> findByVisitaId(Long idVisita);

    /** Informes de las visitas de un cliente (portal cliente, RF15). */
    List<Informe> findByVisitaClienteIdOrderByFechaEmisionDesc(Long idCliente);
}
