package com.example.NoMasAccidentes.controller.consulta;

import com.example.NoMasAccidentes.dto.consulta.ConsultaResponse;
import com.example.NoMasAccidentes.dto.consulta.CrearConsultaRequest;
import com.example.NoMasAccidentes.service.consulta.ConsultaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
public class ConsultaController {
    
    private final ConsultaService consultaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ConsultaResponse> listar(Pageable pageable){
        return consultaService.listar(pageable);
    }

    @GetMapping("/empresa/{idEmpresa}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<ConsultaResponse> listarPorEmpresa(@PathVariable Long idEmpresa){
        return consultaService.listarPorEmpresa(idEmpresa);
    }

    /** Llamados atendidos por el profesional autenticado (RF02/RF41). */
    @GetMapping("/mias")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public List<ConsultaResponse> misConsultas(Principal principal){
        return consultaService.misConsultas(principal.getName());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ResponseEntity<ConsultaResponse> crear(@Valid @RequestBody CrearConsultaRequest request,
                                                  Principal principal){
        ConsultaResponse creada = consultaService.crear(request, principal.getName());

        return ResponseEntity
                .created(URI.create("/api/consultas/" + creada.id()))
                .body(creada);
    }
}
