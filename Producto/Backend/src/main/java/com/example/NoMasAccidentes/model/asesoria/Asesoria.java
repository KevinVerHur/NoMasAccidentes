package com.example.NoMasAccidentes.model.asesoria;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.cliente.Cliente;
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
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Asesoría por accidente o fiscalización (RF22–RF25). El cliente cuenta con 10
 * asesorías incluidas al año (RF23); a partir de la 11.ª se marca como extra
 * para su cobro (RF24, {@code esAsesoriaExtra}).
 */
@Entity
@Table(name = "asesoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE asesoria SET activo = false WHERE id_asesoria = ?")
@SQLRestriction("activo = true")
public class Asesoria extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asesoria")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_profesional", nullable = false)
    private Profesional profesional;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @Column(name = "fecha_atencion")
    private LocalDate fechaAtencion;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoAsesoria tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoAsesoria estado = EstadoAsesoria.SOLICITADA;

    @Column(name = "es_asesoria_extra", nullable = false)
    @Builder.Default
    private boolean esAsesoriaExtra = false;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
