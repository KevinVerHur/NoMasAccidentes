package com.example.NoMasAccidentes.controller.rubro;

import com.example.NoMasAccidentes.dto.rubro.RubroResponse;
import com.example.NoMasAccidentes.service.rubro.RubroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Catálogo de rubros (alimenta el selector del alta de empresa). */
@RestController
@RequestMapping("/api/rubros")
@RequiredArgsConstructor
@Tag(name = "Rubros", description = "Catálogo de rubros con tasa de accidentabilidad")
public class RubroController {

    private final RubroService rubroService;

    @Operation(summary = "Listar rubros")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<RubroResponse> listar() {
        return rubroService.listar();
    }
}
