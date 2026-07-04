package com.example.NoMasAccidentes.service.pago;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.pago.CrearPlanPagoRequest;
import com.example.NoMasAccidentes.dto.pago.PlanPagoMapper;
import com.example.NoMasAccidentes.dto.pago.PlanPagoResponse;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.pago.Mensualidad;
import com.example.NoMasAccidentes.model.pago.Pago;
import com.example.NoMasAccidentes.model.pago.Periodicidad;
import com.example.NoMasAccidentes.model.pago.PlanPago;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
import com.example.NoMasAccidentes.repository.pago.PagoRepository;
import com.example.NoMasAccidentes.repository.pago.PlanPagoRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Planes de pago por empresa (RF08). Al crear el plan genera sus cuotas.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PlanPagoService {

    private final PlanPagoRepository planPagoRepository;
    private final PagoRepository pagoRepository;
    private final EmpresaRepository empresaRepository;
    private final MensualidadService mensualidadService;
    private final PlanPagoMapper planPagoMapper;

    @Transactional
    public PlanPagoResponse crear(CrearPlanPagoRequest request) {
        Empresa empresa = empresaRepository.findById(request.idEmpresa())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa", request.idEmpresa()));
        Mensualidad mensualidad = mensualidadService.buscarOFallar(request.idMensualidad());
        Periodicidad periodicidad = request.periodicidad() != null ? request.periodicidad() : Periodicidad.MENSUAL;

        LocalDate fechaTermino = vencimientoCuota(request.fechaInicio(), periodicidad, request.cuotasTotales());

        PlanPago plan = PlanPago.builder()
                .empresa(empresa)
                .mensualidad(mensualidad)
                .fechaInicio(request.fechaInicio())
                .fechaTermino(fechaTermino)
                .cuotasTotales(request.cuotasTotales())
                .periodicidad(periodicidad)
                .build();
        PlanPago guardado = planPagoRepository.save(plan);

        generarCuotas(guardado, mensualidad, periodicidad);
        log.info("Plan de pago creado id={} empresa={} cuotas={} (RF08)",
                guardado.getId(), empresa.getId(), request.cuotasTotales());
        return planPagoMapper.toResponse(guardado);
    }

    private void generarCuotas(PlanPago plan, Mensualidad mensualidad, Periodicidad periodicidad) {
        for (int cuota = 1; cuota <= plan.getCuotasTotales(); cuota++) {
            LocalDate vencimiento = vencimientoCuota(plan.getFechaInicio(), periodicidad, cuota - 1);
            pagoRepository.save(Pago.builder()
                    .plan(plan)
                    .numeroCuota(cuota)
                    .monto(mensualidad.getMontoBase())
                    .fechaEmision(plan.getFechaInicio())
                    .fechaVencimiento(vencimiento)
                    .estadoPago(EstadoPago.PENDIENTE)
                    .build());
        }
    }

    private LocalDate vencimientoCuota(LocalDate inicio, Periodicidad periodicidad, int periodos) {
        return switch (periodicidad) {
            case MENSUAL    -> inicio.plusMonths(periodos);
            case TRIMESTRAL -> inicio.plusMonths(3L * periodos);
            case ANUAL      -> inicio.plusYears(periodos);
        };
    }

    public List<PlanPagoResponse> listarPorEmpresa(Long idEmpresa) {
        return planPagoRepository.findByEmpresaId(idEmpresa).stream().map(planPagoMapper::toResponse).toList();
    }

    public PlanPagoResponse obtenerPorId(Long id) {
        return planPagoMapper.toResponse(planPagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan de pago", id)));
    }

    @Transactional
    public PlanPagoResponse asignarPlanBasico(Empresa empresa) {
        Mensualidad mensualidad = mensualidadService.buscarPlanBasico();

        PlanPago plan = PlanPago.builder()
            .empresa(empresa)
            .mensualidad(mensualidad)
            .fechaInicio(LocalDate.now())
            .fechaTermino(null)
            .cuotasTotales(null)
            .periodicidad(Periodicidad.MENSUAL)
            .activo(true)
            .build();

        PlanPago guardado = planPagoRepository.save(plan);

        generarCuotaMensualSiNoExiste(guardado, LocalDate.now());

        log.info("Plan básico activo asignado automáticamente empresa={} plan={}",
            empresa.getId(), guardado.getId());

        return planPagoMapper.toResponse(guardado);
    }

    @Transactional
    public void generarCuotaMensualSiNoExiste(PlanPago plan, LocalDate fechaReferencia) {
        LocalDate inicioMes = fechaReferencia.withDayOfMonth(1);

        boolean yaExiste = pagoRepository.existsByPlanIdAndFechaEmision(plan.getId(), inicioMes);

        if (yaExiste) {
            return;
        }

        int numeroCuota = pagoRepository.countByPlanId(plan.getId()) + 1;

        pagoRepository.save(Pago.builder()
            .plan(plan)
            .numeroCuota(numeroCuota)
            .monto(plan.getMensualidad().getMontoBase())
            .fechaEmision(inicioMes)
            .fechaVencimiento(inicioMes)
            .estadoPago(EstadoPago.PENDIENTE)
            .build());

        log.info("Cuota mensual generada plan={} cuota={} periodo={}",
            plan.getId(), numeroCuota, inicioMes);
    }
}
