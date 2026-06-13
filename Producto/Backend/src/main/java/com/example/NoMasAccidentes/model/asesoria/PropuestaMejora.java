package com.example.NoMasAccidentes.model.asesoria;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.informe.Informe;
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
 * Propuesta de mejora derivada del informe de una asesoría (RF25). Registra la
 * acción recomendada, su plazo, responsable y el seguimiento de su verificación.
 */
@Entity
@Table(name = "propuesta_mejora")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE propuesta_mejora SET activo = false WHERE id_propuesta = ?")
@SQLRestriction("activo = true")
public class PropuestaMejora extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_propuesta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_informe", nullable = false)
    private Informe informe;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "fecha_propuesta")
    private LocalDate fechaPropuesta;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(name = "fecha_verificacion")
    private LocalDate fechaVerificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoPropuesta estado = EstadoPropuesta.PENDIENTE;

    @Column(name = "responsable", length = 120)
    private String responsable;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
