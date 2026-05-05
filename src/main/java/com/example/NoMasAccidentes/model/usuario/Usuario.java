package com.example.NoMasAccidentes.model.usuario;

import com.example.NoMasAccidentes.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Usuario del sistema con credenciales para iniciar sesión.
 * Autenticación.
 * Soft delete: al "eliminar" un usuario solo se marca activo=false (RNF14, trazabilidad).
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE usuario SET activo = false WHERE id_usuario = ?")
@SQLRestriction("activo = true")
public class Usuario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    /** Contraseña hasheada con bcrypt. NUNCA se expone en respuestas. */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 120)
    private String apellido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;
}
