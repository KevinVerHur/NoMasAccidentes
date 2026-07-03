package com.example.NoMasAccidentes.repository.configuracion;

import com.example.NoMasAccidentes.model.configuracion.ConfiguracionEmpresa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionEmpresaRepository extends JpaRepository<ConfiguracionEmpresa, Long> {
    Optional<ConfiguracionEmpresa> findFirstByActivoTrueOrderByIdAsc();
}