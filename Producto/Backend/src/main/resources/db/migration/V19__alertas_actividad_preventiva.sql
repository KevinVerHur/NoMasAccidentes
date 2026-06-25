ALTER TABLE actividad_preventiva
    ADD COLUMN normativa VARCHAR(120) NULL AFTER descripcion,
    ADD COLUMN alerta_enviada BOOLEAN NOT NULL DEFAULT FALSE AFTER observaciones;

CREATE INDEX idx_actividad_alerta_enviada ON actividad_preventiva(alerta_enviada);
