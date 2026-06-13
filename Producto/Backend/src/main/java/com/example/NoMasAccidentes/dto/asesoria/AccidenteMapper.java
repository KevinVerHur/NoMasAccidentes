package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Accidente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccidenteMapper {

    @Mapping(target = "idAsesoria", source = "asesoria.id")
    AccidenteResponse toResponse(Accidente accidente);
}
