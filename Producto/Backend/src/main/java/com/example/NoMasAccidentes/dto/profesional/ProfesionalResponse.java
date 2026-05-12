package com.example.NoMasAccidentes.dto.profesional;

import com.example.NoMasAccidentes.model.profesional.EstadoProfesional;
import java.math.BigDecimal;

/**
 * Vista pública de un Profesional. Incluye datos básicos del Usuario asociado
 * para evitar lookups extra desde el frontend.
 */
public record ProfesionalResponse(
    Long id,
    Long idUsuario,
    String email,
    String nombreCompleto,
    String rut,
    String telefono,
    String especialidad,
    BigDecimal latitud,
    BigDecimal longitud,
    EstadoProfesional estado,
    boolean activo,
    long cantidadClientes
) {}
