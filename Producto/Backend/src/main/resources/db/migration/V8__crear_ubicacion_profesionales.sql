-- Producto/Backend/src/main/resources/db/migration/V8__crear_ubicacion_profesional.sql

CREATE TABLE ubicacion_profesional (
    id_ubicacion BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_profesional BIGINT NOT NULL,
    latitud DECIMAL(9,6) NOT NULL,
    longitud DECIMAL(9,6) NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ubicacion_profesional
        FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ubicacion_profesional_fecha
ON ubicacion_profesional(id_profesional, fecha_registro);