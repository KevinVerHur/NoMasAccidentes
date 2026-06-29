package com.example.NoMasAccidentes.dto.cliente;

import com.example.NoMasAccidentes.model.cliente.EstadoCliente;

public record ClienteResponse(
    Long id,
    String razonSocial,
    String rut,
    String nombreContacto,
    String email,
    String telefono,
    String rubro,
    String plan,
    Integer cantidadTrabajadores,
    EstadoCliente estado,
    Long idProfesional,
    String nombreProfesional,
    boolean activo
) {}
