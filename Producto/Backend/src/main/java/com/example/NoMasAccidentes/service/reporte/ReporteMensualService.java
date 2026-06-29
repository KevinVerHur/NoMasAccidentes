package com.example.NoMasAccidentes.service.reporte;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.reporte.ReporteMensualMapper;
import com.example.NoMasAccidentes.dto.reporte.ReporteMensualResponse;
import com.example.NoMasAccidentes.model.capacitacion.EstadoCapacitacion;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.reporte.ReporteMensual;
import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.repository.asesoria.AccidenteRepository;
import com.example.NoMasAccidentes.repository.asesoria.AsesoriaRepository;
import com.example.NoMasAccidentes.repository.asesoria.MultaRepository;
import com.example.NoMasAccidentes.repository.capacitacion.CapacitacionRepository;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.consulta.ConsultaRepository;
import com.example.NoMasAccidentes.repository.pago.CobroExtraRepository;
import com.example.NoMasAccidentes.repository.reporte.ReporteMensualRepository;
import com.example.NoMasAccidentes.repository.visita.VisitaRepository;
import com.example.NoMasAccidentes.service.cliente.ClienteService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reporte mensual de gestión por cliente (RF38, RF39). Capa de agregación:
 * consolida los totales realizados en el periodo a partir de los módulos
 * operativos y produce el PDF, reutilizando el almacenamiento de informes.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReporteMensualService {

    private final ReporteMensualRepository reporteRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final ReporteMensualMapper mapper;
    private final ReporteMensualPdfService pdfService;
    private final com.example.NoMasAccidentes.service.informe.AlmacenamientoInformeService almacenamiento;

    // Repositorios de los módulos que alimentan el reporte (RF39).
    private final VisitaRepository visitaRepository;
    private final CapacitacionRepository capacitacionRepository;
    private final AsesoriaRepository asesoriaRepository;
    private final ConsultaRepository consultaRepository;
    private final AccidenteRepository accidenteRepository;
    private final MultaRepository multaRepository;
    private final CobroExtraRepository cobroExtraRepository;

    /**
     * Genera (o regenera, RF46) el reporte mensual de un cliente para el periodo
     * indicado. Contabiliza lo realizado en el mes y produce el PDF. Idempotente
     * sobre la clave natural (cliente, mes, año).
     */
    @Transactional
    public ReporteMensualResponse generar(Long idCliente, int mes, int anio) {
        validarPeriodo(mes, anio);
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", idCliente));

        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        LocalDateTime desdeDt = desde.atStartOfDay();
        LocalDateTime hastaDt = hasta.atTime(LocalTime.MAX);

        var existente = reporteRepository.findByClienteIdAndMesAndAnio(idCliente, mes, anio);
        ReporteMensual reporte = existente.orElseGet(ReporteMensual::new);
        reporte.setCliente(cliente);
        reporte.setMes(mes);
        reporte.setAnio(anio);
        reporte.setFechaEmision(LocalDate.now());
        reporte.setEsActualizacionExtra(existente.isPresent());

        reporte.setTotalVisitas((int) visitaRepository
                .countByClienteIdAndEstadoAndFechaFinBetween(idCliente, EstadoVisita.REALIZADA, desdeDt, hastaDt));
        reporte.setTotalCapacitaciones((int) capacitacionRepository
                .countByClienteIdAndEstadoAndFechaRealizacionBetween(idCliente, EstadoCapacitacion.REALIZADA, desde, hasta));
        reporte.setTotalAsesorias((int) asesoriaRepository
                .countByClienteIdAndFechaAtencionBetween(idCliente, desde, hasta));
        reporte.setTotalLlamados((int) consultaRepository
                .countByClienteIdAndFechaHoraBetween(idCliente, desdeDt, hastaDt));
        reporte.setTotalAccidentes((int) accidenteRepository
                .countByAsesoriaClienteIdAndFechaOcurrenciaBetween(idCliente, desde, hasta));
        reporte.setTotalMultas((int) multaRepository
                .countByFiscalizacionAsesoriaClienteIdAndFechaEmisionBetween(idCliente, desde, hasta));
        reporte.setCostosExtra(cobroExtraRepository
                .sumMontoByClienteAndFechaGeneracionBetween(idCliente, desde, hasta));

        byte[] pdf = pdfService.generar(reporte);
        String nombreArchivo = "reporte-" + idCliente + "-" + anio + "-" + mes + "-" + UUID.randomUUID() + ".pdf";
        reporte.setUrlPdf(almacenamiento.guardar(nombreArchivo, pdf));

        ReporteMensual guardado = reporteRepository.save(reporte);
        log.info("Reporte mensual generado id={} cliente={} periodo={}-{} (RF38)",
                guardado.getId(), idCliente, anio, mes);
        return mapper.toResponse(guardado);
    }

    /** Reportes históricos de un cliente (RF38, RF42). */
    public List<ReporteMensualResponse> listarPorCliente(Long idCliente) {
        return reporteRepository.findByClienteIdOrderByAnioDescMesDesc(idCliente)
                .stream().map(mapper::toResponse).toList();
    }

    public ReporteMensualResponse obtener(Long idReporte) {
        return mapper.toResponse(buscarOFallar(idReporte));
    }

    /** Descarga el PDF del reporte. */
    public byte[] descargarPdf(Long idReporte) {
        return almacenamiento.descargar(conArchivo(buscarOFallar(idReporte)).getUrlPdf());
    }

    // ---- Portal cliente (solo lectura de lo propio) ----

    public List<ReporteMensualResponse> listarMisReportes(String emailUsuario) {
        Long idCliente = clienteService.clienteAutenticado(emailUsuario).getId();
        return listarPorCliente(idCliente);
    }

    public byte[] descargarMiPdf(Long idReporte, String emailUsuario) {
        Long idCliente = clienteService.clienteAutenticado(emailUsuario).getId();
        ReporteMensual reporte = conArchivo(buscarOFallar(idReporte));
        if (!reporte.getCliente().getId().equals(idCliente)) {
            // No revelar la existencia de reportes ajenos.
            throw new RecursoNoEncontradoException("Reporte", idReporte);
        }
        return almacenamiento.descargar(reporte.getUrlPdf());
    }

    // ---- internos ----

    private ReporteMensual buscarOFallar(Long idReporte) {
        return reporteRepository.findById(idReporte)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reporte", idReporte));
    }

    private ReporteMensual conArchivo(ReporteMensual reporte) {
        if (reporte.getUrlPdf() == null) {
            throw new ConflictoNegocioException("El reporte no tiene un archivo asociado");
        }
        return reporte;
    }

    private void validarPeriodo(int mes, int anio) {
        if (mes < 1 || mes > 12) {
            throw new ConflictoNegocioException("El mes debe estar entre 1 y 12");
        }
        if (anio < 2000 || anio > LocalDate.now().getYear() + 1) {
            throw new ConflictoNegocioException("El año del reporte no es válido");
        }
    }
}
