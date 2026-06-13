package com.example.NoMasAccidentes.controller.consulta;

import com.example.NoMasAccidentes.dto.consulta.ConsultaResponse;
import com.example.NoMasAccidentes.dto.consulta.CrearConsultaRequest;
import com.example.NoMasAccidentes.service.consulta.ConsultaService;
import jakarta.validation.Valid;
import java.net.URI;
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

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<ConsultaResponse> listarPorCliente(@PathVariable Long idCliente){
        return consultaService.listarPorCliente(idCliente);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConsultaResponse> crear(@Valid @RequestBody CrearConsultaRequest request){
        ConsultaResponse creada = consultaService.crear(request);

        return ResponseEntity
                .created(URI.create("/api/consultas/" + creada.id()))
                .body(creada);
    }
}
