package com.example.NoMasAccidentes.repository.profesional;

import com.example.NoMasAccidentes.model.profesional.UbicacionProfesional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UbicacionProfesionalRepository extends JpaRepository<UbicacionProfesional, Long> {

    @Query("""
        select u
        from UbicacionProfesional u
        where u.fechaRegistro >= :desde
          and u.fechaRegistro = (
            select max(u2.fechaRegistro)
            from UbicacionProfesional u2
            where u2.profesional.id = u.profesional.id
        )
    """)
    List<UbicacionProfesional> findUltimasUbicacionesActivas(@Param("desde") LocalDateTime desde);

    Optional<UbicacionProfesional> findTopByProfesionalUsuarioEmailOrderByFechaRegistroDesc(String email);
}