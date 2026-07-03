package com.example.NoMasAccidentes.model.solicitud;

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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Solicitud del cliente para una asesoría/capacitación/visita (mejora web).
 * La crea el representante autenticado; el admin la aprueba (creando el
 * recurso real y marcando si es extra) o la rechaza. Soft delete (RNF14).
 */
@Entity
@Table(name = "solicitud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE solicitud SET activo = false WHERE id_solicitud = ?")
@SQLRestriction("activo = true")
public class Solicitud extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoSolicitud tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    /** Fecha en que el cliente preferiría el servicio (opcional, orientativa). */
    @Column(name = "fecha_preferida")
    private LocalDate fechaPreferida;

    /** Definido al aprobar: si el servicio queda fuera del plan (cobro extra). */
    @Column(name = "es_extra", nullable = false)
    @Builder.Default
    private boolean esExtra = false;

    /** Comentario del admin en la respuesta (aprobación o rechazo). */
    @Column(name = "respuesta_admin", length = 500)
    private String respuestaAdmin;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
