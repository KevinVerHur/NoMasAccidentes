package com.example.NoMasAccidentes.model.configuracion;

import com.example.NoMasAccidentes.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "configuracion_empresa")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionEmpresa extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracion_empresa")
    private Long id;

    @Column(name = "nombre_empresa", nullable = false, length = 200)
    private String nombreEmpresa;

    @Column(name = "rut", nullable = false, length = 12)
    private String rut;

    @Column(name = "email_contacto", nullable = false, length = 120)
    private String emailContacto;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "region", length = 80)
    private String region;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}