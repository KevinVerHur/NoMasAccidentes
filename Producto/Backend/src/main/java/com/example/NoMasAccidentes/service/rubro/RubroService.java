package com.example.NoMasAccidentes.service.rubro;

import com.example.NoMasAccidentes.dto.rubro.RubroResponse;
import com.example.NoMasAccidentes.repository.rubro.RubroRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Catálogo de rubros (solo lectura; alimenta el selector del alta de empresa). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RubroService {

    private final RubroRepository rubroRepository;

    public List<RubroResponse> listar() {
        return rubroRepository.findAll().stream()
                .map(r -> new RubroResponse(r.getId(), r.getNombre(), r.getTasaAccidentabilidad()))
                .toList();
    }
}
