package com.example.NoMasAccidentes.dto.usuario;

import com.example.NoMasAccidentes.model.usuario.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Conversiones Usuario ↔ DTO. La asignación de Rol y el hasheo de password
 * los hace UsuarioService, no este mapper.
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "rol", expression = "java(usuario.getRol() != null ? usuario.getRol().getNombre() : null)")
    UsuarioResponse toResponse(Usuario usuario);
}
