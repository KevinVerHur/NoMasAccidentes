package com.example.NoMasAccidentes.dto.profesional;

import com.example.NoMasAccidentes.model.profesional.Profesional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Conversión Profesional → DTO. La construcción de la entidad
 * (asignación de Usuario) la hace ProfesionalService.
 */
@Mapper(componentModel = "spring")
public interface ProfesionalMapper {

    @Mapping(target = "idUsuario",      source = "usuario.id")
    @Mapping(target = "email",          source = "usuario.email")
    @Mapping(target = "nombreCompleto", expression = "java(profesional.getUsuario().getNombre() + \" \" + profesional.getUsuario().getApellido())")
    ProfesionalResponse toResponse(Profesional profesional);
}
