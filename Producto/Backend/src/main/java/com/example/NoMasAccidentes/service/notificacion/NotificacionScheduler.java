package com.example.NoMasAccidentes.service.notificacion;

import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.repository.capacitacion.CapacitacionRepository;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.pago.PagoRepository;
import com.example.NoMasAccidentes.repository.visita.VisitaRepository;
import com.example.NoMasAccidentes.service.actividad.ActividadPreventivaService;
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
    private final CapacitacionRepository capacitacionRepository;
    private final ClienteRepository clienteRepository;
    private final ActividadPreventivaService actividadPreventivaService;
    private final CorreoService correoService;

    private static final DateTimeFormatter FECHA_FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter HORA_FORMATO =
            DateTimeFormatter.ofPattern("HH:mm");

    @Scheduled(cron = "0 0 8 * * *", zone = "America/Santiago")
    @Transactional
    public void enviarRecordatoriosVisitas24HorasAntes(){
        LocalDate manana = LocalDate.now().plusDays(1);

        var visitas = visitaRepository.findByEstadoAndRecordatorioEnviadoFalseAndFechaProgramada(
                EstadoVisita.PROGRAMADA,
                manana
        );

        visitas.forEach(visita -> {
            Empresa empresa = visita.getEmpresa();
            String email = emailRepresentante(empresa);
            if (email != null) {
                correoService.enviarRecordatorioVisita(
                        email,
                        empresa.getRazonSocial(),
                        visita.getFechaProgramada().format(FECHA_FORMATO),
                        ""
                );
            }
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
            Empresa empresa = pago.getPlan().getEmpresa();
            String email = emailRepresentante(empresa);
            if (email != null) {
                correoService.enviarAlertaPagoPendiente(
                    email,
                    empresa.getRazonSocial(),
                    pago.getFechaVencimiento().format(FECHA_FORMATO),
                    formatoMoneda.format(pago.getMonto())
                );
            }
            pago.setAlertaEnviada(true);
        });

        log.info("Alertas de pagos pendientes procesadas: {}", pagos.size());
    }

    /**
     * RF30: recordatorio al representante 3 días antes de cada capacitación programada.
     */
    @Scheduled(cron = "0 30 8 * * MON-FRI", zone = "America/Santiago")
    @Transactional
    public void enviarRecordatoriosCapacitaciones(){
        LocalDate enTresDias = LocalDate.now().plusDays(3);

        var capacitaciones = capacitacionRepository.findParaRecordatorio(enTresDias);

        capacitaciones.forEach(cap -> {
            Empresa empresa = cap.getEmpresa();
            String email = emailRepresentante(empresa);
            if (email != null) {
                correoService.enviarRecordatorioCapacitacion(
                        email,
                        empresa.getRazonSocial(),
                        cap.getCurso(),
                        cap.getFechaProgramada().format(FECHA_FORMATO),
                        cap.getHoraProgramada().format(HORA_FORMATO)
                );
            }
            cap.setRecordatorioEnviado(true);
        });

        log.info("Recordatorios de capacitaciones procesados: {}", capacitaciones.size());
    }

    /**
     * RF32: avisa al admin de capacitaciones programadas que no se realizaron en su fecha.
     */
    @Scheduled(cron = "0 0 10 * * MON-FRI", zone = "America/Santiago")
    @Transactional
    public void notificarCapacitacionesNoRealizadas(){
        var capacitaciones = capacitacionRepository.findIncumplidasSinNotificar(LocalDate.now());

        capacitaciones.forEach(cap -> {
            correoService.enviarAlertaCapacitacionNoRealizada(
                    cap.getEmpresa().getRazonSocial(),
                    cap.getCurso(),
                    cap.getFechaProgramada().format(FECHA_FORMATO)
            );
            cap.setIncumplimientoNotificado(true);
        });

        log.info("Alertas de capacitaciones no realizadas procesadas: {}", capacitaciones.size());
    }

    /**
     * RF32: avisa al admin de actividades preventivas planificadas que vencieron sin cumplirse.
     * Engancha la lógica ya existente en ActividadPreventivaService (antes sin scheduler).
     */
    @Scheduled(cron = "0 15 10 * * MON-FRI", zone = "America/Santiago")
    public void notificarActividadesPreventivasVencidas(){
        int procesadas = actividadPreventivaService.procesarVencidasYAlertar();
        log.info("Alertas de actividades preventivas vencidas procesadas: {}", procesadas);
    }

    /** Correo del representante principal de la empresa (primero con email válido). */
    private String emailRepresentante(Empresa empresa) {
        return clienteRepository.findByEmpresaId(empresa.getId()).stream()
                .map(rep -> rep.getEmail())
                .filter(e -> e != null && !e.isBlank())
                .findFirst()
                .orElse(null);
    }
}
