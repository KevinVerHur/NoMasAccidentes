package com.example.NoMasAccidentes.model.cliente;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Cliente = representante / persona de contacto de una empresa adherida.
 * Una empresa puede tener varios representantes. La credencial de acceso al
 * portal (rol CLIENTE) es de la persona, no de la empresa: vive en id_usuario
 * (nullable — un contacto puede existir sin login).
 * Soft delete: marcar activo=false mantiene trazabilidad histórica (RNF14).
 */
@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE cliente SET activo = false WHERE id_cliente = ?")
@SQLRestriction("activo = true")
public class Cliente extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "cargo", length = 80)
    private String cargo;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    /** Cuenta de acceso al portal (rol CLIENTE). Se provisiona al crear el
     *  representante; define su contraseña vía invitación por correo. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
