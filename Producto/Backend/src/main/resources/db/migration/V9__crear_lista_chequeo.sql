CREATE TABLE lista_chequeo (
    id_lista_chequeo BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    contenido TEXT NOT NULL,
    anio_control INT NOT NULL,
    modificaciones_anio INT NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME,
    fecha_actualizacion DATETIME,
    creado_por VARCHAR(80),
    actualizado_por VARCHAR(80),
    CONSTRAINT fk_lista_chequeo_cliente
        FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_lista_chequeo_cliente
ON lista_chequeo(id_cliente);

CREATE INDEX idx_lista_chequeo_cliente_anio
ON lista_chequeo(id_cliente, anio_control);