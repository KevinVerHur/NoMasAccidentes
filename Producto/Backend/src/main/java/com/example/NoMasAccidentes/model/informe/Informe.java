package com.example.NoMasAccidentes.model.informe;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.visita.Visita;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Informe posterior a una visita (RF15). El PDF se almacena en S3 (prod) o en
 * disco local (dev); {@code urlPdf} guarda la clave/ruta del archivo.
 */
@Entity
@Table(name = "informe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE informe SET activo = false WHERE id_informe = ?")
@SQLRestriction("activo = true")
public class Informe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_informe")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_visita")
    private Visita visita;

    /** Referencia a asesoría (módulo futuro). Sin FK por ahora. */
    @Column(name = "id_asesoria")
    private Long idAsesoria;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "contenido", length = 4000)
    private String contenido;

    @Column(name = "hallazgos", length = 2000)
    private String hallazgos;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoInforme estado = EstadoInforme.GENERADO;

    /** Clave/ruta del PDF almacenado (S3 key o ruta local). */
    @Column(name = "url_pdf", length = 300)
    private String urlPdf;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}
