package com.example.NoMasAccidentes.repository.cliente;

import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.cliente.EstadoCliente;
import java.util.Collection;
import java.util.List;
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

    /** KPI dashboard: clientes en un estado (ej. ACTIVO). */
    long countByEstado(EstadoCliente estado);

    /** KPI dashboard: clientes morosos/suspendidos. */
    long countByEstadoIn(Collection<EstadoCliente> estados);

    /** Alertas dashboard: clientes morosos/suspendidos con sus datos. */
    List<Cliente> findByEstadoIn(Collection<EstadoCliente> estados);

    /** Dashboard profesional: clientes asignados al profesional autenticado. */
    List<Cliente> findByProfesionalUsuarioEmailOrderByRazonSocialAsc(String email);
}
