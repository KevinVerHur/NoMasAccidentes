package com.example.NoMasAccidentes.repository.asistente;

import com.example.NoMasAccidentes.model.asistente.Asistente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenteRepository extends JpaRepository<Asistente, Long> {

    List<Asistente> findByEmpresaId(Long idEmpresa);

    boolean existsByRut(String rut);
}
