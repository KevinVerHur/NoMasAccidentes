package com.example.NoMasAccidentes.model.notificacion;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.usuario.Usuario;
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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Notificación in-app dirigida a un usuario (bandeja del portal).
 * Registra el mismo evento que dispara un correo transaccional, permitiendo
 * una bandeja con badge de no leídas y navegación al recurso relacionado.
 * Soft delete: activo=false conserva trazabilidad histórica (RNF14).
 */
@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE notificacion SET activo = false WHERE id_notificacion = ?")
@SQLRestriction("activo = true")
public class Notificacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long id;

    /** Usuario destinatario de la notificación. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoNotificacion tipo;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    /** Ruta del frontend a la que navegar al hacer clic (opcional). */
    @Column(name = "enlace", length = 200)
    private String enlace;

    @Column(name = "leida", nullable = false)
    @Builder.Default
    private boolean leida = false;

    @Column(name = "fecha_leida")
    private LocalDateTime fechaLeida;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
