-- ============================================================
-- Mejora: el cliente puede reportar "listo de mi lado" en una
-- actividad preventiva. Es una señal para que la consultora
-- verifique y marque CUMPLIDA; NO cambia el estado por sí sola.
-- ============================================================

ALTER TABLE actividad_preventiva
    ADD COLUMN reportado_por_cliente BOOLEAN NOT NULL DEFAULT FALSE AFTER alerta_enviada,
    ADD COLUMN fecha_reporte_cliente DATETIME AFTER reportado_por_cliente,
    ADD COLUMN comentario_cliente    VARCHAR(500) AFTER fecha_reporte_cliente;

CREATE INDEX idx_actividad_reporte_cliente ON actividad_preventiva(reportado_por_cliente);
