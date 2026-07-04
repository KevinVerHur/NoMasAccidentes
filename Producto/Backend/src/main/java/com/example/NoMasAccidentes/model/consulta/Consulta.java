package com.example.NoMasAccidentes.model.consulta;

import com.example.NoMasAccidentes.common.BaseEntity;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "consulta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consulta extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    /** Profesional de la consultora que atendió/resolvió el llamado (RF02/RF41).
     *  Nullable: un admin puede registrar un llamado sin atribuirlo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profesional")
    private Profesional profesional;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "detalle", length = 1000)
    private String detalle;

    @Column(name = "fuera_horario", nullable = false)
    private boolean fueraHorario;

    @Column(name = "costo_adicional", nullable = false)
    private boolean costoAdicional;
}
