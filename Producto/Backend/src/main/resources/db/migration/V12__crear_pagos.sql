-- =====================================================
-- V13: subdominio financiero / pagos (RF08–RF12)
--   RF08: planes de pago mensuales por cliente
--   RF09: registrar pagos
--   RF10: historial de pagos
--   RF11: control de morosidades
--   RF12: suspensión por pagos atrasados
-- =====================================================

-- Catálogo de planes (mensualidades) — RF08.
CREATE TABLE mensualidad (
    id_mensualidad               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    nombre_plan                  VARCHAR(80)   NOT NULL UNIQUE,
    monto_base                   DECIMAL(10,2) NOT NULL,
    visitas_incluidas            INT,
    asesorias_incluidas          INT,
    capacitaciones_incluidas     INT,
    costo_visita_extra           DECIMAL(10,2),
    costo_asesoria_extra         DECIMAL(10,2),
    costo_capacitacion_extra     DECIMAL(10,2),
    costo_llamado_fuera_horario  DECIMAL(10,2),
    activo                       BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion               DATETIME,
    fecha_actualizacion          DATETIME,
    creado_por                   VARCHAR(80),
    actualizado_por              VARCHAR(80)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Contrato del cliente con un plan (RF08).
CREATE TABLE plan_de_pago (
    id_plan              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_cliente           BIGINT        NOT NULL,
    id_mensualidad       BIGINT        NOT NULL,
    fecha_inicio         DATE          NOT NULL,
    fecha_termino        DATE,
    cuotas_totales       INT,
    periodicidad         VARCHAR(20)   NOT NULL DEFAULT 'MENSUAL',
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_plan_cliente     FOREIGN KEY (id_cliente)     REFERENCES cliente(id_cliente),
    CONSTRAINT fk_plan_mensualidad FOREIGN KEY (id_mensualidad) REFERENCES mensualidad(id_mensualidad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cuotas de pago (RF09, RF10).
CREATE TABLE pago (
    id_pago              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_plan              BIGINT        NOT NULL,
    numero_cuota         INT           NOT NULL,
    monto                DECIMAL(10,2) NOT NULL,
    fecha_emision        DATE          NOT NULL,
    fecha_vencimiento    DATE          NOT NULL,
    fecha_pago           DATE,
    medio_pago           VARCHAR(40),
    estado_pago          VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    alerta_enviada       BOOLEAN       NOT NULL DEFAULT FALSE,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_pago_plan FOREIGN KEY (id_plan) REFERENCES plan_de_pago(id_plan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pago_estado       ON pago(estado_pago);
CREATE INDEX idx_pago_vencimiento  ON pago(fecha_vencimiento);

-- Cobros extra asociados a una cuota (RF21/24/28). id_origen es polimórfico (sin FK).
CREATE TABLE cobro_extra (
    id_cobro_extra       BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_pago              BIGINT        NOT NULL,
    tipo_cobro           VARCHAR(30)   NOT NULL,
    id_origen            BIGINT,
    descripcion          VARCHAR(500),
    monto                DECIMAL(10,2) NOT NULL,
    fecha_generacion     DATE          NOT NULL,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_cobro_pago FOREIGN KEY (id_pago) REFERENCES pago(id_pago)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
