package com.example.NoMasAccidentes.model.profesional;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Profesional de prevención de riesgos. Asociado 1-a-1 con un Usuario del sistema.
 * Ggestión de profesionales
 */
@Entity
@Table(name = "profesional")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE profesional SET activo = false WHERE id_profesional = ?")
@SQLRestriction("activo = true")
public class Profesional extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profesional")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "rut", nullable = false, unique = true, length = 12)
    private String rut;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "especialidad", length = 120)
    private String especialidad;

    /** Latitud actual del profesional (geolocalización para RF de visitas). */
    @Column(name = "latitud", precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
