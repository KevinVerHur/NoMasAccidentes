package com.example.NoMasAccidentes.dto.empresa;

import com.example.NoMasAccidentes.model.empresa.Empresa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    @Mapping(target = "idRubro",           source = "rubro.id")
    @Mapping(target = "nombreRubro",       source = "rubro.nombre")
    @Mapping(target = "idProfesional",     source = "profesional.id")
    @Mapping(target = "nombreProfesional", expression = "java(empresa.getProfesional() != null ? empresa.getProfesional().getUsuario().getNombre() + \" \" + empresa.getProfesional().getUsuario().getApellido() : null)")
    EmpresaResponse toResponse(Empresa empresa);
}
