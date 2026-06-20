package com.example.NoMasAccidentes.controller.capacitacion;

import com.example.NoMasAccidentes.dto.capacitacion.*;
import com.example.NoMasAccidentes.service.capacitacion.CapacitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints REST para gestión de capacitaciones.
 *
 *  POST   /api/capacitaciones
 *      → Programar nueva capacitación (valida 15 días RF-CAP1, flag extra RF-CAP4)
 *
 *  GET    /api/capacitaciones
 *      → Listar todas paginadas (ADMIN)
 *
 *  GET    /api/capacitaciones/{id}
 *      → Obtener una capacitación con sus asistentes
 *
 *  GET    /api/capacitaciones/cliente/{idCliente}
 *      → Capacitaciones de un cliente
 *
 *  GET    /api/capacitaciones/cliente/{idCliente}/extras
 *      → Solo las capacitaciones extra del cliente (RF-CAP4)
 *
 *  GET    /api/capacitaciones/relator/{idRelator}
 *      → Capacitaciones que dicta un relator
 *
 *  POST   /api/capacitaciones/{id}/asistentes
 *      → Inscribir asistentes (N:M, valida cupos, RF-CAP2)
 *
 *  POST   /api/capacitaciones/{id}/asistentes/{idAsistente}/confirmar
 *      → Confirmar asistencia (RF-CAP3)
 *
 *  PATCH  /api/capacitaciones/{id}/asistentes/{idAsistente}/asistio
 *      → Registrar asistencia efectiva (presente/ausente)
 *
 *  PATCH  /api/capacitaciones/{id}/iniciar
 *      → Iniciar la capacitación (PROGRAMADA → EN_CURSO)
 *
 *  PATCH  /api/capacitaciones/{id}/finalizar
 *      → Finalizar la capacitación (→ REALIZADA). Requiere ≥1 asistente con asistio=true.
 *
 *  PATCH  /api/capacitaciones/{id}/cancelar
 *      → Cancelar una capacitación
 */
@RestController
@RequestMapping("/api/capacitaciones")
@RequiredArgsConstructor
@Tag(name = "Capacitaciones", description = "Gestión de capacitaciones y asistencia (RF-CAP)")
public class CapacitacionController {

    private final CapacitacionService capacitacionService;

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Operation(summary = "Listar todas las capacitaciones paginadas")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL','CLIENTE')")
    public Page<CapacitacionResponse> listar(Pageable pageable) {
        return capacitacionService.listar(pageable);
    }

    @Operation(summary = "Obtener capacitación por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL','CLIENTE')")
    public CapacitacionResponse obtener(@PathVariable Long id) {
        return capacitacionService.obtener(id);
    }

    @Operation(summary = "Listar capacitaciones de un cliente")
    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL','CLIENTE')")
    public List<CapacitacionResponse> listarPorCliente(@PathVariable Long idCliente) {
        return capacitacionService.listarPorCliente(idCliente);
    }

    @Operation(
        summary = "Listar capacitaciones extra de un cliente",
        description = "Retorna solo las capacitaciones con esCapacitacionExtra=true " +
                      "que generan costo adicional al plan (RF-CAP4)."
    )
    @GetMapping("/cliente/{idCliente}/extras")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<CapacitacionResponse> listarExtras(@PathVariable Long idCliente) {
        return capacitacionService.listarExtras(idCliente);
    }

    @Operation(summary = "Listar capacitaciones que dicta un relator")
    @GetMapping("/relator/{idRelator}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<CapacitacionResponse> listarPorRelator(@PathVariable Long idRelator) {
        return capacitacionService.listarPorRelator(idRelator);
    }

    // ── Comandos ──────────────────────────────────────────────────────────────

    @Operation(
        summary = "Programar una nueva capacitación",
        description = "Crea una capacitación con los datos del formulario. " +
                      "La fecha debe estar al menos 15 días en el futuro (RF-CAP1). " +
                      "esCapacitacionExtra=true → genera costo adicional al plan (RF-CAP4)."
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROFESIONAL')")
    public ResponseEntity<CapacitacionResponse> crear(
            @Valid @RequestBody CrearCapacitacionRequest request) {

        CapacitacionResponse creada = capacitacionService.crear(request);

        return ResponseEntity
                .created(URI.create("/api/capacitaciones/" + creada.id()))
                .body(creada);
    }

    @Operation(
        summary = "Inscribir asistentes a una capacitación",
        description = "Crea registros de asistencia (N:M). Valida cupos disponibles " +
                      "y que los asistentes pertenezcan al cliente (RF-CAP2)."
    )
    @PostMapping("/{id}/asistentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL','CLIENTE')")  // ← agregar CLIENTE
    public CapacitacionResponse inscribirAsistentes(
            @PathVariable Long id,
            @Valid @RequestBody InscribirAsistentesRequest request) {

        return capacitacionService.inscribirAsistentes(id, request);
    }

    @Operation(
        summary = "Confirmar asistencia",
        description = "El asistente confirma su participación. Solo se puede confirmar " +
                      "una vez y si la capacitación no está cancelada ni realizada (RF-CAP3)."
    )
    @PostMapping("/{id}/asistentes/{idAsistente}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL', 'CLIENTE')")  // ← agregar CLIENTE
    public AsistenciaResponse confirmarAsistencia(
            @PathVariable Long id,
            @PathVariable Long idAsistente,
            @RequestBody(required = false) ConfirmarAsistenciaRequest request) {

        return capacitacionService.confirmarAsistencia(id, idAsistente, request);
    }
    @Operation(summary = "Registrar asistencia efectiva (presente/ausente)")
@PatchMapping("/{id}/asistentes/{idAsistente}/asistio")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
public AsistenciaResponse registrarAsistencia(
        @PathVariable Long id,
        @PathVariable Long idAsistente,
        @RequestParam boolean asistio) {
    return capacitacionService.registrarAsistencia(id, idAsistente, asistio);
        }
    @Operation(summary = "Iniciar la capacitación: pasa a EN_CURSO")
    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public CapacitacionResponse iniciar(@PathVariable Long id) {
        return capacitacionService.iniciar(id);
    }

    @Operation(
        summary = "Finalizar la capacitación: pasa a REALIZADA",
        description = "Requiere que al menos un asistente tenga registrada su asistencia " +
                      "efectiva (asistio=true) mediante el endpoint /asistentes/{idAsistente}/asistio."
    )
    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public CapacitacionResponse finalizar(@PathVariable Long id) {
        return capacitacionService.finalizar(id);
    }

    @Operation(summary = "Cancelar una capacitación")
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public CapacitacionResponse cancelar(@PathVariable Long id) {
        return capacitacionService.cancelar(id);
    }
}