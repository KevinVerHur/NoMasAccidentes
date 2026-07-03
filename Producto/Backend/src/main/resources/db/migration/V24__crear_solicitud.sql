-- ============================================================
-- Solicitudes del cliente (mejora más allá del RF: canal web).
-- El cliente solicita una asesoría/capacitación/visita desde el
-- portal; el admin aprueba (creando el recurso real, marcando si
-- es extra según el plan) o rechaza. Ambas transiciones notifican.
--   PENDIENTE -> APROBADA | RECHAZADA
-- ============================================================

CREATE TABLE solicitud (
    id_solicitud          BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_empresa            BIGINT NOT NULL,
    tipo                  VARCHAR(20) NOT NULL,          -- ASESORIA | CAPACITACION | VISITA
    estado                VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    descripcion           VARCHAR(500) NOT NULL,
    fecha_preferida       DATE,
    es_extra              BOOLEAN NOT NULL DEFAULT FALSE,
    respuesta_admin       VARCHAR(500),
    fecha_respuesta       DATETIME,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80),
    CONSTRAINT fk_solicitud_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_solicitud_empresa ON solicitud(id_empresa);
CREATE INDEX idx_solicitud_estado ON solicitud(estado);
CREATE INDEX idx_solicitud_activo ON solicitud(activo);
