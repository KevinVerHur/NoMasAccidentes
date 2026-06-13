package com.example.NoMasAccidentes.dto.visita;

import com.example.NoMasAccidentes.model.visita.ItemChequeo;
import com.example.NoMasAccidentes.model.visita.ListaChequeo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ListaChequeoMapper {

    @Mapping(target = "idCliente", source = "cliente.id")
    ListaChequeoResponse toResponse(ListaChequeo lista);

    ItemChequeoResponse toItemResponse(ItemChequeo item);
}
