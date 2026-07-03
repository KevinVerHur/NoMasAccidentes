package com.example.NoMasAccidentes.dto.empresa;

import com.example.NoMasAccidentes.model.empresa.EstadoEmpresa;

public record EmpresaResponse(
    Long id,
    String razonSocial,
    String rut,
    String direccion,
    String comuna,
    Long idRubro,
    String nombreRubro,
    String plan,
    Integer cantidadTrabajadores,
    EstadoEmpresa estado,
    Long idProfesional,
    String nombreProfesional,
    boolean activo
) {}
