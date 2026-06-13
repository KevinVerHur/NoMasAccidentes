package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Multa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MultaMapper {

    @Mapping(target = "idFiscalizacion", source = "fiscalizacion.id")
    MultaResponse toResponse(Multa multa);
}
