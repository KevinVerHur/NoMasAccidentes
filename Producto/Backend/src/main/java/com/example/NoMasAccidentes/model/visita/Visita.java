package com.example.NoMasAccidentes.model.visita;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.profesional.Profesional;
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
import java.math.BigDecimal;
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
 * Visita a un cliente: planificada (RF13) y luego registrada en terreno (RF14).
 * Aplica la lista de chequeo del cliente (RF16).
 */
@Entity
@Table(name = "visita")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE visita SET activo = false WHERE id_visita = ?")
@SQLRestriction("activo = true")
public class Visita extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_visita")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_profesional", nullable = false)
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lista_chequeo", nullable = false)
    private ListaChequeo listaChequeo;

    @Column(name = "tipo_revision", length = 20)
    private String tipoRevision;

    /** Fecha planificada de la visita (RF13). */
    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    /** Inicio real en terreno (RF14). */
    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    /** Fin real en terreno (RF14). */
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoVisita estado = EstadoVisita.PROGRAMADA;

    /** Geolocalización del registro en terreno (RF05/RF14). */
    @Column(name = "latitud", precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "observaciones", length = 2000)
    private String observaciones;

    /** Visita adicional fuera del plan, facturable como costo extra. */
    @Column(name = "es_visita_extra", nullable = false)
    @Builder.Default
    private boolean esVisitaExtra = false;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    /** Indica si ya se envió el recordatorio 24 h antes (RF29). */
    @Column(name = "recordatorio_enviado", nullable = false)
    @Builder.Default
    private boolean recordatorioEnviado = false;
}
