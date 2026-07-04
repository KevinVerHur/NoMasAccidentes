package com.example.NoMasAccidentes.service.pago;

import com.example.NoMasAccidentes.repository.pago.PlanPagoRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CuotaMensualScheduler {

    private final PlanPagoRepository planPagoRepository;
    private final PlanPagoService planPagoService;

    @Scheduled(cron = "0 0 6 1 * *")
    public void generarCuotasDelMes() {
        LocalDate hoy = LocalDate.now();

        planPagoRepository.findByActivoTrue()
                .forEach(plan -> planPagoService.generarCuotaMensualSiNoExiste(plan, hoy));

        log.info("Generación mensual de cuotas ejecutada para {}", hoy);
    }
}