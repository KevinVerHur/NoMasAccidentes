-- =====================================================
-- V21: reporte mensual por cliente (RF38, RF39).
-- Capa de agregación: consolida los totales del periodo
-- (visitas, capacitaciones, asesorías, llamados, accidentes,
-- multas y costos extra) en una fila por cliente/mes/año.
-- El PDF se almacena vía AlmacenamientoInformeService (S3 o
-- disco local) y su clave queda en url_pdf, igual que informe.
-- =====================================================

CREATE TABLE reporte_mensual (
    id_reporte               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_cliente               BIGINT        NOT NULL,
    mes                      INT           NOT NULL,
    anio                     INT           NOT NULL,
    fecha_emision            DATE          NOT NULL,
    total_visitas            INT           NOT NULL DEFAULT 0,
    total_capacitaciones     INT           NOT NULL DEFAULT 0,
    total_asesorias          INT           NOT NULL DEFAULT 0,
    total_llamados           INT           NOT NULL DEFAULT 0,
    total_accidentes         INT           NOT NULL DEFAULT 0,
    total_multas             INT           NOT NULL DEFAULT 0,
    costos_extra             DECIMAL(10,2) NOT NULL DEFAULT 0,
    url_pdf                  VARCHAR(300),
    es_actualizacion_extra   BOOLEAN       NOT NULL DEFAULT FALSE,
    activo                   BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion           DATETIME,
    fecha_actualizacion      DATETIME,
    creado_por               VARCHAR(80),
    actualizado_por          VARCHAR(80),
    CONSTRAINT uk_reporte_periodo UNIQUE (id_cliente, mes, anio),
    CONSTRAINT fk_reporte_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_reporte_cliente ON reporte_mensual(id_cliente);
CREATE INDEX idx_reporte_periodo ON reporte_mensual(anio, mes);
