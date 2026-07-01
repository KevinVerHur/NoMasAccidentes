package com.example.NoMasAccidentes.model.visita;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Lista de chequeo asociada a un cliente (RF16). Una lista por cliente.
 * El contador {@code cambiosRealizadosAnio} controla el límite de
 * modificaciones anuales (RF17: máximo 2 veces al año).
 */
@Entity
@Table(name = "lista_chequeo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE lista_chequeo SET activo = false WHERE id_lista_chequeo = ?")
@SQLRestriction("activo = true")
public class ListaChequeo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lista_chequeo")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false, unique = true)
    private Empresa empresa;

    @Column(name = "nombre", length = 120)
    private String nombre;

    /** Cantidad de modificaciones realizadas en el año vigente (RF17). */
    @Column(name = "cambios_realizados_anio", nullable = false)
    @Builder.Default
    private Integer cambiosRealizadosAnio = 0;

    /** Año al que corresponde el contador de cambios; al cambiar de año se reinicia. */
    @Column(name = "anio_vigente")
    private Integer anioVigente;

    @Column(name = "fecha_ultima_modificacion")
    private LocalDate fechaUltimaModificacion;

    @OneToMany(mappedBy = "listaChequeo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<ItemChequeo> items = new ArrayList<>();

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
