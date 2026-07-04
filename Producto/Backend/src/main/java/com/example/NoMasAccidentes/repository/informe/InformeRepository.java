package com.example.NoMasAccidentes.repository.informe;

import com.example.NoMasAccidentes.model.informe.Informe;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformeRepository extends JpaRepository<Informe, Long> {

    Optional<Informe> findByVisitaId(Long idVisita);

    /** Informe de una asesoría (el informe de asesoría reutiliza esta tabla, RF15). */
    Optional<Informe> findByIdAsesoria(Long idAsesoria);

    /** Informes de las visitas de una empresa (portal cliente, RF15). */
    List<Informe> findByVisitaEmpresaIdOrderByFechaEmisionDesc(Long idEmpresa);

    /** Informes de un conjunto de asesorías (portal cliente, RF15/RF07). */
    List<Informe> findByIdAsesoriaInOrderByFechaEmisionDesc(List<Long> idsAsesoria);
}
