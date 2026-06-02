package com.example.NoMasAccidentes.service.notificacion;

import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.repository.pago.PagoRepository;
import com.example.NoMasAccidentes.repository.visita.VisitaRepository;
import com.example.NoMasAccidentes.service.usuario.CorreoService;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private static final DateTimeFormatter FECHA_HORA = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Scheduled(cron = "0 0 * * * *", zone = "America/Santiago")
    @Transactional
    public void enviarRecordatoriosVisitas24HorasAntes(){
        LocalDateTime desde = LocalDateTime.now().plusHours(24);
        LocalDateTime hasta = desde.plusHours(1);

        var visitas = visitaRepository.findByEstadoAndRecordatorioEnviadoFalseAndFechaProgramadaBetween(
                EstadoVisita.PROGRAMADA,
                desde,
                hasta
        );

        visitas.forEach(visita -> {
            var cliente = visita.getCliente();

            correoService.enviarRecordatorioVisita(
                    cliente.getEmail(),
                    cliente.getRazonSocial(),
                    visita.getFechaProgramada().format(FECHA_HORA),
                    visita.getDireccion()
            );

            visita.setRecordatorioEnviado(true);
        });

        log.info("Recordatorios de visitas procesados: {}", visitas.size());
    }


    @Scheduled(cron = "0 0 9 * * *", zone = "America/Santiago")
    @Transactional
    public void enviarAlertasPagosPendientes(){
        var pagos = pagoRepository.findByEstadoInAndAlertaEnviadaFalseAndFechaVencimientoLessThanEqual(
                List.of(EstadoPago.PENDIENTE, EstadoPago.VENCIDO),
                LocalDate.now()      
        );

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

        pagos.forEach(pago -> {
            var cliente = pago.getCliente();

            correoService.enviarAlertaPagoPendiente(
                cliente.getEmail(),
                cliente.getRazonSocial(),
                pago.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                formatoMoneda.format(pago.getMonto())
            );

            pago.setAlertaEnviada(true);
        });

        log.info("Alertas de pagos pendientes procesadas: {}", pagos.size());
    }
}
