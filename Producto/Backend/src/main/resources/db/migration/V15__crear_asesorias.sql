-- =====================================================
-- V15: asesorías (RF22–RF25) — asesorías por accidentes o
-- fiscalizaciones. Núcleo del subdominio. Las entidades
-- satélite (accidente, fiscalizacion, multa, propuesta_mejora)
-- se agregan en una migración posterior.
-- Además cierra la FK informe.id_asesoria -> asesoria, que
-- quedó nullable y sin FK en V11 (el informe de asesoría
-- reutiliza la tabla informe, RF15).
-- =====================================================

CREATE TABLE asesoria (
    id_asesoria          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa           BIGINT        NOT NULL,
    id_profesional       BIGINT        NOT NULL,
    fecha_solicitud      DATE          NOT NULL,
    fecha_atencion       DATE,
    motivo               VARCHAR(500)  NOT NULL,
    tipo                 VARCHAR(20)   NOT NULL,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'SOLICITADA',
    es_asesoria_extra    BOOLEAN       NOT NULL DEFAULT FALSE,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_asesoria_empresa     FOREIGN KEY (id_empresa)     REFERENCES empresa(id_empresa),
    CONSTRAINT fk_asesoria_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_asesoria_empresa ON asesoria(id_empresa);
CREATE INDEX idx_asesoria_estado  ON asesoria(estado);

-- Cierra la referencia informe -> asesoria (RF15 para asesorías; el informe reutiliza esta tabla).
ALTER TABLE informe
    ADD CONSTRAINT fk_informe_asesoria FOREIGN KEY (id_asesoria) REFERENCES asesoria(id_asesoria);
