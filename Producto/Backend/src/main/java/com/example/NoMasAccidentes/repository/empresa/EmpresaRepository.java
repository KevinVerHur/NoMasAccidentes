package com.example.NoMasAccidentes.repository.empresa;

import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.empresa.EstadoEmpresa;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByRut(String rut);

    Page<Empresa> findByEstado(EstadoEmpresa estado, Pageable pageable);

    long countByProfesionalId(Long idProfesional);

    /** KPI dashboard: empresas en un estado (ej. ACTIVO). */
    long countByEstado(EstadoEmpresa estado);

    /** KPI dashboard: empresas morosas/suspendidas. */
    long countByEstadoIn(Collection<EstadoEmpresa> estados);

    /** Alertas dashboard: empresas morosas/suspendidas con sus datos. */
    List<Empresa> findByEstadoIn(Collection<EstadoEmpresa> estados);

    /** Dashboard profesional: empresas asignadas al profesional autenticado. */
    List<Empresa> findByProfesionalUsuarioEmailOrderByRazonSocialAsc(String email);
}
