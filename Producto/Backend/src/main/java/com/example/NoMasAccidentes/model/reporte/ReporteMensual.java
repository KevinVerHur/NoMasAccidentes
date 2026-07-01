package com.example.NoMasAccidentes.model.reporte;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Reporte mensual de gestión por cliente (RF38, RF39). Consolida los totales
 * realizados en el periodo (visitas, capacitaciones, asesorías, llamados,
 * accidentes, multas y costos extra). Una fila por cliente/mes/año.
 */
@Entity
@Table(name = "reporte_mensual",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reporte_periodo",
                columnNames = {"id_empresa", "mes", "anio"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE reporte_mensual SET activo = false WHERE id_reporte = ?")
@SQLRestriction("activo = true")
public class ReporteMensual extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @Column(name = "mes", nullable = false)
    private int mes;

    @Column(name = "anio", nullable = false)
    private int anio;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "total_visitas", nullable = false)
    @Builder.Default
    private int totalVisitas = 0;

    @Column(name = "total_capacitaciones", nullable = false)
    @Builder.Default
    private int totalCapacitaciones = 0;

    @Column(name = "total_asesorias", nullable = false)
    @Builder.Default
    private int totalAsesorias = 0;

    @Column(name = "total_llamados", nullable = false)
    @Builder.Default
    private int totalLlamados = 0;

    @Column(name = "total_accidentes", nullable = false)
    @Builder.Default
    private int totalAccidentes = 0;

    @Column(name = "total_multas", nullable = false)
    @Builder.Default
    private int totalMultas = 0;

    @Column(name = "costos_extra", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costosExtra = BigDecimal.ZERO;

    @Column(name = "url_pdf", length = 300)
    private String urlPdf;

    /** Regeneración del reporte fuera del cierre mensual programado (RF46). */
    @Column(name = "es_actualizacion_extra", nullable = false)
    @Builder.Default
    private boolean esActualizacionExtra = false;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
