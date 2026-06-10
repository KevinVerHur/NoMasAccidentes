CREATE TABLE consulta (
    id_consulta BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    detalle VARCHAR(1000),
    fuera_horario BOOLEAN NOT NULL DEFAULT FALSE,
    costo_adicional BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion DATETIME,
    fecha_actualizacion DATETIME,
    creado_por VARCHAR(80),
    actualizado_por VARCHAR(80),
    CONSTRAINT fk_consulta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

CREATE INDEX idx_consulta_cliente_fecha ON consulta(id_cliente, fecha_hora);
CREATE INDEX idx_consulta_costo_adicional ON consulta(costo_adicional);