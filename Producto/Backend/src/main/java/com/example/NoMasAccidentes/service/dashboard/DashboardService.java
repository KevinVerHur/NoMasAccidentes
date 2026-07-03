package com.example.NoMasAccidentes.service.dashboard;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.dashboard.DashboardAdminResponse;
import com.example.NoMasAccidentes.dto.dashboard.DashboardAdminResponse.AccidentabilidadCliente;
import com.example.NoMasAccidentes.dto.dashboard.DashboardAdminResponse.Alerta;
import com.example.NoMasAccidentes.dto.dashboard.DashboardAdminResponse.ControlPago;
import com.example.NoMasAccidentes.dto.dashboard.DashboardAdminResponse.Kpis;
import com.example.NoMasAccidentes.dto.dashboard.DashboardAdminResponse.VisitaReciente;
import com.example.NoMasAccidentes.dto.dashboard.DashboardClienteResponse;
import com.example.NoMasAccidentes.dto.dashboard.DashboardProfesionalResponse;
import com.example.NoMasAccidentes.dto.dashboard.DashboardProfesionalResponse.ClienteAsignado;
import com.example.NoMasAccidentes.model.asesoria.EstadoAsesoria;
import com.example.NoMasAccidentes.model.capacitacion.Capacitacion;
import com.example.NoMasAccidentes.model.capacitacion.EstadoCapacitacion;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.empresa.EstadoEmpresa;
import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.pago.Pago;
import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.model.visita.Visita;
import com.example.NoMasAccidentes.repository.asesoria.AccidenteRepository;
import com.example.NoMasAccidentes.repository.asesoria.AsesoriaRepository;
import com.example.NoMasAccidentes.repository.capacitacion.CapacitacionRepository;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
import com.example.NoMasAccidentes.repository.pago.PagoRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.repository.reporte.ReporteMensualRepository;
import com.example.NoMasAccidentes.repository.visita.VisitaRepository;
import com.example.NoMasAccidentes.service.empresa.EmpresaService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Agrega la información del dashboard del administrador (RF45) reutilizando los
 * repositorios de los módulos operativos. Solo lectura; no persiste entidades.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final List<EstadoEmpresa> ESTADOS_MOROSOS = List.of(EstadoEmpresa.MOROSO, EstadoEmpresa.SUSPENDIDO);
    private static final int LIMITE_ASESORIAS = 10;
    private static final int TOPE_ACCIDENTABILIDAD = 5;
    private static final int TOPE_ALERTAS = 6;

    private final EmpresaRepository empresaRepository;
    private final VisitaRepository visitaRepository;
    private final CapacitacionRepository capacitacionRepository;
    private final AsesoriaRepository asesoriaRepository;
    private final AccidenteRepository accidenteRepository;
    private final PagoRepository pagoRepository;
    private final ProfesionalRepository profesionalRepository;
    private final ReporteMensualRepository reporteRepository;
    private final EmpresaService empresaService;

    public DashboardAdminResponse admin() {
        LocalDate hoy = LocalDate.now();
        List<Empresa> empresas = empresaRepository.findAll();
        return new DashboardAdminResponse(
                kpis(hoy),
                visitasRecientes(),
                alertas(hoy, empresas),
                accidentabilidad(hoy.getYear(), empresas),
                controlPagos(hoy, empresas));
    }

    private Kpis kpis(LocalDate hoy) {
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        return new Kpis(
                empresaRepository.countByEstado(EstadoEmpresa.ACTIVO),
                visitaRepository.countByEstadoAndFechaProgramadaBetween(EstadoVisita.PROGRAMADA, lunes, domingo),
                empresaRepository.countByEstadoIn(ESTADOS_MOROSOS),
                capacitacionRepository.countByFechaProgramadaBetween(inicioMes, finMes));
    }

    private List<VisitaReciente> visitasRecientes() {
        return visitaRepository.findTop6ByOrderByFechaProgramadaDesc().stream()
                .map(v -> new VisitaReciente(
                        v.getEmpresa().getId(),
                        v.getEmpresa().getRazonSocial(),
                        nombreProfesional(v),
                        v.getFechaProgramada(),
                        v.getEstado().name()))
                .toList();
    }

    private List<Alerta> alertas(LocalDate hoy, List<Empresa> empresas) {
        List<Alerta> peligros = new ArrayList<>();
        List<Alerta> avisos = new ArrayList<>();

        // Empresas morosas / suspendidas (RF11/RF12).
        for (Empresa c : empresaRepository.findByEstadoIn(ESTADOS_MOROSOS)) {
            peligros.add(new Alerta("peligro", c.getRazonSocial(),
                    "Empresa con pagos pendientes; servicio en riesgo de suspensión."));
        }

        // Visitas planificadas cuya fecha ya pasó y siguen sin realizarse (RF13/RF36).
        for (Visita v : visitaRepository.findByEstadoAndFechaProgramadaLessThan(EstadoVisita.PROGRAMADA, hoy)) {
            peligros.add(new Alerta("peligro", v.getEmpresa().getRazonSocial(),
                    "Visita planificada del " + v.getFechaProgramada().format(F_FECHA) + " sin realizar."));
        }

        // Empresas cerca del límite de asesorías incluidas en el plan (RF27).
        LocalDate inicioAnio = hoy.withDayOfYear(1);
        LocalDate finAnio = hoy.withDayOfYear(hoy.lengthOfYear());
        for (Empresa c : empresas) {
            long usadas = asesoriaRepository.countByEmpresaIdAndFechaSolicitudBetweenAndEstadoNot(
                    c.getId(), inicioAnio, finAnio, EstadoAsesoria.CANCELADA);
            if (usadas >= LIMITE_ASESORIAS - 1) {
                avisos.add(new Alerta("warn", c.getRazonSocial(),
                        "Usó " + usadas + "/" + LIMITE_ASESORIAS + " asesorías incluidas en el plan."));
            }
        }

        // Capacitaciones cuya fecha ya pasó y siguen programadas (RF36).
        for (var cap : capacitacionRepository.findByEstadoAndFechaProgramadaLessThan(EstadoCapacitacion.PROGRAMADA, hoy)) {
            avisos.add(new Alerta("warn", cap.getEmpresa().getRazonSocial(),
                    "Capacitación \"" + cap.getCurso() + "\" del " + cap.getFechaProgramada().format(F_FECHA)
                            + " sin registrar."));
        }

        List<Alerta> alertas = new ArrayList<>(peligros);
        alertas.addAll(avisos);
        return alertas.size() > TOPE_ALERTAS ? alertas.subList(0, TOPE_ALERTAS) : alertas;
    }

    private List<AccidentabilidadCliente> accidentabilidad(int anio, List<Empresa> empresas) {
        LocalDate desde = LocalDate.of(anio, 1, 1);
        LocalDate hasta = LocalDate.of(anio, 12, 31);

        return empresas.stream()
                .map(c -> {
                    long accidentes = accidenteRepository
                            .countByAsesoriaEmpresaIdAndFechaOcurrenciaBetween(c.getId(), desde, hasta);
                    Integer trabajadores = c.getCantidadTrabajadores();
                    Double tasa = (trabajadores != null && trabajadores > 0)
                            ? (accidentes * 100.0) / trabajadores
                            : null;
                    return new AccidentabilidadCliente(c.getId(), c.getRazonSocial(), accidentes, trabajadores, tasa);
                })
                .sorted(Comparator.comparingDouble(
                        (AccidentabilidadCliente a) -> a.tasa() != null ? a.tasa() : -1).reversed())
                .limit(TOPE_ACCIDENTABILIDAD)
                .toList();
    }

    private List<ControlPago> controlPagos(LocalDate hoy, List<Empresa> empresas) {
        List<ControlPago> filas = new ArrayList<>();
        for (Empresa c : empresas) {
            List<Pago> pagos = pagoRepository.findByPlanEmpresaIdOrderByFechaVencimientoDesc(c.getId());

            var planMensual = pagos.isEmpty() ? null : pagos.get(0).getMonto();
            var ultimoPago = pagos.stream()
                    .filter(p -> p.getEstadoPago() == EstadoPago.PAGADO && p.getFechaPago() != null)
                    .map(Pago::getFechaPago)
                    .max(LocalDate::compareTo)
                    .orElse(null);
            long mesesAdeudados = pagos.stream()
                    .filter(p -> p.getEstadoPago() != EstadoPago.PAGADO && p.getFechaVencimiento().isBefore(hoy))
                    .count();

            filas.add(new ControlPago(c.getId(), c.getRazonSocial(), planMensual, ultimoPago,
                    mesesAdeudados, estadoPago(c.getEstado(), mesesAdeudados)));
        }
        return filas;
    }

    // ---- Dashboard del cliente (portal cliente) ----

    public DashboardClienteResponse cliente(String email) {
        Empresa c = empresaService.empresaAutenticada(email);
        Long id = c.getId();
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        LocalDate inicioAnio = hoy.withDayOfYear(1);
        LocalDate finAnio = hoy.withDayOfYear(hoy.lengthOfYear());
        LocalDateTime inicioMesDt = inicioMes.atStartOfDay();
        LocalDateTime finMesDt = finMes.atTime(LocalTime.MAX);

        long visitasRealizadasMes = visitaRepository
                .countByEmpresaIdAndEstadoAndFechaFinBetween(id, EstadoVisita.REALIZADA, inicioMesDt, finMesDt);
        long visitasProgramadasMes = visitaRepository
                .countByEmpresaIdAndFechaProgramadaBetween(id, inicioMes, finMes);
        List<Capacitacion> capacitacionesPendientes = capacitacionRepository
                .findByEmpresaIdAndEstado(id, EstadoCapacitacion.PROGRAMADA);
        long asesoriasUsadas = asesoriaRepository
                .countByEmpresaIdAndFechaSolicitudBetweenAndEstadoNot(id, inicioAnio, finAnio, EstadoAsesoria.CANCELADA);

        List<Pago> pagos = pagoRepository.findByPlanEmpresaIdOrderByFechaVencimientoDesc(id);
        long mesesAdeudados = pagos.stream()
                .filter(p -> p.getEstadoPago() != EstadoPago.PAGADO && p.getFechaVencimiento().isBefore(hoy))
                .count();
        String estadoPago = estadoPago(c.getEstado(), mesesAdeudados);
        LocalDate proximoVencimiento = pagos.stream()
                .filter(p -> p.getEstadoPago() != EstadoPago.PAGADO)
                .map(Pago::getFechaVencimiento)
                .min(LocalDate::compareTo)
                .orElse(null);

        var kpis = new DashboardClienteResponse.Kpis(
                visitasRealizadasMes, visitasProgramadasMes, capacitacionesPendientes.size(),
                asesoriasUsadas, LIMITE_ASESORIAS, estadoPago, proximoVencimiento);

        return new DashboardClienteResponse(
                kpis,
                accionesCliente(id, hoy, estadoPago, proximoVencimiento, capacitacionesPendientes),
                proximasActividades(id, hoy, capacitacionesPendientes),
                resumenPreventivo(id, inicioMes, finMes, inicioAnio, finAnio));
    }

    private List<DashboardClienteResponse.Accion> accionesCliente(
            Long id, LocalDate hoy, String estadoPago, LocalDate proximoVencimiento,
            List<Capacitacion> capacitacionesPendientes) {
        List<DashboardClienteResponse.Accion> acciones = new ArrayList<>();

        if (!"Al día".equals(estadoPago)) {
            acciones.add(new DashboardClienteResponse.Accion("peligro", "Pago pendiente",
                    proximoVencimiento != null
                            ? "Tienes una cuota con vencimiento " + proximoVencimiento.format(F_FECHA) + "."
                            : "Tienes cuotas pendientes; regulariza para mantener el servicio activo."));
        }

        capacitacionesPendientes.stream()
                .filter(cap -> !cap.getFechaProgramada().isBefore(hoy))
                .min(Comparator.comparing(Capacitacion::getFechaProgramada))
                .ifPresent(cap -> acciones.add(new DashboardClienteResponse.Accion("warn", "Confirmación pendiente",
                        "Capacitación \"" + cap.getCurso() + "\" programada para el "
                                + cap.getFechaProgramada().format(F_FECHA) + ".")));

        reporteRepository.findByEmpresaIdOrderByAnioDescMesDesc(id).stream().findFirst()
                .ifPresent(r -> acciones.add(new DashboardClienteResponse.Accion("info", "Reporte mensual disponible",
                        "Tu reporte más reciente puede revisarse desde el módulo de reportes.")));

        return acciones;
    }

    private List<DashboardClienteResponse.ProximaActividad> proximasActividades(
            Long id, LocalDate hoy, List<Capacitacion> capacitacionesPendientes) {
        List<DashboardClienteResponse.ProximaActividad> actividades = new ArrayList<>();

        visitaRepository.findByEmpresaIdOrderByFechaProgramadaDesc(id).stream()
                .filter(v -> v.getEstado() == EstadoVisita.PROGRAMADA && !v.getFechaProgramada().isBefore(hoy))
                .forEach(v -> actividades.add(new DashboardClienteResponse.ProximaActividad(
                        v.getFechaProgramada(), "Visita preventiva", nombreProfesional(v), "Programada")));

        capacitacionesPendientes.stream()
                .filter(cap -> !cap.getFechaProgramada().isBefore(hoy))
                .forEach(cap -> actividades.add(new DashboardClienteResponse.ProximaActividad(
                        cap.getFechaProgramada(), "Capacitación: " + cap.getCurso(), nombreRelator(cap), "Por confirmar")));

        actividades.sort(Comparator.comparing(DashboardClienteResponse.ProximaActividad::fecha));
        return actividades.size() > 5 ? actividades.subList(0, 5) : actividades;
    }

    private DashboardClienteResponse.ResumenPreventivo resumenPreventivo(
            Long id, LocalDate inicioMes, LocalDate finMes, LocalDate inicioAnio, LocalDate finAnio) {
        return new DashboardClienteResponse.ResumenPreventivo(
                accidenteRepository.countByAsesoriaEmpresaIdAndFechaOcurrenciaBetween(id, inicioMes, finMes),
                accidenteRepository.sumDiasPerdidosByEmpresaAndFechaOcurrenciaBetween(id, inicioMes, finMes),
                accidenteRepository.countByAsesoriaEmpresaIdAndFechaOcurrenciaBetween(id, inicioAnio, finAnio),
                capacitacionRepository.countByEmpresaIdAndEstadoAndFechaRealizacionBetween(
                        id, EstadoCapacitacion.REALIZADA, inicioAnio, finAnio));
    }

    // ---- Dashboard del profesional (empresas asignadas) ----

    public DashboardProfesionalResponse profesional(String email) {
        profesionalRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", email));

        List<Empresa> empresas = empresaRepository.findByProfesionalUsuarioEmailOrderByRazonSocialAsc(email);
        long morosos = empresas.stream().filter(c -> ESTADOS_MOROSOS.contains(c.getEstado())).count();

        List<ClienteAsignado> filas = empresas.stream()
                .map(c -> new ClienteAsignado(
                        c.getId(), c.getRazonSocial(),
                        c.getRubro() != null ? c.getRubro().getNombre() : null,
                        ultimaVisita(c.getId()), c.getEstado().name()))
                .toList();

        return new DashboardProfesionalResponse(empresas.size(), morosos, filas);
    }

    private LocalDate ultimaVisita(Long idEmpresa) {
        return visitaRepository.findByEmpresaIdOrderByFechaProgramadaDesc(idEmpresa).stream()
                .filter(v -> v.getEstado() == EstadoVisita.REALIZADA && v.getFechaFin() != null)
                .map(v -> v.getFechaFin().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    private String nombreRelator(Capacitacion cap) {
        if (cap.getRelator() == null || cap.getRelator().getUsuario() == null) {
            return "—";
        }
        var u = cap.getRelator().getUsuario();
        return u.getNombre() + " " + u.getApellido();
    }

    private String estadoPago(EstadoEmpresa estado, long mesesAdeudados) {
        return switch (estado) {
            case SUSPENDIDO -> "Suspendido";
            case MOROSO -> "Moroso";
            case ACTIVO -> mesesAdeudados > 0 ? "Atrasado" : "Al día";
        };
    }

    private String nombreProfesional(Visita v) {
        var u = v.getProfesional().getUsuario();
        return u.getNombre() + " " + u.getApellido();
    }
}
