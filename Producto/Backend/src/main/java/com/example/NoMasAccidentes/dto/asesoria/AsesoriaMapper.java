package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.Asesoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AsesoriaMapper {

    @Mapping(target = "idCliente",          source = "cliente.id")
    @Mapping(target = "razonSocialCliente", source = "cliente.razonSocial")
    @Mapping(target = "idProfesional",      source = "profesional.id")
    @Mapping(target = "nombreProfesional",  expression = "java(asesoria.getProfesional().getUsuario().getNombre() + \" \" + asesoria.getProfesional().getUsuario().getApellido())")
    AsesoriaResponse toResponse(Asesoria asesoria);
}
