package com.example.NoMasAccidentes.controller.capacitacion;

import com.example.NoMasAccidentes.dto.capacitacion.*;
import com.example.NoMasAccidentes.service.capacitacion.ActaCapacitacionPdfService;
import com.example.NoMasAccidentes.service.capacitacion.CapacitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/capacitaciones")
@RequiredArgsConstructor
@Tag(name = "Capacitaciones", description = "Gestión de capacitaciones y asistencia (RF-CAP)")
public class CapacitacionController {

    private final CapacitacionService capacitacionService;
    private final ActaCapacitacionPdfService actaCapacitacionPdfService;

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

    @Operation(summary = "Generar acta de capacitación en PDF")
    @GetMapping("/{id}/acta-pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL','CLIENTE')")
    public ResponseEntity<byte[]> generarActaPdf(@PathVariable Long id) {

        byte[] pdf = actaCapacitacionPdfService.generarActaPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("acta-capacitacion-" + id + ".pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @Operation(summary = "Listar capacitaciones de un cliente")
    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL','CLIENTE')")
    public List<CapacitacionResponse> listarPorCliente(@PathVariable Long idCliente) {
        return capacitacionService.listarPorCliente(idCliente);
    }

    @Operation(
            summary = "Listar capacitaciones extra de un cliente",
            description = "Retorna solo las capacitaciones con esCapacitacionExtra=true que generan costo adicional al plan (RF-CAP4)."
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

    @Operation(summary = "Programar una nueva capacitación")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROFESIONAL')")
    public ResponseEntity<CapacitacionResponse> crear(
            @Valid @RequestBody CrearCapacitacionRequest request) {

        CapacitacionResponse creada = capacitacionService.crear(request);

        return ResponseEntity
                .created(URI.create("/api/capacitaciones/" + creada.id()))
                .body(creada);
    }

    @Operation(summary = "Inscribir asistentes a una capacitación")
    @PostMapping("/{id}/asistentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL','CLIENTE')")
    public CapacitacionResponse inscribirAsistentes(
            @PathVariable Long id,
            @Valid @RequestBody InscribirAsistentesRequest request) {

        return capacitacionService.inscribirAsistentes(id, request);
    }

    @Operation(summary = "Confirmar asistencia")
    @PostMapping("/{id}/asistentes/{idAsistente}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL', 'CLIENTE')")
    public AsistenciaResponse confirmarAsistencia(
            @PathVariable Long id,
            @PathVariable Long idAsistente,
            @RequestBody(required = false) ConfirmarAsistenciaRequest request) {

        return capacitacionService.confirmarAsistencia(id, idAsistente, request);
    }

    @Operation(summary = "Registrar asistencia efectiva")
    @PatchMapping("/{id}/asistentes/{idAsistente}/asistio")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public AsistenciaResponse registrarAsistencia(
            @PathVariable Long id,
            @PathVariable Long idAsistente,
            @RequestParam boolean asistio) {

        return capacitacionService.registrarAsistencia(id, idAsistente, asistio);
    }

    @Operation(summary = "Iniciar la capacitación")
    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public CapacitacionResponse iniciar(@PathVariable Long id) {
        return capacitacionService.iniciar(id);
    }

    @Operation(summary = "Finalizar la capacitación")
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