// Producto/Backend/src/main/java/com/example/NoMasAccidentes/dto/profesional/RegistrarUbicacionRequest.java
package com.example.NoMasAccidentes.dto.profesional;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RegistrarUbicacionRequest(

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    BigDecimal latitud,

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    BigDecimal longitud
) {}