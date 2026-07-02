package com.example.NoMasAccidentes.service.actividad;

import com.example.NoMasAccidentes.common.*;
import com.example.NoMasAccidentes.dto.actividad.*;
import com.example.NoMasAccidentes.model.actividad.*;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.repository.actividad.ActividadPreventivaRepository;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
import com.example.NoMasAccidentes.service.empresa.EmpresaService;
import com.example.NoMasAccidentes.service.usuario.CorreoService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActividadPreventivaService {


    private final ActividadPreventivaRepository repository;
    private final EmpresaRepository empresaRepository;
    private final EmpresaService empresaService;
    private final ActividadPreventivaMapper mapper;
    private final CorreoService correoService;

    @Transactional
    public ActividadPreventivaResponse crear(CrearActividadPreventivaRequest r) {
    validarFechas(r.fechaPlanificada(), r.fechaCompromiso());

        Empresa empresa = empresaRepository.findById(r.idEmpresa())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa", r.idEmpresa()));

        ActividadPreventiva actividad = ActividadPreventiva.builder()
                .empresa(empresa)
                .titulo(r.titulo())
                .descripcion(r.descripcion())
                .normativa(r.normativa())
                .responsable(r.responsable())
                .fechaPlanificada(r.fechaPlanificada())
                .fechaCompromiso(r.fechaCompromiso())
                .observaciones(r.observaciones())
                .build();

        return mapper.toResponse(repository.save(actividad));
    }


    @Transactional
    public Page<ActividadPreventivaResponse> listar(Long idEmpresa, EstadoActividadPreventiva estado, Pageable pageable){
        repository.marcarVencidas(LocalDate.now());

        Page<ActividadPreventiva> page;
        if(idEmpresa != null && estado != null){
            page = repository.findByEmpresaIdAndEstado(idEmpresa, estado, pageable);
        } else if (idEmpresa != null){
            page = repository.findByEmpresaId(idEmpresa, pageable);
        } else if (estado != null){
            page = repository.findByEstado(estado, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        return page.map(mapper::toResponse);
    }

    @Transactional
    public ActividadPreventivaResponse obtener(Long id){
        repository.marcarVencidas(LocalDate.now());
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public ActividadPreventivaResponse actualizar(Long id, ActualizarActividadPreventivaRequest r){
        validarFechas(r.fechaPlanificada(), r.fechaCompromiso());

        ActividadPreventiva a = buscar(id);
        a.setTitulo(r.titulo());
        a.setDescripcion(r.descripcion());
        a.setNormativa(r.normativa());
        a.setResponsable(r.responsable());
        a.setFechaPlanificada(r.fechaPlanificada());
        a.setFechaCompromiso(r.fechaCompromiso());
        a.setObservaciones(r.observaciones());

        if(r.estado() != null) {
            aplicarEstado(a, r.estado(), r.observaciones());
        }

        return mapper.toResponse(a);
    }

    @Transactional
    public ActividadPreventivaResponse cambiarEstado(Long id, CambiarEstadoActividadRequest r){
        ActividadPreventiva a = buscar(id);
        aplicarEstado(a, r.estado(), r.observaciones());
        return mapper.toResponse(a);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    @Transactional
    public List<ActividadPreventivaResponse> misActividades(String email) {
        repository.marcarVencidas(LocalDate.now());
        Long idEmpresa = empresaService.empresaAutenticada(email).getId();
        return repository.findByEmpresaIdOrderByFechaCompromisoAsc(idEmpresa)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }



    @Transactional
    public int procesarVencidasYAlertar() {
        List<ActividadPreventiva> vencidas = repository.findVencidasSinAlerta(LocalDate.now());

        vencidas.forEach(actividad -> {
            actividad.setEstado(EstadoActividadPreventiva.VENCIDA);
            correoService.enviarAlertaActividadPreventivaVencida(
                    actividad.getEmpresa().getRazonSocial(),
                    actividad.getTitulo(),
                    actividad.getNormativa(),
                    actividad.getResponsable(),
                    actividad.getFechaCompromiso().toString()
            );
            actividad.setAlertaEnviada(true);
        });

        return vencidas.size();
    }

    @Transactional
    private ActividadPreventiva buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad preventiva", id));
    }

    private void validarFechas(LocalDate planificada, LocalDate compromiso){
        if(compromiso.isBefore(planificada)) {
            throw new ConflictoNegocioException("La fecha compromiso no puede ser anterion a la fecha planificada");
        }
    }

    private void aplicarEstado(ActividadPreventiva a, EstadoActividadPreventiva estado, String observaciones){
        if(a.getEstado() == EstadoActividadPreventiva.CUMPLIDA && estado != EstadoActividadPreventiva.CUMPLIDA){
            throw new ConflictoNegocioException("No se puede reabrir una actividad ya cumplida.");
        }

        a.setEstado(estado);
        a.setObservaciones(observaciones);

        if (estado == EstadoActividadPreventiva.CUMPLIDA){
            a.setFechaCumplimiento(LocalDate.now());
        } else {
            a.setFechaCumplimiento(null);
        }
    }
}
