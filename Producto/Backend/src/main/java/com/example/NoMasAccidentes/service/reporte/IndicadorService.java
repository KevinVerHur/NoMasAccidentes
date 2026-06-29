package com.example.NoMasAccidentes.service.reporte;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.reporte.AccidentabilidadMensualResponse;
import com.example.NoMasAccidentes.dto.reporte.RendimientoProfesionalResponse;
import com.example.NoMasAccidentes.model.capacitacion.EstadoCapacitacion;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.repository.asesoria.AccidenteRepository;
import com.example.NoMasAccidentes.repository.asesoria.AsesoriaRepository;
import com.example.NoMasAccidentes.repository.capacitacion.CapacitacionRepository;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.repository.visita.VisitaRepository;
import com.example.NoMasAccidentes.service.cliente.ClienteService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Indicadores de gestión derivados de los módulos operativos:
 * accidentabilidad por cliente (RF40) y rendimiento por profesional (RF41).
 * Agregación de solo lectura; no persiste entidades propias.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndicadorService {

    private final ClienteRepository clienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final AccidenteRepository accidenteRepository;
    private final VisitaRepository visitaRepository;
    private final AsesoriaRepository asesoriaRepository;
    private final CapacitacionRepository capacitacionRepository;
    private final ClienteService clienteService;

    /** Serie mensual (12 meses) de accidentabilidad de un cliente para el año (RF40). */
    public List<AccidentabilidadMensualResponse> accidentabilidadAnual(Long idCliente, int anio) {
        validarAnio(anio);
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", idCliente));
        Integer trabajadores = cliente.getCantidadTrabajadores();

        List<AccidentabilidadMensualResponse> serie = new ArrayList<>(12);
        for (int mes = 1; mes <= 12; mes++) {
            LocalDate desde = LocalDate.of(anio, mes, 1);
            LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());

            long accidentes = accidenteRepository
                    .countByAsesoriaClienteIdAndFechaOcurrenciaBetween(idCliente, desde, hasta);
            long diasPerdidos = accidenteRepository
                    .sumDiasPerdidosByClienteAndFechaOcurrenciaBetween(idCliente, desde, hasta);
            Double tasa = (trabajadores != null && trabajadores > 0)
                    ? (accidentes * 100.0) / trabajadores
                    : null;

            serie.add(new AccidentabilidadMensualResponse(mes, accidentes, diasPerdidos, tasa));
        }
        return serie;
    }

    /** Accidentabilidad anual del cliente autenticado (portal cliente, RF40). */
    public List<AccidentabilidadMensualResponse> miAccidentabilidad(String emailUsuario, int anio) {
        Long idCliente = clienteService.clienteAutenticado(emailUsuario).getId();
        return accidentabilidadAnual(idCliente, anio);
    }

    /** Rendimiento de cada profesional en el periodo (RF41). */
    public List<RendimientoProfesionalResponse> rendimientoProfesional(int mes, int anio) {
        validarPeriodo(mes, anio);
        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        LocalDateTime desdeDt = desde.atStartOfDay();
        LocalDateTime hastaDt = hasta.atTime(LocalTime.MAX);

        List<RendimientoProfesionalResponse> resultado = new ArrayList<>();
        for (Profesional p : profesionalRepository.findAll()) {
            long visitasRealizadas = visitaRepository
                    .countByProfesionalIdAndEstadoAndFechaFinBetween(p.getId(), EstadoVisita.REALIZADA, desdeDt, hastaDt);
            long visitasProgramadas = visitaRepository
                    .countByProfesionalIdAndFechaProgramadaBetween(p.getId(), desde, hasta);
            long asesorias = asesoriaRepository
                    .countByProfesionalIdAndFechaAtencionBetween(p.getId(), desde, hasta);
            long capacitaciones = capacitacionRepository
                    .countByRelatorIdAndEstadoAndFechaRealizacionBetween(p.getId(), EstadoCapacitacion.REALIZADA, desde, hasta);
            Double cumplimiento = visitasProgramadas > 0
                    ? (visitasRealizadas * 100.0) / visitasProgramadas
                    : null;

            resultado.add(new RendimientoProfesionalResponse(
                    p.getId(),
                    p.getUsuario().getNombre() + " " + p.getUsuario().getApellido(),
                    visitasRealizadas, visitasProgramadas, asesorias, capacitaciones, cumplimiento));
        }
        return resultado;
    }

    private void validarPeriodo(int mes, int anio) {
        if (mes < 1 || mes > 12) {
            throw new ConflictoNegocioException("El mes debe estar entre 1 y 12");
        }
        validarAnio(anio);
    }

    private void validarAnio(int anio) {
        if (anio < 2000 || anio > LocalDate.now().getYear() + 1) {
            throw new ConflictoNegocioException("El año no es válido");
        }
    }
}
