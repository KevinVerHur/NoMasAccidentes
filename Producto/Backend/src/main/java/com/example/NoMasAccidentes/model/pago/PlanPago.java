package com.example.NoMasAccidentes.model.pago;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.empresa.Empresa;
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
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Contrato de un cliente con un plan de pago (RF08).
 */
@Entity
@Table(name = "plan_de_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE plan_de_pago SET activo = false WHERE id_plan = ?")
@SQLRestriction("activo = true")
public class PlanPago extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mensualidad", nullable = false)
    private Mensualidad mensualidad;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_termino")
    private LocalDate fechaTermino;

    @Column(name = "cuotas_totales")
    private Integer cuotasTotales;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicidad", nullable = false, length = 20)
    @Builder.Default
    private Periodicidad periodicidad = Periodicidad.MENSUAL;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
