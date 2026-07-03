package com.example.NoMasAccidentes.service.configuracion;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.configuracion.ActualizarConfiguracionEmpresaRequest;
import com.example.NoMasAccidentes.dto.configuracion.ConfiguracionEmpresaResponse;
import com.example.NoMasAccidentes.model.configuracion.ConfiguracionEmpresa;
import com.example.NoMasAccidentes.repository.configuracion.ConfiguracionEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguracionEmpresaService {

    private final ConfiguracionEmpresaRepository repository;

    public ConfiguracionEmpresaResponse obtener() {
        return toResponse(buscarConfiguracion());
    }

    @Transactional
    public ConfiguracionEmpresaResponse actualizar(ActualizarConfiguracionEmpresaRequest request) {
        ConfiguracionEmpresa configuracion = buscarConfiguracion();

        configuracion.setNombreEmpresa(request.nombreEmpresa());
        configuracion.setRut(request.rut());
        configuracion.setEmailContacto(request.emailContacto());
        configuracion.setTelefono(request.telefono());
        configuracion.setDireccion(request.direccion());
        configuracion.setRegion(request.region());

        return toResponse(configuracion);
    }

    private ConfiguracionEmpresa buscarConfiguracion() {
        return repository.findFirstByActivoTrueOrderByIdAsc()
                .orElseThrow(() -> new RecursoNoEncontradoException("Configuracion de empresa no encontrada"));
    }

    private ConfiguracionEmpresaResponse toResponse(ConfiguracionEmpresa entity) {
        return new ConfiguracionEmpresaResponse(
                entity.getId(),
                entity.getNombreEmpresa(),
                entity.getRut(),
                entity.getEmailContacto(),
                entity.getTelefono(),
                entity.getDireccion(),
                entity.getRegion()
        );
    }
}