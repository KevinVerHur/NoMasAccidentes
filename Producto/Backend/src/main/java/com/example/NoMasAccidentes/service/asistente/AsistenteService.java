package com.example.NoMasAccidentes.service.asistente;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.asistente.AsistenteRequest;
import com.example.NoMasAccidentes.dto.asistente.AsistenteResponse;
import com.example.NoMasAccidentes.model.asistente.Asistente;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.repository.asistente.AsistenteRepository;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AsistenteService {

    private final AsistenteRepository asistenteRepository;
    private final EmpresaRepository   empresaRepository;

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<AsistenteResponse> listarPorEmpresa(Long idEmpresa) {
        if (!empresaRepository.existsById(idEmpresa)) {
            throw new RecursoNoEncontradoException("Empresa", idEmpresa);
        }
        return asistenteRepository.findByEmpresaId(idEmpresa)
                .stream().map(this::toResponse).toList();
    }

    public AsistenteResponse obtener(Long id) {
        return toResponse(buscarOFallar(id));
    }

    // ── Comandos ──────────────────────────────────────────────────────────────

    @Transactional
    public AsistenteResponse crear(AsistenteRequest request) {
        if (asistenteRepository.existsByRut(request.rut())) {
            throw new ConflictoNegocioException(
                    "Ya existe un asistente registrado con el RUT " + request.rut());
        }

        Empresa empresa = empresaRepository.findById(request.idEmpresa())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa", request.idEmpresa()));

        Asistente asistente = Asistente.builder()
                .empresa(empresa)
                .rut(request.rut())
                .nombre(request.nombre())
                .apellidos(request.apellidos())
                .cargo(request.cargo())
                .area(request.area())
                .email(request.email())
                .build();

        Asistente guardado = asistenteRepository.save(asistente);
        log.info("Asistente creado id={} rut={} empresa={}", guardado.getId(), guardado.getRut(), empresa.getRazonSocial());
        return toResponse(guardado);
    }

    @Transactional
    public AsistenteResponse editar(Long id, AsistenteRequest request) {
        Asistente asistente = buscarOFallar(id);

        if (!asistente.getRut().equals(request.rut())
                && asistenteRepository.existsByRut(request.rut())) {
            throw new ConflictoNegocioException(
                    "Ya existe un asistente con el RUT " + request.rut());
        }

        asistente.setRut(request.rut());
        asistente.setNombre(request.nombre());
        asistente.setApellidos(request.apellidos());
        asistente.setCargo(request.cargo());
        asistente.setArea(request.area());
        asistente.setEmail(request.email());

        log.info("Asistente editado id={}", id);
        return toResponse(asistente);
    }

    @Transactional
    public void eliminar(Long id) {
        Asistente asistente = buscarOFallar(id);
        asistenteRepository.delete(asistente); // soft delete via @SQLDelete
        log.info("Asistente eliminado id={}", id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Asistente buscarOFallar(Long id) {
        return asistenteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asistente", id));
    }

    private AsistenteResponse toResponse(Asistente a) {
        return new AsistenteResponse(
                a.getId(),
                a.getEmpresa().getId(),
                a.getEmpresa().getRazonSocial(),
                a.getRut(),
                a.getNombre(),
                a.getApellidos(),
                a.getNombre() + " " + a.getApellidos(),
                a.getCargo(),
                a.getArea(),
                a.getEmail()
        );
    }
}
