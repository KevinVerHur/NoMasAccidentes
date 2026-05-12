package com.example.NoMasAccidentes.model.cliente;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Empresa cliente que contrata los servicios de prevención de riesgos.
 * Soft delete: marcar activo=false mantiene trazabilidad histórica (RNF14).
 */
@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE cliente SET activo = false WHERE id_cliente = ?")
@SQLRestriction("activo = true")
public class Cliente extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "rut", nullable = false, unique = true, length = 12)
    private String rut;

    @Column(name = "nombre_contacto", nullable = false, length = 120)
    private String nombreContacto;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "rubro", nullable = false, length = 80)
    private String rubro;

    @Column(name = "plan", nullable = false, length = 40)
    private String plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoCliente estado = EstadoCliente.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profesional")
    private Profesional profesional;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
