package com.example.NoMasAccidentes.dto.pago;

import com.example.NoMasAccidentes.model.pago.Mensualidad;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MensualidadMapper {

    MensualidadResponse toResponse(Mensualidad mensualidad);

    Mensualidad toEntity(CrearMensualidadRequest request);
}
