CREATE TABLE IF NOT EXISTS revinfo (
    rev INT NOT NULL AUTO_INCREMENT,
    revtstmp BIGINT,
    PRIMARY KEY (rev)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Espejo Envers de empresa (@Audited). El representante (tabla cliente)
-- NO se audita, por eso no hay cliente_aud.
CREATE TABLE empresa_aud (
    id_empresa BIGINT NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,

    razon_social VARCHAR(200),
    rut VARCHAR(12),
    direccion VARCHAR(200),
    comuna VARCHAR(80),
    id_rubro BIGINT,
    plan VARCHAR(40),
    cantidad_trabajadores INT,
    estado VARCHAR(20),
    id_profesional BIGINT,
    activo BOOLEAN,

    fecha_creacion DATETIME,
    fecha_actualizacion DATETIME,
    creado_por VARCHAR(80),
    actualizado_por VARCHAR(80),

    PRIMARY KEY (id_empresa, rev),
    CONSTRAINT fk_empresa_aud_rev
        FOREIGN KEY (rev) REFERENCES revinfo (rev)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE profesional_aud (
    id_profesional BIGINT NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,

    id_usuario BIGINT,
    rut VARCHAR(12),
    telefono VARCHAR(20),
    especialidad VARCHAR(120),
    latitud DECIMAL(9,6),
    longitud DECIMAL(9,6),
    estado VARCHAR(30),
    activo BOOLEAN,

    fecha_creacion DATETIME,
    fecha_actualizacion DATETIME,
    creado_por VARCHAR(80),
    actualizado_por VARCHAR(80),

    PRIMARY KEY (id_profesional, rev),
    CONSTRAINT fk_profesional_aud_rev
        FOREIGN KEY (rev) REFERENCES revinfo (rev)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
