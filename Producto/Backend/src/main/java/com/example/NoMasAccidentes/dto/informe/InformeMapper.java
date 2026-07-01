package com.example.NoMasAccidentes.dto.informe;

import com.example.NoMasAccidentes.model.informe.Informe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InformeMapper {

    @Mapping(target = "idVisita",           source = "visita.id")
    @Mapping(target = "razonSocialEmpresa", source = "visita.empresa.razonSocial")
    @Mapping(target = "nombreProfesional",  expression = "java(informe.getVisita().getProfesional().getUsuario().getNombre() + \" \" + informe.getVisita().getProfesional().getUsuario().getApellido())")
    @Mapping(target = "tieneArchivo",       expression = "java(informe.getUrlPdf() != null)")
    InformeResponse toResponse(Informe informe);
}
