package com.example.NoMasAccidentes.model.asistencia;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.asistente.Asistente;
import com.example.NoMasAccidentes.model.capacitacion.Capacitacion;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Tabla intermedia enriquecida de la relación N:M entre {@link Capacitacion} y {@link Asistente}.
 *
 *  - confirmado: true cuando el asistente confirma su participación (RF-CAP3).
 *  - asistio:    true cuando el ADMIN registra la asistencia efectiva.
 *
 * La restricción UNIQUE (id_capacitacion, id_asistente) evita inscripciones duplicadas.
 */
@Entity
@Table(
    name = "asistencia",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_asistencia",
        columnNames = {"id_capacitacion", "id_asistente"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE asistencia SET activo = false WHERE id_asistencia = ?")
@SQLRestriction("activo = true")
public class Asistencia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistencia")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_capacitacion", nullable = false)
    private Capacitacion capacitacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asistente", nullable = false)
    private Asistente asistente;

    /** true cuando el asistente llama al endpoint /confirmar (RF-CAP3). */
    @Column(name = "confirmado", nullable = false)
    @Builder.Default
    private boolean confirmado = false;

    /** true cuando el ADMIN registra que la persona efectivamente asistió. */
    @Column(name = "asistio", nullable = false)
    @Builder.Default
    private boolean asistio = false;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "firma_digital", length = 300)
    private String firmaDigital;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
