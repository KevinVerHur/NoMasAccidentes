-- =====================================================
-- V4: catálogo de rubros + empresa cliente (RF06–RF12)
--   Modelo separado (MER final):
--     rubro   → catálogo de riesgo con tasa de accidentabilidad base.
--     empresa → persona jurídica adherida que contrata los servicios.
--   El representante (persona de contacto con acceso al portal) se
--   crea aparte en V6 (tabla cliente).
-- =====================================================

-- ---------- Catálogo de rubros (una empresa pertenece a un rubro) ----------
CREATE TABLE rubro (
    id_rubro              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre                VARCHAR(80)  NOT NULL UNIQUE,
    tasa_accidentabilidad DECIMAL(5,2),
    activo                BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Rubros del catálogo (según el prototipo No Más Accidentes .html).
-- tasa_accidentabilidad es referencial (%) para el análisis de riesgo (RF40).
INSERT INTO rubro (nombre, tasa_accidentabilidad, activo, creado_por) VALUES
 ('Construcción', 5.50, TRUE, 'flyway-seed'),
 ('Minería',      4.00, TRUE, 'flyway-seed'),
 ('Transporte',   4.50, TRUE, 'flyway-seed');

-- ---------- Empresa cliente (persona jurídica) ----------
CREATE TABLE empresa (
    id_empresa            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    razon_social          VARCHAR(200) NOT NULL,
    rut                   VARCHAR(12)  NOT NULL UNIQUE,
    direccion             VARCHAR(200),
    comuna                VARCHAR(80),
    id_rubro              BIGINT       NOT NULL,
    plan                  VARCHAR(40)  NOT NULL DEFAULT 'BASICO',
    cantidad_trabajadores INT,
    estado                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    id_profesional        BIGINT,
    activo                BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80),
    CONSTRAINT fk_empresa_rubro       FOREIGN KEY (id_rubro)       REFERENCES rubro(id_rubro),
    CONSTRAINT fk_empresa_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_empresa_rut    ON empresa(rut);
CREATE INDEX idx_empresa_estado ON empresa(estado);
CREATE INDEX idx_empresa_activo ON empresa(activo);
CREATE INDEX idx_empresa_rubro  ON empresa(id_rubro);
