package com.example.NoMasAccidentes.controller.profesional;

import com.example.NoMasAccidentes.dto.profesional.RegistrarUbicacionRequest;
import com.example.NoMasAccidentes.dto.profesional.UbicacionProfesionalResponse;
import com.example.NoMasAccidentes.service.profesional.UbicacionProfesionalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ubicaciones")
@RequiredArgsConstructor
public class UbicacionProfesionalController {

    private final UbicacionProfesionalService ubicacionService;

    @PostMapping
    @PreAuthorize("hasRole('PROFESIONAL')")
    public UbicacionProfesionalResponse registrarMiUbicacion(
            Authentication authentication,
            @Valid @RequestBody RegistrarUbicacionRequest request
    ) {
        return ubicacionService.registrarMiUbicacion(authentication.getName(), request);
    }

    @GetMapping("/activas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UbicacionProfesionalResponse> listarUbicacionesActivas() {
        return ubicacionService.listarUbicacionesActivas();
    }

    @GetMapping("/me/ultima")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public UbicacionProfesionalResponse obtenerMiUltimaUbicacion(Authentication authentication) {
        return ubicacionService.obtenerMiUltimaUbicacion(authentication.getName());
    }
}