-- ============================================================
-- RF30/RF32: flags de notificación de capacitaciones
--   recordatorio_enviado      → recordatorio al cliente 3 días antes (RF30)
--   incumplimiento_notificado → aviso al admin si quedó sin realizar (RF32)
-- Patrón anti-duplicado idéntico a visita.recordatorio_enviado
-- y actividad_preventiva.alerta_enviada.
-- ============================================================

ALTER TABLE capacitacion
    ADD COLUMN recordatorio_enviado      BOOLEAN NOT NULL DEFAULT FALSE AFTER estado,
    ADD COLUMN incumplimiento_notificado BOOLEAN NOT NULL DEFAULT FALSE AFTER recordatorio_enviado;

-- En MySQL los índices van sin IF NOT EXISTS
CREATE INDEX idx_cap_recordatorio   ON capacitacion(recordatorio_enviado);
CREATE INDEX idx_cap_incumplimiento ON capacitacion(incumplimiento_notificado);
