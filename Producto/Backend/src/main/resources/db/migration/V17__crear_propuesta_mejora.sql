-- =====================================================
-- V17: propuestas de mejora (RF25) — acciones de mejora derivadas del
-- informe de una asesoría. Cuelgan del informe (el informe de asesoría
-- reutiliza la tabla `informe`, cuya FK id_asesoria se cerró en V15).
-- =====================================================

CREATE TABLE propuesta_mejora (
    id_propuesta         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_informe           BIGINT        NOT NULL,
    descripcion          VARCHAR(1000) NOT NULL,
    fecha_propuesta      DATE,
    fecha_limite         DATE,
    fecha_verificacion   DATE,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    responsable          VARCHAR(120),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_propuesta_informe FOREIGN KEY (id_informe) REFERENCES informe(id_informe)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_propuesta_informe ON propuesta_mejora(id_informe);
