-- =====================================================
-- V7: subdominio visita (RF13–RF17)
--   RF13: planificar visitas mensuales (mínimo 2 por mes)
--   RF14: registrar visitas realizadas en terreno
--   RF16: asociar listas de chequeo por cliente
--   RF17: modificar listas de chequeo (máximo 2 veces al año)
-- =====================================================

-- Lista de chequeo: una por cliente (RF16). El contador anual controla RF17.
CREATE TABLE lista_chequeo (
    id_lista_chequeo           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    id_cliente                 BIGINT       NOT NULL UNIQUE,
    nombre                     VARCHAR(120),
    cambios_realizados_anio    INT          NOT NULL DEFAULT 0,
    anio_vigente               INT,
    fecha_ultima_modificacion  DATE,
    activo                     BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion             DATETIME,
    fecha_actualizacion        DATETIME,
    creado_por                 VARCHAR(80),
    actualizado_por            VARCHAR(80),
    CONSTRAINT fk_lista_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ítems de la lista de chequeo.
CREATE TABLE item_chequeo (
    id_item              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_lista_chequeo     BIGINT        NOT NULL,
    descripcion          VARCHAR(250)  NOT NULL,
    categoria            VARCHAR(80),
    obligatorio          BOOLEAN       NOT NULL DEFAULT TRUE,
    orden                INT,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_item_lista FOREIGN KEY (id_lista_chequeo) REFERENCES lista_chequeo(id_lista_chequeo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Visita: planificada (RF13) y registrada en terreno (RF14).
-- Requiere lista de chequeo del cliente (RF16) -> id_lista_chequeo NOT NULL.
CREATE TABLE visita (
    id_visita            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_cliente           BIGINT        NOT NULL,
    id_profesional       BIGINT        NOT NULL,
    id_lista_chequeo     BIGINT        NOT NULL,
    tipo_revision        VARCHAR(20),
    fecha_programada     DATE          NOT NULL,
    fecha_inicio         DATETIME,
    fecha_fin            DATETIME,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'PROGRAMADA',
    latitud              DECIMAL(9,6),
    longitud             DECIMAL(9,6),
    observaciones        VARCHAR(2000),
    es_visita_extra      BOOLEAN       NOT NULL DEFAULT FALSE,
    recordatorio_enviado BOOLEAN       NOT NULL DEFAULT FALSE,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_visita_cliente      FOREIGN KEY (id_cliente)       REFERENCES cliente(id_cliente),
    CONSTRAINT fk_visita_profesional  FOREIGN KEY (id_profesional)   REFERENCES profesional(id_profesional),
    CONSTRAINT fk_visita_lista        FOREIGN KEY (id_lista_chequeo) REFERENCES lista_chequeo(id_lista_chequeo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_visita_estado ON visita(estado);
CREATE INDEX idx_visita_fecha  ON visita(fecha_programada);
