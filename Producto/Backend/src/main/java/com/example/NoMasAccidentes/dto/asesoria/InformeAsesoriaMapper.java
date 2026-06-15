package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.informe.Informe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapea el informe de asesoría (reutiliza la entidad {@link Informe} con
 * {@code visita} nula e {@code idAsesoria} seteado).
 */
@Mapper(componentModel = "spring")
public interface InformeAsesoriaMapper {

    @Mapping(target = "tieneArchivo", expression = "java(informe.getUrlPdf() != null)")
    InformeAsesoriaResponse toResponse(Informe informe);
}
