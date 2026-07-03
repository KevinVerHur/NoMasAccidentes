package com.example.NoMasAccidentes.dto.configuracion;

public record ConfiguracionEmpresaResponse(
        Long id,
        String nombreEmpresa,
        String rut,
        String emailContacto,
        String telefono,
        String direccion,
        String region
) {
}