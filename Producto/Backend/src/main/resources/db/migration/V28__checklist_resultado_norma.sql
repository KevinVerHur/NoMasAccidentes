-- RF19/RF20: la visita en terreno marca cada ítem de la lista de chequeo como
-- Cumple / No cumple / No aplica (modelo de la lista de autoverificación de la
-- Dirección del Trabajo), y cada ítem cita su norma legal. El informe post-visita
-- refleja ese resultado y las propuestas de mejora ante incumplimiento.

-- Referencia normativa del ítem (ej. "Art. 21 D.S. 594/1999 MINSAL").
ALTER TABLE item_chequeo
    ADD COLUMN norma_legal VARCHAR(250) NULL AFTER categoria;

-- Resultado del chequeo por visita: una fila por (visita, ítem) evaluado.
CREATE TABLE resultado_chequeo (
    id_resultado        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    id_visita           BIGINT       NOT NULL,
    id_item             BIGINT       NOT NULL,
    estado              VARCHAR(20)  NOT NULL,          -- CUMPLE | NO_CUMPLE | NO_APLICA
    observacion         VARCHAR(500),
    fecha_creacion      DATETIME,
    fecha_actualizacion DATETIME,
    creado_por          VARCHAR(80),
    actualizado_por     VARCHAR(80),
    CONSTRAINT fk_resultado_visita FOREIGN KEY (id_visita) REFERENCES visita(id_visita),
    CONSTRAINT fk_resultado_item   FOREIGN KEY (id_item)   REFERENCES item_chequeo(id_item),
    CONSTRAINT uq_resultado_visita_item UNIQUE (id_visita, id_item)
);

CREATE INDEX idx_resultado_visita ON resultado_chequeo(id_visita);
