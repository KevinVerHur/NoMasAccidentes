CREATE TABLE visita (
    id_visita BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    id_profesional BIGINT,
    fecha_programada DATETIME NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    motivo VARCHAR(250),
    estado VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    recordatorio_enviado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion DATETIME,
    fecha_actualizacion DATETIME,
    creado_por VARCHAR(80),
    actualizado_por VARCHAR(80),
    CONSTRAINT fk_visita_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_visita_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
);

CREATE INDEX idx_visita_recordatorio 
ON visita(estado, recordatorio_enviado, fecha_programada);

CREATE TABLE pago (
    id_pago BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    alerta_enviada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion DATETIME,
    fecha_actualizacion DATETIME,
    creado_por VARCHAR(80),
    actualizado_por VARCHAR(80),
    CONSTRAINT fk_pago_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

CREATE INDEX idx_pago_alerta 
ON pago(estado, alerta_enviada, fecha_vencimiento);