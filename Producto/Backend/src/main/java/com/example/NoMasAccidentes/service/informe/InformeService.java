package com.example.NoMasAccidentes.service.informe;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.informe.InformeMapper;
import com.example.NoMasAccidentes.dto.informe.InformeResponse;
import com.example.NoMasAccidentes.model.informe.EstadoInforme;
import com.example.NoMasAccidentes.model.informe.Informe;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.model.visita.Visita;
import com.example.NoMasAccidentes.repository.informe.InformeRepository;
import com.example.NoMasAccidentes.repository.visita.VisitaRepository;
import com.example.NoMasAccidentes.service.cliente.ClienteService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generación y consulta de informes posteriores a la visita (RF15).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InformeService {

    private final InformeRepository informeRepository;
    private final VisitaRepository visitaRepository;
    private final InformePdfService pdfService;
    private final AlmacenamientoInformeService almacenamiento;
    private final InformeMapper informeMapper;
    private final ClienteService clienteService;

    /**
     * Genera (o regenera) el informe PDF de una visita realizada (RF15).
     * Solo se permite sobre visitas en estado REALIZADA.
     */
    @Transactional
    public InformeResponse generarParaVisita(Long idVisita) {
        Visita visita = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new RecursoNoEncontradoException("Visita", idVisita));
        if (visita.getEstado() != EstadoVisita.REALIZADA) {
            throw new ConflictoNegocioException(
                    "Solo se puede generar el informe de una visita REALIZADA (RF15)");
        }

        byte[] pdf = pdfService.generar(visita);
        String nombreArchivo = "informe-visita-" + idVisita + "-" + UUID.randomUUID() + ".pdf";
        String clave = almacenamiento.guardar(nombreArchivo, pdf);

        Informe informe = informeRepository.findByVisitaId(idVisita).orElseGet(Informe::new);
        informe.setVisita(visita);
        informe.setFechaEmision(LocalDate.now());
        informe.setEstado(EstadoInforme.GENERADO);
        informe.setHallazgos(visita.getObservaciones());
        informe.setContenido("Informe de la visita a " + visita.getCliente().getRazonSocial()
                + " del " + visita.getFechaProgramada());
        informe.setUrlPdf(clave);

        Informe guardado = informeRepository.save(informe);
        log.info("Informe generado id={} visita={} (RF15)", guardado.getId(), idVisita);
        return informeMapper.toResponse(guardado);
    }

    public InformeResponse obtenerPorVisita(Long idVisita) {
        return informeMapper.toResponse(informeRepository.findByVisitaId(idVisita)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Informe no encontrado para la visita id=" + idVisita)));
    }

    /** Devuelve el contenido del PDF para descarga. */
    public byte[] descargarPdf(Long idInforme) {
        return descargarPdf(buscarConArchivo(idInforme));
    }

    // ---- Portal cliente (solo lectura de lo propio) ----

    /** Lista los informes de las visitas del cliente autenticado (RF15). */
    public List<InformeResponse> listarMisInformes(String emailUsuario) {
        Long idCliente = clienteService.clienteAutenticado(emailUsuario).getId();
        return informeRepository.findByVisitaClienteIdOrderByFechaEmisionDesc(idCliente)
                .stream().map(informeMapper::toResponse).toList();
    }

    /** Descarga un informe verificando que pertenezca al cliente autenticado. */
    public byte[] descargarMiPdf(Long idInforme, String emailUsuario) {
        Long idCliente = clienteService.clienteAutenticado(emailUsuario).getId();
        Informe informe = buscarConArchivo(idInforme);
        Cliente clienteInforme = informe.getVisita() != null ? informe.getVisita().getCliente() : null;
        if (clienteInforme == null || !clienteInforme.getId().equals(idCliente)) {
            // No revelar existencia de informes ajenos.
            throw new RecursoNoEncontradoException("Informe", idInforme);
        }
        return descargarPdf(informe);
    }

    private Informe buscarConArchivo(Long idInforme) {
        Informe informe = informeRepository.findById(idInforme)
                .orElseThrow(() -> new RecursoNoEncontradoException("Informe", idInforme));
        if (informe.getUrlPdf() == null) {
            throw new ConflictoNegocioException("El informe no tiene un archivo asociado");
        }
        return informe;
    }

    private byte[] descargarPdf(Informe informe) {
        return almacenamiento.descargar(informe.getUrlPdf());
    }
}
