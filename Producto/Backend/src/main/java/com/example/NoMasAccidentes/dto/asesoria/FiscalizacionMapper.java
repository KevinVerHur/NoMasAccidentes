package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Fiscalizacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FiscalizacionMapper {

    @Mapping(target = "idAsesoria", source = "asesoria.id")
    FiscalizacionResponse toResponse(Fiscalizacion fiscalizacion);
}
