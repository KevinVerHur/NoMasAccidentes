package com.example.NoMasAccidentes.model.pago;

import com.example.NoMasAccidentes.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Catálogo de planes de pago (mensualidades) — RF08.
 * Define el monto base y lo incluido/cobros extra de cada plan.
 */
@Entity
@Table(name = "mensualidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE mensualidad SET activo = false WHERE id_mensualidad = ?")
@SQLRestriction("activo = true")
public class Mensualidad extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensualidad")
    private Long id;

    @Column(name = "nombre_plan", nullable = false, unique = true, length = 80)
    private String nombrePlan;

    @Column(name = "monto_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoBase;

    @Column(name = "visitas_incluidas")
    private Integer visitasIncluidas;

    @Column(name = "asesorias_incluidas")
    private Integer asesoriasIncluidas;

    @Column(name = "capacitaciones_incluidas")
    private Integer capacitacionesIncluidas;

    @Column(name = "costo_visita_extra", precision = 10, scale = 2)
    private BigDecimal costoVisitaExtra;

    @Column(name = "costo_asesoria_extra", precision = 10, scale = 2)
    private BigDecimal costoAsesoriaExtra;

    @Column(name = "costo_capacitacion_extra", precision = 10, scale = 2)
    private BigDecimal costoCapacitacionExtra;

    @Column(name = "costo_llamado_fuera_horario", precision = 10, scale = 2)
    private BigDecimal costoLlamadoFueraHorario;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
