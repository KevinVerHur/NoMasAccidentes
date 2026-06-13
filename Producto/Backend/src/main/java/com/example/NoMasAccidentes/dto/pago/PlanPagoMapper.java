package com.example.NoMasAccidentes.dto.pago;

import com.example.NoMasAccidentes.model.pago.PlanPago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlanPagoMapper {

    @Mapping(target = "idCliente",          source = "cliente.id")
    @Mapping(target = "razonSocialCliente", source = "cliente.razonSocial")
    @Mapping(target = "idMensualidad",      source = "mensualidad.id")
    @Mapping(target = "nombrePlan",         source = "mensualidad.nombrePlan")
    PlanPagoResponse toResponse(PlanPago plan);
}
