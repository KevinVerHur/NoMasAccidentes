package com.example.NoMasAccidentes.dto.pago;

import com.example.NoMasAccidentes.model.pago.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(target = "idPlan",             source = "plan.id")
    @Mapping(target = "idCliente",          source = "plan.cliente.id")
    @Mapping(target = "razonSocialCliente", source = "plan.cliente.razonSocial")
    PagoResponse toResponse(Pago pago);
}
