package com.example.NoMasAccidentes.dto.cliente;

import com.example.NoMasAccidentes.model.cliente.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepresentanteMapper {

    @Mapping(target = "idEmpresa",   source = "empresa.id")
    @Mapping(target = "tieneAcceso", expression = "java(cliente.getUsuario() != null)")
    RepresentanteResponse toResponse(Cliente cliente);
}
