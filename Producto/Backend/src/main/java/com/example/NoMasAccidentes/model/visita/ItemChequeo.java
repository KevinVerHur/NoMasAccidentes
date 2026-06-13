package com.example.NoMasAccidentes.model.visita;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Ítem individual de una lista de chequeo (RF16).
 */
@Entity
@Table(name = "item_chequeo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE item_chequeo SET activo = false WHERE id_item = ?")
@SQLRestriction("activo = true")
public class ItemChequeo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lista_chequeo", nullable = false)
    private ListaChequeo listaChequeo;

    @Column(name = "descripcion", nullable = false, length = 250)
    private String descripcion;

    @Column(name = "categoria", length = 80)
    private String categoria;

    @Column(name = "obligatorio", nullable = false)
    @Builder.Default
    private boolean obligatorio = true;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
