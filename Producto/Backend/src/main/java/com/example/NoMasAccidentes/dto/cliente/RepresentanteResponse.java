package com.example.NoMasAccidentes.dto.cliente;

public record RepresentanteResponse(
    Long id,
    Long idEmpresa,
    String nombre,
    String cargo,
    String email,
    String telefono,
    /** true si el representante tiene cuenta de acceso al portal (rol CLIENTE). */
    boolean tieneAcceso,
    boolean activo
) {}
