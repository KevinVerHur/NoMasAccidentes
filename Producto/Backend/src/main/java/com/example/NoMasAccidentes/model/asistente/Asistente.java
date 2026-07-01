package com.example.NoMasAccidentes.model.asistente;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Trabajador del cliente que puede inscribirse como asistente a capacitaciones.
 * Un cliente tiene muchos asistentes (relación 1:N).
 */
@Entity
@Table(name = "asistente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE asistente SET activo = false WHERE id_asistente = ?")
@SQLRestriction("activo = true")
public class Asistente extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistente")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @Column(name = "rut", nullable = false, unique = true, length = 12)
    private String rut;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 120)
    private String apellidos;

    @Column(name = "cargo", length = 80)
    private String cargo;

    @Column(name = "area", length = 80)
    private String area;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
