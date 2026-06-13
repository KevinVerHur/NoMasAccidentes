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
 * Accidente laboral que motiva una asesoría (RF22). Insumo de los indicadores
 * de accidentabilidad por cliente (RF35).
 */
@Entity
@Table(name = "accidente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE accidente SET activo = false WHERE id_accidente = ?")
@SQLRestriction("activo = true")
public class Accidente extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_accidente")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asesoria", nullable = false)
    private Asesoria asesoria;

    @Column(name = "fecha_ocurrencia", nullable = false)
    private LocalDate fechaOcurrencia;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravedad", nullable = false, length = 20)
    private GravedadAccidente gravedad;

    @Column(name = "trabajador_afectado", length = 150)
    private String trabajadorAfectado;

    @Column(name = "dias_perdidos")
    private Integer diasPerdidos;

    @Column(name = "fue_reportado_susseso", nullable = false)
    @Builder.Default
    private boolean fueReportadoSusseso = false;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
