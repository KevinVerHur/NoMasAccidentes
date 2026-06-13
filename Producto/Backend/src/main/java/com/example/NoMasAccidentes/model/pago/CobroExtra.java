package com.example.NoMasAccidentes.model.pago;

import com.example.NoMasAccidentes.common.BaseEntity;
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
 * Cobro extra asociado a una cuota (RF21, RF24, RF28).
 * {@code idOrigen} referencia (polimórfica, sin FK) la entidad que generó el cobro.
 */
@Entity
@Table(name = "cobro_extra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE cobro_extra SET activo = false WHERE id_cobro_extra = ?")
@SQLRestriction("activo = true")
public class CobroExtra extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cobro_extra")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pago", nullable = false)
    private Pago pago;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cobro", nullable = false, length = 30)
    private TipoCobro tipoCobro;

    @Column(name = "id_origen")
    private Long idOrigen;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDate fechaGeneracion;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
