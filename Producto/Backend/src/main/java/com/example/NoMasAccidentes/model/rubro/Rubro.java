package com.example.NoMasAccidentes.model.rubro;

import com.example.NoMasAccidentes.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * Catálogo de rubros. Una empresa pertenece a un rubro; la tasa de
 * accidentabilidad base sirve de referencia para el análisis de riesgo (RF40).
 */
@Entity
@Table(name = "rubro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE rubro SET activo = false WHERE id_rubro = ?")
@SQLRestriction("activo = true")
public class Rubro extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rubro")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 80)
    private String nombre;

    /** Tasa de accidentabilidad referencial del rubro (%). */
    @Column(name = "tasa_accidentabilidad", precision = 5, scale = 2)
    private BigDecimal tasaAccidentabilidad;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
