package com.example.NoMasAccidentes.repository.cliente;

import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.cliente.EstadoCliente;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByRut(String rut);

    /** Resuelve el cliente a partir de su cuenta de usuario (portal cliente). */
    Optional<Cliente> findByUsuarioEmail(String email);

    Page<Cliente> findByEstado(EstadoCliente estado, Pageable pageable);

    long countByProfesionalId(Long idProfesional);
}
