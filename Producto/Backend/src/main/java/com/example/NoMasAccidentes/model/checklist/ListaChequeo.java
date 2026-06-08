package com.example.NoMasAccidentes.model.checklist;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import jakarta.persistence.*;
import java.time.Year;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Lista de chequeo asociada a un cliente.
 * RF16/RF17: permite controlar cambios con maximo 2 modificaciones por anio.
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

    public static final int LIMITE_MODIFICACIONES_ANUALES = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lista_chequeo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Lob
    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "anio_control", nullable = false)
    @Builder.Default
    private Integer anioControl = Year.now().getValue();

    @Column(name = "modificaciones_anio", nullable = false)
    @Builder.Default
    private Integer modificacionesAnio = 0;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    public void reiniciarContadorSiCambioAnio(int anioActual) {
        if (!anioControl.equals(anioActual)) {
            anioControl = anioActual;
            modificacionesAnio = 0;
        }
    }

    public boolean puedeModificarse() {
        return modificacionesAnio < LIMITE_MODIFICACIONES_ANUALES;
    }

    public void registrarModificacion() {
        modificacionesAnio++;
    }

    public int modificacionesDisponibles() {
        return Math.max(0, LIMITE_MODIFICACIONES_ANUALES - modificacionesAnio);
    }
}