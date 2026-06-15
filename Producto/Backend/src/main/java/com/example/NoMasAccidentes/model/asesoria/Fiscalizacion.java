package com.example.NoMasAccidentes.model.asesoria;

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
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Fiscalización de un organismo que motiva una asesoría (RF22) y sustenta el
 * cumplimiento normativo (RF42-44). Puede derivar en multas.
 */
@Entity
@Table(name = "fiscalizacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE fiscalizacion SET activo = false WHERE id_fiscalizacion = ?")
@SQLRestriction("activo = true")
public class Fiscalizacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fiscalizacion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asesoria", nullable = false)
    private Asesoria asesoria;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidad_fiscalizadora", nullable = false, length = 30)
    private EntidadFiscalizadora entidadFiscalizadora;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", length = 20)
    private ResultadoFiscalizacion resultado;

    @Column(name = "observaciones", length = 2000)
    private String observaciones;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
