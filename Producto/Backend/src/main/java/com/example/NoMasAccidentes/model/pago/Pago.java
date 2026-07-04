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
 * Cuota de pago de un plan (RF09, RF10).
 */
@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE pago SET activo = false WHERE id_pago = ?")
@SQLRestriction("activo = true")
public class Pago extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plan", nullable = false)
    private PlanPago plan;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "medio_pago", length = 40)
    private String medioPago;

    /** Token de la transacción Webpay (RF09); correlaciona el retorno con la cuota. */
    @Column(name = "webpay_token", length = 100)
    private String webpayToken;

    /** Orden de compra enviada a Transbank (RF09). */
    @Column(name = "webpay_orden_compra", length = 50)
    private String webpayOrdenCompra;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 20)
    @Builder.Default
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    /** Indica si ya se envió la alerta de pago pendiente (RF31). */
    @Column(name = "alerta_enviada", nullable = false)
    @Builder.Default
    private boolean alertaEnviada = false;
}
