package com.example.NoMasAccidentes.service.reporte;

import com.example.NoMasAccidentes.dto.reporte.ReporteMensualResponse;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.service.usuario.CorreoService;
import java.time.YearMonth;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cierre mensual automático de reportes (RF46). Genera el reporte del mes recién
 * terminado para todos los clientes. Cada cliente se procesa en su propia
 * transacción (la llamada externa a {@link ReporteMensualService#generar}), de
 * modo que un fallo aislado no detiene al resto del lote.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReporteMensualScheduler {

    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private final ReporteMensualService reporteService;
    private final ClienteRepository clienteRepository;
    private final CorreoService correoService;

    @Scheduled(cron = "${app.scheduler.reporte-mensual.cron}", zone = "America/Santiago")
    public void cierreMensualProgramado() {
        YearMonth periodo = YearMonth.now(ZONA).minusMonths(1);
        int generados = generarCierreMensual(periodo.getMonthValue(), periodo.getYear());
        log.info("Cierre mensual de reportes ejecutado para {}: {} reporte(s) generados (RF46)",
                periodo, generados);
    }

    /**
     * Genera el reporte del periodo para todos los clientes y se lo envía por
     * correo con el PDF adjunto. Devuelve cuántos se generaron.
     */
    public int generarCierreMensual(int mes, int anio) {
        int generados = 0;
        for (Cliente cliente : clienteRepository.findAll()) {
            try {
                ReporteMensualResponse reporte = reporteService.generar(cliente.getId(), mes, anio);
                generados++;
                enviarPorCorreo(cliente, reporte, mes, anio);
            } catch (Exception e) {
                log.error("No se pudo generar el reporte {}-{} del cliente id={}: {}",
                        anio, mes, cliente.getId(), e.getMessage());
            }
        }
        return generados;
    }

    /** Envía el reporte al cliente con el PDF adjunto (un fallo de correo no afecta la generación). */
    private void enviarPorCorreo(Cliente cliente, ReporteMensualResponse reporte, int mes, int anio) {
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            log.warn("Cliente id={} sin email; no se envía el reporte {}-{}", cliente.getId(), anio, mes);
            return;
        }
        byte[] pdf = reporteService.descargarPdf(reporte.id());
        correoService.enviarReporteMensual(cliente.getEmail(), cliente.getRazonSocial(), mes, anio, pdf);
    }
}
