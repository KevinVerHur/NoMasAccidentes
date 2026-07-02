package com.example.NoMasAccidentes.model.empresa;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.rubro.Rubro;
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
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Empresa cliente (persona jurídica) que contrata los servicios de prevención
 * de riesgos. Las actividades operativas (visitas, pagos, capacitaciones,
 * asesorías, reportes) cuelgan de la empresa; las personas de contacto son los
 * representantes (entidad Cliente).
 * Soft delete: marcar activo=false mantiene trazabilidad histórica (RNF14).
 */
@Entity
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@SQLDelete(sql = "UPDATE empresa SET activo = false WHERE id_empresa = ?")
@SQLRestriction("activo = true")
public class Empresa extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long id;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "rut", nullable = false, unique = true, length = 12)
    private String rut;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "comuna", length = 80)
    private String comuna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rubro", nullable = false)
    private Rubro rubro;

    @Column(name = "plan", nullable = false, length = 40)
    private String plan;

    /** Nº de trabajadores de la empresa; insumo de la tasa de accidentabilidad (RF40). */
    @Column(name = "cantidad_trabajadores")
    private Integer cantidadTrabajadores;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoEmpresa estado = EstadoEmpresa.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profesional")
    private Profesional profesional;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
