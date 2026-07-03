-- ============================================================
-- Bandeja de notificaciones in-app (Fase 4).
-- Cada notificación cuelga de un usuario destinatario (id_usuario)
-- y registra el mismo evento que dispara un correo transaccional:
--   VISITA_PLANIFICADA      → al profesional asignado
--   CAPACITACION_PROGRAMADA → al cliente (representante con acceso)
--   ASESORIA_REGISTRADA     → al cliente (representante con acceso)
-- El enlace permite navegar al recurso relacionado desde el frontend.
-- ============================================================

CREATE TABLE notificacion (
    id_notificacion       BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario            BIGINT NOT NULL,
    tipo                  VARCHAR(40) NOT NULL,
    titulo                VARCHAR(160) NOT NULL,
    mensaje               VARCHAR(500) NOT NULL,
    enlace                VARCHAR(200),
    leida                 BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_leida           DATETIME,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80),
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notificacion_usuario_leida ON notificacion(id_usuario, leida);
CREATE INDEX idx_notificacion_activo ON notificacion(activo);
