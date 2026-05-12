package com.example.NoMasAccidentes.dto.cliente;

import com.example.NoMasAccidentes.model.cliente.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(target = "idProfesional",    source = "profesional.id")
    @Mapping(target = "nombreProfesional", expression = "java(cliente.getProfesional() != null ? cliente.getProfesional().getUsuario().getNombre() + \" \" + cliente.getProfesional().getUsuario().getApellido() : null)")
    ClienteResponse toResponse(Cliente cliente);
}
