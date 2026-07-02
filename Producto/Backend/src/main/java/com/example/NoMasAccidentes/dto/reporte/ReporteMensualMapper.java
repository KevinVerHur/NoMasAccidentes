package com.example.NoMasAccidentes.dto.reporte;

import com.example.NoMasAccidentes.model.reporte.ReporteMensual;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReporteMensualMapper {

    @Mapping(target = "idEmpresa",          source = "empresa.id")
    @Mapping(target = "razonSocialEmpresa", source = "empresa.razonSocial")
    @Mapping(target = "tieneArchivo",       expression = "java(reporte.getUrlPdf() != null)")
    ReporteMensualResponse toResponse(ReporteMensual reporte);
}
