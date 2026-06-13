package com.example.NoMasAccidentes.repository.asesoria;

import com.example.NoMasAccidentes.model.asesoria.PropuestaMejora;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropuestaMejoraRepository extends JpaRepository<PropuestaMejora, Long> {

    List<PropuestaMejora> findByInformeIdOrderByFechaPropuestaDesc(Long idInforme);
}
