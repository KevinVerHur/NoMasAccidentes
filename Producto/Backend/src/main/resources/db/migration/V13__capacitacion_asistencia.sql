-- ============================================================
-- Migración: capacitacion, asistente, asistencia
-- ============================================================

CREATE TABLE IF NOT EXISTS asistente (
    id_asistente         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_cliente           BIGINT        NOT NULL,
    rut                  VARCHAR(12)   NOT NULL UNIQUE,
    nombre               VARCHAR(80)   NOT NULL,
    apellidos            VARCHAR(120)  NOT NULL,
    cargo                VARCHAR(80),
    area                 VARCHAR(80),
    email                VARCHAR(120),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_asistente_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS capacitacion (
    id_capacitacion       BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_cliente            BIGINT        NOT NULL,
    curso                 VARCHAR(150)  NOT NULL,
    id_relator            BIGINT        NOT NULL,
    fecha_programada      DATE          NOT NULL,
    hora_programada       TIME          NOT NULL,
    cupos                 INT           NOT NULL,
    objetivo              VARCHAR(500),
    fecha_realizacion     DATE,
    estado                VARCHAR(20)   NOT NULL DEFAULT 'PROGRAMADA',
    es_capacitacion_extra BOOLEAN       NOT NULL DEFAULT FALSE,
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80),
    CONSTRAINT fk_capacitacion_cliente FOREIGN KEY (id_cliente)  REFERENCES cliente(id_cliente),
    CONSTRAINT fk_capacitacion_relator FOREIGN KEY (id_relator)  REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- En MySQL los índices van sin IF NOT EXISTS
CREATE INDEX idx_cap_cliente ON capacitacion(id_cliente);
CREATE INDEX idx_cap_relator ON capacitacion(id_relator);
CREATE INDEX idx_cap_fecha   ON capacitacion(fecha_programada);
CREATE INDEX idx_cap_estado  ON capacitacion(estado);
CREATE INDEX idx_cap_extra   ON capacitacion(es_capacitacion_extra);

CREATE TABLE IF NOT EXISTS asistencia (
    id_asistencia        BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_capacitacion      BIGINT        NOT NULL,
    id_asistente         BIGINT        NOT NULL,
    confirmado           BOOLEAN       NOT NULL DEFAULT FALSE,
    asistio              BOOLEAN       NOT NULL DEFAULT FALSE,
    fecha_confirmacion   DATETIME,
    firma_digital        VARCHAR(300),
    observaciones        VARCHAR(500),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT uk_asistencia           UNIQUE (id_capacitacion, id_asistente),
    CONSTRAINT fk_asistencia_capacit   FOREIGN KEY (id_capacitacion) REFERENCES capacitacion(id_capacitacion),
    CONSTRAINT fk_asistencia_asistente FOREIGN KEY (id_asistente)    REFERENCES asistente(id_asistente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;