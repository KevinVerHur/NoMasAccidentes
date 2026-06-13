-- =====================================================
-- V16: satélites de la asesoría — accidente, fiscalizacion y multa.
-- accidente/fiscalizacion son la causa de la asesoría (RF22);
-- multa cuelga de una fiscalización (cumplimiento normativo RF42-44).
-- La propuesta_mejora (RF25) se agrega junto al informe de asesoría
-- en una migración posterior (depende de la tabla informe).
-- =====================================================

CREATE TABLE accidente (
    id_accidente          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_asesoria           BIGINT        NOT NULL,
    fecha_ocurrencia      DATE          NOT NULL,
    descripcion           VARCHAR(2000),
    gravedad              VARCHAR(20)   NOT NULL,
    trabajador_afectado   VARCHAR(150),
    dias_perdidos         INT,
    fue_reportado_susseso BOOLEAN       NOT NULL DEFAULT FALSE,
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80),
    CONSTRAINT fk_accidente_asesoria FOREIGN KEY (id_asesoria) REFERENCES asesoria(id_asesoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_accidente_asesoria ON accidente(id_asesoria);

CREATE TABLE fiscalizacion (
    id_fiscalizacion      BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_asesoria           BIGINT        NOT NULL,
    fecha                 DATE          NOT NULL,
    entidad_fiscalizadora VARCHAR(30)   NOT NULL,
    motivo                VARCHAR(500),
    resultado             VARCHAR(20),
    observaciones         VARCHAR(2000),
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80),
    CONSTRAINT fk_fiscalizacion_asesoria FOREIGN KEY (id_asesoria) REFERENCES asesoria(id_asesoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_fiscalizacion_asesoria ON fiscalizacion(id_asesoria);

CREATE TABLE multa (
    id_multa              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_fiscalizacion      BIGINT        NOT NULL,
    fecha_emision         DATE          NOT NULL,
    monto                 DECIMAL(12,2) NOT NULL,
    motivo                VARCHAR(1000),
    normativa_infringida  VARCHAR(150),
    estado_pago           VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80),
    CONSTRAINT fk_multa_fiscalizacion FOREIGN KEY (id_fiscalizacion) REFERENCES fiscalizacion(id_fiscalizacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_multa_fiscalizacion ON multa(id_fiscalizacion);
