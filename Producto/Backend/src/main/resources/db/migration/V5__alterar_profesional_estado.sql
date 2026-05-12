-- RF04: Agregar estado operativo al profesional (DISPONIBLE, EN_VISITA, EN_CAPACITACION)
ALTER TABLE profesional
    ADD COLUMN estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE';
