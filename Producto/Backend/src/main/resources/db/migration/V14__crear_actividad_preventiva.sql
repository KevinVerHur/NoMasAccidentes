CREATE TABLE actividad_preventiva (
    id_actividad          BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente            BIGINT NOT NULL,
    titulo                VARCHAR(160) NOT NULL,
    descripcion           VARCHAR(1000),
    responsable           VARCHAR(120),
    fecha_planificada     DATE NOT NULL,
    fecha_compromiso      DATE NOT NULL,
    fecha_cumplimiento    DATE,
    estado                VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observaciones         VARCHAR(1000),
    activo                BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80),
    CONSTRAINT fk_actividad_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_actividad_cliente ON actividad_preventiva(id_cliente);
CREATE INDEX idx_actividad_estado ON actividad_preventiva(estado);
CREATE INDEX idx_actividad_fecha_compromiso ON actividad_preventiva(fecha_compromiso);
CREATE INDEX idx_actividad_activo ON actividad_preventiva(activo);