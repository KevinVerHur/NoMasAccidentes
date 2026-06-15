package com.example.NoMasAccidentes.model.capacitacion;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.asistencia.Asistencia;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Capacitación programada para un cliente.
 *
 * Campos del formulario "Nueva Capacitación":
 *   - cliente        → ManyToOne {@link Cliente}
 *   - curso          → nombre/tema de la capacitación
 *   - relator        → ManyToOne {@link Profesional} (el profesional que dicta)
 *   - fechaProgramada → LocalDate
 *   - horaProgramada  → LocalTime
 *   - cupos          → cantidad máxima de asistentes
 *   - objetivo       → descripción del objetivo de la capacitación
 *
 * Reglas de negocio:
 *   - RF-CAP1: debe programarse con al menos 15 días de anticipación (validado en servicio).
 *   - RF-CAP4: esCapacitacionExtra=true indica costo adicional al plan del cliente.
 *
 * Relación N:M con {@link Asistente} a través de {@link Asistencia}.
 */
@Entity
@Table(name = "capacitacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE capacitacion SET activo = false WHERE id_capacitacion = ?")
@SQLRestriction("activo = true")
public class Capacitacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_capacitacion")
    private Long id;

    /** Cliente al que está dirigida la capacitación. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    /** Nombre del curso / tema (campo "Curso" del formulario). */
    @Column(name = "curso", nullable = false, length = 150)
    private String curso;

    /**
     * Profesional que dicta la capacitación (campo "Relator" del formulario).
     * En el sistema los relatores son los Profesionales registrados.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_relator", nullable = false)
    private Profesional relator;

    /** Fecha programada (campo "Fecha" del formulario). */
    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    /** Hora de inicio (campo "Hora" del formulario, ej. 09:30). */
    @Column(name = "hora_programada", nullable = false)
    private LocalTime horaProgramada;

    /** Cantidad máxima de asistentes permitidos (campo "Cupos"). */
    @Column(name = "cupos", nullable = false)
    private Integer cupos;

    /** Descripción del objetivo de la capacitación (campo "Objetivo"). */
    @Column(name = "objetivo", length = 500)
    private String objetivo;

    /** Fecha real de realización (se actualiza al finalizar). */
    @Column(name = "fecha_realizacion")
    private LocalDate fechaRealizacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoCapacitacion estado = EstadoCapacitacion.PROGRAMADA;

    /**
     * Indica si la capacitación es adicional al plan del cliente
     * y por tanto genera un cobro extra (RF-CAP4).
     */
    @Column(name = "es_capacitacion_extra", nullable = false)
    @Builder.Default
    private boolean esCapacitacionExtra = false;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    /** Asistentes inscritos (N:M a través de {@link Asistencia}). */
    @OneToMany(mappedBy = "capacitacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Asistencia> asistencias = new ArrayList<>();
}
