package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.PropuestaMejora;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PropuestaMejoraMapper {

    @Mapping(target = "idInforme", source = "informe.id")
    PropuestaMejoraResponse toResponse(PropuestaMejora propuesta);
}
