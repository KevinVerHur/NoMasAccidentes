package com.example.NoMasAccidentes.repository.visita;

import com.example.NoMasAccidentes.model.visita.ListaChequeo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaChequeoRepository extends JpaRepository<ListaChequeo, Long> {

    Optional<ListaChequeo> findByClienteId(Long idCliente);

    boolean existsByClienteId(Long idCliente);
}
