package com.example.NoMasAccidentes.dto.pago;

import com.example.NoMasAccidentes.model.pago.PlanPago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlanPagoMapper {

    @Mapping(target = "idEmpresa",          source = "empresa.id")
    @Mapping(target = "razonSocialEmpresa", source = "empresa.razonSocial")
    @Mapping(target = "idMensualidad",      source = "mensualidad.id")
    @Mapping(target = "nombrePlan",         source = "mensualidad.nombrePlan")
    PlanPagoResponse toResponse(PlanPago plan);
}
