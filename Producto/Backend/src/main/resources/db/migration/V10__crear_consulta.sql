CREATE TABLE consulta (
    id_consulta BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_empresa BIGINT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    detalle VARCHAR(1000),
    fuera_horario BOOLEAN NOT NULL DEFAULT FALSE,
    costo_adicional BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion DATETIME,
    fecha_actualizacion DATETIME,
    creado_por VARCHAR(80),
    actualizado_por VARCHAR(80),
    CONSTRAINT fk_consulta_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
);

CREATE INDEX idx_consulta_empresa_fecha ON consulta(id_empresa, fecha_hora);
CREATE INDEX idx_consulta_costo_adicional ON consulta(costo_adicional);
