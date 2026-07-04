-- RF30: el llamado al centro de atención lo atiende un profesional de la consultora.
-- Se registra quién lo resolvió para alimentar el historial (RF02) y el rendimiento
-- por profesional (RF41). Nullable: los admin pueden registrar sin profesional y las
-- filas previas quedan sin atribuir.
ALTER TABLE consulta
    ADD COLUMN id_profesional BIGINT NULL AFTER id_empresa,
    ADD CONSTRAINT fk_consulta_profesional
        FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional);

CREATE INDEX idx_consulta_profesional ON consulta(id_profesional);
