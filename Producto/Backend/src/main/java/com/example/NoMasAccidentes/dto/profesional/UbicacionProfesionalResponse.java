// Producto/Backend/src/main/java/com/example/NoMasAccidentes/dto/profesional/UbicacionProfesionalResponse.java
package com.example.NoMasAccidentes.dto.profesional;

import com.example.NoMasAccidentes.model.profesional.EstadoProfesional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UbicacionProfesionalResponse(
    Long idProfesional,
    String nombreProfesional,
    String email,
    EstadoProfesional estado,
    BigDecimal latitud,
    BigDecimal longitud,
    LocalDateTime fechaRegistro
) {}