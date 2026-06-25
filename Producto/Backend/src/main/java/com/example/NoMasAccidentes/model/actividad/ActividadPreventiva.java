package com.example.NoMasAccidentes.model.actividad;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "actividad_preventiva")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE actividad_preventiva SET activo = false WHERE id_actividad =?")
@SQLRestriction("activo = true")
public class ActividadPreventiva extends BaseEntity{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 120)
    private String normativa;

    @Column(length = 120)
    private String responsable;

    @Column(name = "fecha_planificada", nullable = false)
    private LocalDate fechaPlanificada;

    @Column(name = "fecha_compromiso", nullable = false)
    private LocalDate fechaCompromiso;

    @Column(name = "fecha_cumplimiento")
    private LocalDate fechaCumplimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoActividadPreventiva estado = EstadoActividadPreventiva.PENDIENTE;

    @Column(length = 1000)
    private String observaciones;

    @Column(name = "alerta_enviada", nullable = false)
    @Builder.Default
    private boolean alertaEnviada = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}
