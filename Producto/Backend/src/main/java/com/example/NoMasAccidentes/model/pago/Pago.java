package com.example.NoMasAccidentes.model.pago;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago extends BaseEntity{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(name = "alerta_enviada", nullable = false)
    @Builder.Default
    private boolean alertaEnviada = false;
}
