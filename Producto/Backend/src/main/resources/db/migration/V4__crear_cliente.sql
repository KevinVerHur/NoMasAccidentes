-- =====================================================
-- V4: subdominio cliente (RF06–RF12)
-- =====================================================

CREATE TABLE cliente (
    id_cliente          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    razon_social        VARCHAR(200) NOT NULL,
    rut                 VARCHAR(12)  NOT NULL UNIQUE,
    nombre_contacto     VARCHAR(120) NOT NULL,
    email               VARCHAR(120) NOT NULL,
    telefono            VARCHAR(20),
    rubro               VARCHAR(80)  NOT NULL,
    plan                VARCHAR(40)  NOT NULL DEFAULT 'BASICO',
    cantidad_trabajadores INT,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    id_profesional      BIGINT,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion      DATETIME,
    fecha_actualizacion DATETIME,
    creado_por          VARCHAR(80),
    actualizado_por     VARCHAR(80),
    CONSTRAINT fk_cliente_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cliente_rut    ON cliente(rut);
CREATE INDEX idx_cliente_estado ON cliente(estado);
CREATE INDEX idx_cliente_activo ON cliente(activo);
