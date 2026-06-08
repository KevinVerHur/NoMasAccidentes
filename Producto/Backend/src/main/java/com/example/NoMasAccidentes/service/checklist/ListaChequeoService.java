package com.example.NoMasAccidentes.service.checklist;

import com.example.NoMasAccidentes.common.LimiteModificacionesExcedidoException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.checklist.ActualizarListaChequeoRequest;
import com.example.NoMasAccidentes.dto.checklist.CrearListaChequeoRequest;
import com.example.NoMasAccidentes.dto.checklist.ListaChequeoMapper;
import com.example.NoMasAccidentes.dto.checklist.ListaChequeoResponse;
import com.example.NoMasAccidentes.model.checklist.ListaChequeo;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.repository.checklist.ListaChequeoRepository;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import java.time.Year;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestion de listas de chequeo por cliente.
 * RF16/RF17: maximo 2 modificaciones por anio.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ListaChequeoService {

    private final ListaChequeoRepository listaChequeoRepository;
    private final ClienteRepository clienteRepository;
    private final ListaChequeoMapper listaChequeoMapper;

    @Transactional
    public ListaChequeoResponse crear(CrearListaChequeoRequest request) {
        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", request.idCliente()));

        ListaChequeo lista = ListaChequeo.builder()
                .cliente(cliente)
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .contenido(request.contenido())
                .anioControl(Year.now().getValue())
                .modificacionesAnio(0)
                .activo(true)
                .build();

        ListaChequeo guardada = listaChequeoRepository.save(lista);
        log.info("Lista de chequeo creada id={} cliente={} (RF16)", guardada.getId(), cliente.getId());

        return listaChequeoMapper.toResponse(guardada);
    }

    public List<ListaChequeoResponse> listarPorCliente(Long idCliente) {
        if (!clienteRepository.existsById(idCliente)) {
            throw new RecursoNoEncontradoException("Cliente", idCliente);
        }

        return listaChequeoRepository.findByClienteIdOrderByFechaActualizacionDesc(idCliente)
                .stream()
                .map(this::actualizarContadorParaResponse)
                .map(listaChequeoMapper::toResponse)
                .toList();
    }

    public ListaChequeoResponse obtenerPorId(Long id) {
        return listaChequeoMapper.toResponse(actualizarContadorParaResponse(buscarOFallar(id)));
    }

    @Transactional
    public ListaChequeoResponse actualizar(Long id, ActualizarListaChequeoRequest request) {
        ListaChequeo lista = buscarOFallar(id);
        int anioActual = Year.now().getValue();

        lista.reiniciarContadorSiCambioAnio(anioActual);

        if (!lista.puedeModificarse()) {
            throw new LimiteModificacionesExcedidoException(
                    "La lista de chequeo del cliente id=%s ya alcanzo el limite de %s modificaciones para el anio %s"
                            .formatted(
                                    lista.getCliente().getId(),
                                    ListaChequeo.LIMITE_MODIFICACIONES_ANUALES,
                                    lista.getAnioControl()
                            )
            );
        }

        lista.setNombre(request.nombre());
        lista.setDescripcion(request.descripcion());
        lista.setContenido(request.contenido());
        lista.registrarModificacion();

        log.info(
                "Lista de chequeo actualizada id={} cliente={} modificaciones={}/{} anio={} (RF17)",
                lista.getId(),
                lista.getCliente().getId(),
                lista.getModificacionesAnio(),
                ListaChequeo.LIMITE_MODIFICACIONES_ANUALES,
                lista.getAnioControl()
        );

        return listaChequeoMapper.toResponse(lista);
    }

    @Transactional
    public void eliminar(Long id) {
        ListaChequeo lista = buscarOFallar(id);
        listaChequeoRepository.delete(lista);
        log.info("Lista de chequeo eliminada soft id={} cliente={} (RNF14)", id, lista.getCliente().getId());
    }

    private ListaChequeo buscarOFallar(Long id) {
        return listaChequeoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista de chequeo", id));
    }

    private ListaChequeo actualizarContadorParaResponse(ListaChequeo lista) {
        lista.reiniciarContadorSiCambioAnio(Year.now().getValue());
        return lista;
    }
}