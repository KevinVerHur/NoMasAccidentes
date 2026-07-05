-- RNF14: indices compuestos puntuales para consultas administrativas de solicitudes.
-- V33 ya existe como seed de demo; por eso esta mejora usa V34.
-- No se agregan indices redundantes sobre FK cubiertas por InnoDB o por indices existentes.

CREATE INDEX idx_solicitud_empresa_fecha
ON solicitud(id_empresa, fecha_creacion);

CREATE INDEX idx_solicitud_estado_fecha
ON solicitud(estado, fecha_creacion);
