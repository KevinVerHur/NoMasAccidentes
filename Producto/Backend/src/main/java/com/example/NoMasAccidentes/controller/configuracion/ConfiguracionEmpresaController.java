package com.example.NoMasAccidentes.controller.configuracion;

import com.example.NoMasAccidentes.dto.configuracion.ActualizarConfiguracionEmpresaRequest;
import com.example.NoMasAccidentes.dto.configuracion.ConfiguracionEmpresaResponse;
import com.example.NoMasAccidentes.service.configuracion.ConfiguracionEmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracion/empresa")
@RequiredArgsConstructor
public class ConfiguracionEmpresaController {

    private final ConfiguracionEmpresaService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ConfiguracionEmpresaResponse obtener() {
        return service.obtener();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ConfiguracionEmpresaResponse actualizar(
            @Valid @RequestBody ActualizarConfiguracionEmpresaRequest request
    ) {
        return service.actualizar(request);
    }
}