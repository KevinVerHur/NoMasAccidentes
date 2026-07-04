package com.example.NoMasAccidentes.service.asesoria;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.asesoria.InformeAsesoriaMapper;
import com.example.NoMasAccidentes.dto.asesoria.InformeAsesoriaResponse;
import com.example.NoMasAccidentes.model.asesoria.Accidente;
import com.example.NoMasAccidentes.model.asesoria.Asesoria;
import com.example.NoMasAccidentes.model.asesoria.EstadoAsesoria;
import com.example.NoMasAccidentes.model.asesoria.Fiscalizacion;
import com.example.NoMasAccidentes.model.asesoria.PropuestaMejora;
import com.example.NoMasAccidentes.model.informe.EstadoInforme;
import com.example.NoMasAccidentes.model.informe.Informe;
import com.example.NoMasAccidentes.repository.asesoria.AccidenteRepository;
import com.example.NoMasAccidentes.repository.asesoria.AsesoriaRepository;
import com.example.NoMasAccidentes.repository.asesoria.FiscalizacionRepository;
import com.example.NoMasAccidentes.repository.asesoria.PropuestaMejoraRepository;
import com.example.NoMasAccidentes.repository.informe.InformeRepository;
import com.example.NoMasAccidentes.service.empresa.EmpresaService;
import com.example.NoMasAccidentes.service.informe.AlmacenamientoInformeService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Genera y consulta el informe de una asesoría (RF15 para asesorías). Reutiliza
 * la entidad/tabla {@link Informe} (con {@code visita} nula e {@code idAsesoria}
 * seteado) y la infraestructura de almacenamiento de PDFs. La descarga del PDF
 * se sirve por el endpoint genérico {@code /api/informes/{id}/descarga}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InformeAsesoriaService {

    private final InformeRepository informeRepository;
    private final AsesoriaRepository asesoriaRepository;
    private final AccidenteRepository accidenteRepository;
    private final FiscalizacionRepository fiscalizacionRepository;
    private final PropuestaMejoraRepository propuestaMejoraRepository;
    private final InformeAsesoriaPdfService pdfService;
    private final AlmacenamientoInformeService almacenamiento;
    private final InformeAsesoriaMapper informeAsesoriaMapper;
    private final EmpresaService empresaService;

    /**
     * Genera (o regenera) el informe PDF de una asesoría atendida (RF15, RF25).
     * Solo se permite sobre asesorías EN_PROCESO o CERRADA.
     */
    @Transactional
    public InformeAsesoriaResponse generarParaAsesoria(Long idAsesoria) {
        Asesoria asesoria = asesoriaRepository.findById(idAsesoria)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asesoría", idAsesoria));
        if (asesoria.getEstado() == EstadoAsesoria.SOLICITADA
                || asesoria.getEstado() == EstadoAsesoria.CANCELADA) {
            throw new ConflictoNegocioException(
                    "Solo se puede generar el informe de una asesoría atendida (EN_PROCESO o CERRADA)");
        }

        Informe informe = informeRepository.findByIdAsesoria(idAsesoria).orElseGet(Informe::new);

        List<Accidente> accidentes =
                accidenteRepository.findByAsesoriaIdOrderByFechaOcurrenciaDesc(idAsesoria);
        List<Fiscalizacion> fiscalizaciones =
                fiscalizacionRepository.findByAsesoriaIdOrderByFechaDesc(idAsesoria);
        List<PropuestaMejora> propuestas = informe.getId() != null
                ? propuestaMejoraRepository.findByInformeIdOrderByFechaPropuestaDesc(informe.getId())
                : List.of();

        byte[] pdf = pdfService.generar(asesoria, accidentes, fiscalizaciones, propuestas);
        String nombreArchivo = "informe-asesoria-" + idAsesoria + "-" + UUID.randomUUID() + ".pdf";
        String clave = almacenamiento.guardar("informes-asesoria", asesoria.getEmpresa().getId(), nombreArchivo, pdf);

        informe.setVisita(null);
        informe.setIdAsesoria(idAsesoria);
        informe.setFechaEmision(LocalDate.now());
        informe.setEstado(EstadoInforme.GENERADO);
        informe.setHallazgos(asesoria.getMotivo());
        informe.setContenido("Informe de asesoría (" + asesoria.getTipo() + ") para "
                + asesoria.getEmpresa().getRazonSocial()
                + " — solicitada el " + asesoria.getFechaSolicitud());
        informe.setUrlPdf(clave);

        Informe guardado = informeRepository.save(informe);
        log.info("Informe de asesoría generado id={} asesoria={} (RF15, RF25)",
                guardado.getId(), idAsesoria);
        return informeAsesoriaMapper.toResponse(guardado);
    }

    public InformeAsesoriaResponse obtenerPorAsesoria(Long idAsesoria) {
        return informeAsesoriaMapper.toResponse(informeRepository.findByIdAsesoria(idAsesoria)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Informe no encontrado para la asesoría id=" + idAsesoria)));
    }

    /**
     * Informes de asesoría de la empresa del cliente autenticado (RF15, RF07).
     * Solo lectura; la descarga se sirve por {@code /api/mis-informes/{id}/descarga}.
     */
    public List<InformeAsesoriaResponse> misInformesAsesoria(String emailUsuario) {
        Long idEmpresa = empresaService.empresaAutenticada(emailUsuario).getId();
        List<Long> idsAsesoria = asesoriaRepository
                .findByEmpresaIdOrderByFechaSolicitudDesc(idEmpresa)
                .stream().map(Asesoria::getId).toList();
        if (idsAsesoria.isEmpty()) {
            return List.of();
        }
        return informeRepository.findByIdAsesoriaInOrderByFechaEmisionDesc(idsAsesoria)
                .stream().map(informeAsesoriaMapper::toResponse).toList();
    }
}
