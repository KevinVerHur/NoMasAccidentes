package com.example.NoMasAccidentes.service.pago;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PagoMorosidadScheduler {

    private final PagoService pagoService;

    @Scheduled(cron = "${app.scheduler.morosidad.cron}", zone = "America/Santiago")
    @Transactional
    public void ejecutarControlDiarioMorosidad() {
        int cuotasAtrasadas = pagoService.evaluarMorosidad();
        int clientesSuspendidos = pagoService.suspenderMorosos();

        log.info(
                "Control diario de morosidad ejecutado: {} cuota(s) atrasadas, {} cliente(s) suspendido(s) (RF11/RF12)",
                cuotasAtrasadas,
                clientesSuspendidos
        );
    }
}