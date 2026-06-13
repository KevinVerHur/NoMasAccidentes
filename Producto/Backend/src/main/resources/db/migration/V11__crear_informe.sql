-- =====================================================
-- V12: informes (RF15) — informe posterior a cada visita.
-- Tabla genérica: hoy se usa para visitas (id_visita);
-- id_asesoria queda nullable y sin FK para el futuro módulo de asesorías.
-- =====================================================

CREATE TABLE informe (
    id_informe           BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_visita            BIGINT,
    id_asesoria          BIGINT,
    fecha_emision        DATE          NOT NULL,
    contenido            VARCHAR(4000),
    hallazgos            VARCHAR(2000),
    estado               VARCHAR(20)   NOT NULL DEFAULT 'GENERADO',
    url_pdf              VARCHAR(300),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_informe_visita FOREIGN KEY (id_visita) REFERENCES visita(id_visita)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_informe_visita ON informe(id_visita);
