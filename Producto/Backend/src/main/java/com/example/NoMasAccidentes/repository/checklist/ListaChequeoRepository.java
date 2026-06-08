package com.example.NoMasAccidentes.repository.checklist;

import com.example.NoMasAccidentes.model.checklist.ListaChequeo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaChequeoRepository extends JpaRepository<ListaChequeo, Long> {

    List<ListaChequeo> findByClienteIdOrderByFechaActualizacionDesc(Long idCliente);

    long countByClienteId(Long idCliente);
}