package com.example.NoMasAccidentes.service.visita;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.LimiteModificacionesExcedidoException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.visita.ActualizarListaChequeoRequest;
import com.example.NoMasAccidentes.dto.visita.CrearListaChequeoRequest;
import com.example.NoMasAccidentes.dto.visita.ItemChequeoRequest;
import com.example.NoMasAccidentes.dto.visita.ListaChequeoMapper;
import com.example.NoMasAccidentes.dto.visita.ListaChequeoResponse;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.visita.ItemChequeo;
import com.example.NoMasAccidentes.model.visita.ListaChequeo;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
import com.example.NoMasAccidentes.repository.visita.ListaChequeoRepository;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de listas de chequeo por empresa (RF16) con control de
 * modificaciones anuales (RF17: máximo 2 veces al año).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ListaChequeoService {

    private static final int MAX_MODIFICACIONES_ANIO = 2;

    /** Ítems base con que se siembra la lista al dar de alta una empresa (RF16). */
    private static final List<String[]> ITEMS_POR_DEFECTO = List.of(
            new String[]{"Uso de elementos de protección personal (EPP)", "EPP"},
            new String[]{"Señalización de seguridad visible y vigente", "Señalización"},
            new String[]{"Extintores operativos y con carga vigente", "Emergencias"},
            new String[]{"Vías de evacuación despejadas", "Emergencias"},
            new String[]{"Orden y limpieza en áreas de trabajo", "Condiciones generales"},
            new String[]{"Instalaciones eléctricas sin riesgos visibles", "Condiciones generales"}
    );

    private final ListaChequeoRepository listaChequeoRepository;
    private final EmpresaRepository empresaRepository;
    private final ListaChequeoMapper listaChequeoMapper;

    /** Crea la lista de chequeo de la empresa (RF16). Una sola por empresa. */
    @Transactional
    public ListaChequeoResponse crear(CrearListaChequeoRequest request) {
        Empresa empresa = empresaRepository.findById(request.idEmpresa())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa", request.idEmpresa()));
        if (listaChequeoRepository.existsByEmpresaId(empresa.getId())) {
            throw new ConflictoNegocioException("La empresa ya tiene una lista de chequeo (RF16)");
        }

        ListaChequeo lista = ListaChequeo.builder()
                .empresa(empresa)
                .nombre(request.nombre())
                .anioVigente(Year.now().getValue())
                .cambiosRealizadosAnio(0)
                .build();
        agregarItems(lista, request.items());

        ListaChequeo guardada = listaChequeoRepository.save(lista);
        log.info("Lista de chequeo creada id={} empresa={} (RF16)", guardada.getId(), empresa.getId());
        return listaChequeoMapper.toResponse(guardada);
    }

    /**
     * Siembra una lista de chequeo base para una empresa recién creada (RF16),
     * de modo que se puedan planificar visitas sin un paso manual previo.
     * Idempotente: no hace nada si la empresa ya tiene lista.
     */
    @Transactional
    public void crearPorDefecto(Empresa empresa) {
        if (listaChequeoRepository.existsByEmpresaId(empresa.getId())) {
            return;
        }
        ListaChequeo lista = ListaChequeo.builder()
                .empresa(empresa)
                .nombre("Lista de chequeo general")
                .anioVigente(Year.now().getValue())
                .cambiosRealizadosAnio(0)
                .build();

        int orden = 1;
        for (String[] base : ITEMS_POR_DEFECTO) {
            lista.getItems().add(ItemChequeo.builder()
                    .listaChequeo(lista)
                    .descripcion(base[0])
                    .categoria(base[1])
                    .obligatorio(true)
                    .orden(orden++)
                    .build());
        }

        listaChequeoRepository.save(lista);
        log.info("Lista de chequeo por defecto creada para empresa={} (RF16)", empresa.getId());
    }

    /** Modifica la lista de chequeo respetando el límite anual (RF17). */
    @Transactional
    public ListaChequeoResponse modificar(Long id, ActualizarListaChequeoRequest request) {
        ListaChequeo lista = buscarOFallar(id);
        int anioActual = Year.now().getValue();

        // Reiniciar el contador si cambió el año vigente (RF17).
        if (lista.getAnioVigente() == null || lista.getAnioVigente() != anioActual) {
            lista.setAnioVigente(anioActual);
            lista.setCambiosRealizadosAnio(0);
        }
        if (lista.getCambiosRealizadosAnio() >= MAX_MODIFICACIONES_ANIO) {
            throw new LimiteModificacionesExcedidoException(
                    "La lista de chequeo ya alcanzó el máximo de " + MAX_MODIFICACIONES_ANIO
                            + " modificaciones en " + anioActual + " (RF17)");
        }

        lista.setNombre(request.nombre());
        lista.getItems().clear();
        agregarItems(lista, request.items());
        lista.setCambiosRealizadosAnio(lista.getCambiosRealizadosAnio() + 1);
        lista.setFechaUltimaModificacion(LocalDate.now());

        log.info("Lista de chequeo modificada id={} cambios={}/{} año={} (RF17)",
                id, lista.getCambiosRealizadosAnio(), MAX_MODIFICACIONES_ANIO, anioActual);
        return listaChequeoMapper.toResponse(lista);
    }

    public ListaChequeoResponse obtenerPorId(Long id) {
        return listaChequeoMapper.toResponse(buscarOFallar(id));
    }

    public ListaChequeoResponse obtenerPorEmpresa(Long idEmpresa) {
        return listaChequeoMapper.toResponse(listaChequeoRepository.findByEmpresaId(idEmpresa)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Lista de chequeo no encontrada para la empresa id=" + idEmpresa)));
    }

    private void agregarItems(ListaChequeo lista, List<ItemChequeoRequest> items) {
        if (items == null) {
            return;
        }
        for (ItemChequeoRequest it : items) {
            ItemChequeo item = ItemChequeo.builder()
                    .listaChequeo(lista)
                    .descripcion(it.descripcion())
                    .categoria(it.categoria())
                    .obligatorio(it.obligatorio() == null || it.obligatorio())
                    .orden(it.orden())
                    .build();
            lista.getItems().add(item);
        }
    }

    private ListaChequeo buscarOFallar(Long id) {
        return listaChequeoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista de chequeo", id));
    }
}
