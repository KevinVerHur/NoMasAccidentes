package com.example.NoMasAccidentes.model.usuario;

import com.example.NoMasAccidentes.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Rol del sistema: ADMIN, PROFESIONAL, CLIENTE, CAPACITADOR.
 * Determina los permisos del usuario y se incluye como claim en el JWT.
 */
@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol  extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;


    @Column(name = "nombre", nullable = false, unique = true, length = 40)
    private String nombre;

    @Column(name = "descripcion", length = 200)
    private String descripcion;}
