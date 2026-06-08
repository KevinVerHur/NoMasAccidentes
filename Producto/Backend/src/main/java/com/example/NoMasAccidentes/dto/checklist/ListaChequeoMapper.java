package com.example.NoMasAccidentes.dto.checklist;

import com.example.NoMasAccidentes.model.checklist.ListaChequeo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ListaChequeoMapper {

    @Mapping(target = "idCliente", source = "cliente.id")
    @Mapping(target = "razonSocialCliente", source = "cliente.razonSocial")
    @Mapping(target = "modificacionesDisponibles", expression = "java(lista.modificacionesDisponibles())")
    ListaChequeoResponse toResponse(ListaChequeo lista);
}