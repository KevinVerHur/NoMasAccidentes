package com.example.NoMasAccidentes.dto.pago;

import com.example.NoMasAccidentes.model.pago.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(target = "idPlan",             source = "plan.id")
    @Mapping(target = "idEmpresa",          source = "plan.empresa.id")
    @Mapping(target = "razonSocialEmpresa", source = "plan.empresa.razonSocial")
    PagoResponse toResponse(Pago pago);
}
