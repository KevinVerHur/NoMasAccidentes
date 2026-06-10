package com.example.NoMasAccidentes.dto.pago;

import com.example.NoMasAccidentes.model.pago.CobroExtra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CobroExtraMapper {

    @Mapping(target = "idPago", source = "pago.id")
    CobroExtraResponse toResponse(CobroExtra cobroExtra);
}
