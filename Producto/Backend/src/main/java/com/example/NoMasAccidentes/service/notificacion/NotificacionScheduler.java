package com.example.NoMasAccidentes.service.notificacion;

import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.repository.pago.PagoRepository;
import com.example.NoMasAccidentes.repository.visita.VisitaRepository;
import com.example.NoMasAccidentes.service.usuario.CorreoService;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionScheduler {
    
    private final VisitaRepository visitaRepository;
    private final PagoRepository pagoRepository;
    private final CorreoService correoService;

    private static final DateTimeFormatter FECHA_FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Scheduled(cron = "0 0 8 * * *", zone = "America/Santiago")
    @Transactional
    public void enviarRecordatoriosVisitas24HorasAntes(){
        LocalDate manana = LocalDate.now().plusDays(1);

        var visitas = visitaRepository.findByEstadoAndRecordatorioEnviadoFalseAndFechaProgramada(
                EstadoVisita.PROGRAMADA,
                manana
        );

        visitas.forEach(visita -> {
            var cliente = visita.getCliente();
            correoService.enviarRecordatorioVisita(
                    cliente.getEmail(),
                    cliente.getRazonSocial(),
                    visita.getFechaProgramada().format(FECHA_FORMATO),
                    ""
            );
            visita.setRecordatorioEnviado(true);
        });

        log.info("Recordatorios de visitas procesados: {}", visitas.size());
    }


    @Scheduled(cron = "0 0 9 * * *", zone = "America/Santiago")
    @Transactional
    public void enviarAlertasPagosPendientes(){
        var pagos = pagoRepository.findByEstadoPagoInAndAlertaEnviadaFalseAndFechaVencimientoLessThanEqual(
                List.of(EstadoPago.PENDIENTE, EstadoPago.ATRASADO),
                LocalDate.now()
        );

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

        pagos.forEach(pago -> {
            var cliente = pago.getPlan().getCliente();
            correoService.enviarAlertaPagoPendiente(
                cliente.getEmail(),
                cliente.getRazonSocial(),
                pago.getFechaVencimiento().format(FECHA_FORMATO),
                formatoMoneda.format(pago.getMonto())
            );
            pago.setAlertaEnviada(true);
        });

        log.info("Alertas de pagos pendientes procesadas: {}", pagos.size());
    }
}
