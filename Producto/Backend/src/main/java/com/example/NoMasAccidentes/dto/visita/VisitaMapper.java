package com.example.NoMasAccidentes.dto.visita;

import com.example.NoMasAccidentes.model.visita.Visita;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VisitaMapper {

    @Mapping(target = "idEmpresa",          source = "empresa.id")
    @Mapping(target = "razonSocialEmpresa", source = "empresa.razonSocial")
    @Mapping(target = "idProfesional",      source = "profesional.id")
    @Mapping(target = "nombreProfesional",  expression = "java(visita.getProfesional().getUsuario().getNombre() + \" \" + visita.getProfesional().getUsuario().getApellido())")
    @Mapping(target = "idListaChequeo",     source = "listaChequeo.id")
    VisitaResponse toResponse(Visita visita);
}
