-- =====================================================================
-- MER No Más Accidentes — DDL alineado con el backend implementado.
-- Modelo separado: rubro (catálogo) 1─N empresa (persona jurídica) 1─N
-- cliente (representante/contacto). Las actividades operativas cuelgan de
-- la EMPRESA; el cliente es solo el contacto con acceso al portal.
-- Auditoría vía Hibernate Envers (revinfo, *_aud) en vez de tabla historial.
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


CREATE TABLE rol (
    id_rol               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre               VARCHAR(40)  NOT NULL UNIQUE,
    descripcion          VARCHAR(200),
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE usuario (
    id_usuario           BIGINT        AUTO_INCREMENT PRIMARY KEY,
    email                VARCHAR(120)  NOT NULL UNIQUE,
    password_hash        VARCHAR(255)  NOT NULL,
    nombre               VARCHAR(120)  NOT NULL,
    apellido             VARCHAR(120)  NOT NULL,
    id_rol               BIGINT        NOT NULL,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    ultimo_acceso        DATETIME,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_usuario_email  ON usuario(email);
CREATE INDEX idx_usuario_activo ON usuario(activo);


CREATE TABLE password_reset_token (
    id_token             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    token                VARCHAR(36)   NOT NULL UNIQUE,
    id_usuario           BIGINT        NOT NULL,
    expira_en            DATETIME      NOT NULL,
    usado                BOOLEAN       NOT NULL DEFAULT FALSE,
    creado_en            DATETIME      NOT NULL,
    CONSTRAINT fk_prt_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_prt_token ON password_reset_token(token);


CREATE TABLE profesional (
    id_profesional       BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_usuario           BIGINT        NOT NULL,
    rut                  VARCHAR(12)   NOT NULL,
    telefono             VARCHAR(20),
    especialidad         VARCHAR(120),
    estado               VARCHAR(30)   NOT NULL DEFAULT 'DISPONIBLE',
    latitud              DECIMAL(9,6),
    longitud             DECIMAL(9,6),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT uq_profesional_usuario UNIQUE (id_usuario),
    CONSTRAINT uq_profesional_rut     UNIQUE (rut),
    CONSTRAINT fk_profesional_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Historial de geolocalización del profesional en terreno (RF05).
CREATE TABLE ubicacion_profesional (
    id_ubicacion         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_profesional       BIGINT        NOT NULL,
    latitud              DECIMAL(9,6)  NOT NULL,
    longitud             DECIMAL(9,6)  NOT NULL,
    fecha_registro       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ubicacion_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_ubicacion_prof_fecha ON ubicacion_profesional(id_profesional, fecha_registro);


-- Configuracion de la consultora dueña del sistema (single-tenant): sus datos
-- de identificacion aparecen en encabezados de PDF y correos.
CREATE TABLE configuracion_empresa (
    id_configuracion_empresa BIGINT        AUTO_INCREMENT PRIMARY KEY,
    nombre_empresa           VARCHAR(200)  NOT NULL,
    rut                      VARCHAR(12)   NOT NULL,
    email_contacto           VARCHAR(120)  NOT NULL,
    telefono                 VARCHAR(20),
    direccion                VARCHAR(200),
    region                   VARCHAR(80),
    activo                   BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion           DATETIME,
    fecha_actualizacion      DATETIME,
    creado_por               VARCHAR(80),
    actualizado_por          VARCHAR(80)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =====================  Cliente = rubro + empresa + representante  =====================

CREATE TABLE rubro (
    id_rubro              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    nombre                VARCHAR(80)   NOT NULL UNIQUE,
    tasa_accidentabilidad DECIMAL(5,2),
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Empresa cliente (persona jurídica). Concentra el vínculo comercial:
-- plan, estado y profesional asignado. Pertenece a un rubro.
CREATE TABLE empresa (
    id_empresa             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    razon_social           VARCHAR(200)  NOT NULL,
    rut                    VARCHAR(12)   NOT NULL UNIQUE,
    direccion              VARCHAR(200),
    comuna                 VARCHAR(80),
    id_rubro               BIGINT        NOT NULL,
    plan                   VARCHAR(40)   NOT NULL DEFAULT 'BASICO',
    cantidad_trabajadores  INT,
    estado                 VARCHAR(20)   NOT NULL DEFAULT 'ACTIVO',
    id_profesional         BIGINT,
    activo                 BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion         DATETIME,
    fecha_actualizacion    DATETIME,
    creado_por             VARCHAR(80),
    actualizado_por        VARCHAR(80),
    CONSTRAINT fk_empresa_rubro       FOREIGN KEY (id_rubro)       REFERENCES rubro(id_rubro),
    CONSTRAINT fk_empresa_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_empresa_rut    ON empresa(rut);
CREATE INDEX idx_empresa_estado ON empresa(estado);
CREATE INDEX idx_empresa_activo ON empresa(activo);
CREATE INDEX idx_empresa_rubro  ON empresa(id_rubro);


-- Cliente = representante / persona de contacto de una empresa. Una empresa
-- puede tener varios; la credencial de acceso al portal es de la persona
-- (id_usuario, nullable: puede existir un contacto sin login).
CREATE TABLE cliente (
    id_cliente             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa             BIGINT        NOT NULL,
    nombre                 VARCHAR(120)  NOT NULL,
    cargo                  VARCHAR(80),
    email                  VARCHAR(120)  NOT NULL,
    telefono               VARCHAR(20),
    id_usuario             BIGINT,
    activo                 BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion         DATETIME,
    fecha_actualizacion    DATETIME,
    creado_por             VARCHAR(80),
    actualizado_por        VARCHAR(80),
    CONSTRAINT uq_cliente_usuario UNIQUE (id_usuario),
    CONSTRAINT fk_cliente_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_cliente_empresa ON cliente(id_empresa);
CREATE INDEX idx_cliente_email   ON cliente(email);
CREATE INDEX idx_cliente_activo  ON cliente(activo);


-- =====================  Visitas y listas de chequeo (por empresa)  =====================

CREATE TABLE lista_chequeo (
    id_lista_chequeo           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    id_empresa                 BIGINT       NOT NULL UNIQUE,
    nombre                     VARCHAR(120),
    cambios_realizados_anio    INT          NOT NULL DEFAULT 0,
    anio_vigente               INT,
    fecha_ultima_modificacion  DATE,
    activo                     BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion             DATETIME,
    fecha_actualizacion        DATETIME,
    creado_por                 VARCHAR(80),
    actualizado_por            VARCHAR(80),
    CONSTRAINT fk_lista_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE item_chequeo (
    id_item              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_lista_chequeo     BIGINT        NOT NULL,
    descripcion          VARCHAR(250)  NOT NULL,
    categoria            VARCHAR(80),
    norma_legal          VARCHAR(250),
    obligatorio          BOOLEAN       NOT NULL DEFAULT TRUE,
    orden                INT,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_item_lista FOREIGN KEY (id_lista_chequeo) REFERENCES lista_chequeo(id_lista_chequeo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE visita (
    id_visita            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa           BIGINT        NOT NULL,
    id_profesional       BIGINT        NOT NULL,
    id_lista_chequeo     BIGINT        NOT NULL,
    tipo_revision        VARCHAR(20),
    fecha_programada     DATE          NOT NULL,
    fecha_inicio         DATETIME,
    fecha_fin            DATETIME,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'PROGRAMADA',
    latitud              DECIMAL(9,6),
    longitud             DECIMAL(9,6),
    observaciones        VARCHAR(2000),
    es_visita_extra      BOOLEAN       NOT NULL DEFAULT FALSE,
    recordatorio_enviado BOOLEAN       NOT NULL DEFAULT FALSE,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_visita_empresa      FOREIGN KEY (id_empresa)       REFERENCES empresa(id_empresa),
    CONSTRAINT fk_visita_profesional  FOREIGN KEY (id_profesional)   REFERENCES profesional(id_profesional),
    CONSTRAINT fk_visita_lista        FOREIGN KEY (id_lista_chequeo) REFERENCES lista_chequeo(id_lista_chequeo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_visita_estado ON visita(estado);
CREATE INDEX idx_visita_fecha  ON visita(fecha_programada);


-- Resultado del chequeo por visita (RF19/RF20): una fila por (visita, item)
-- evaluado en terreno como Cumple / No cumple / No aplica.
CREATE TABLE resultado_chequeo (
    id_resultado         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_visita            BIGINT        NOT NULL,
    id_item              BIGINT        NOT NULL,
    estado               VARCHAR(20)   NOT NULL,
    observacion          VARCHAR(500),
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT uq_resultado_visita_item UNIQUE (id_visita, id_item),
    CONSTRAINT fk_resultado_visita FOREIGN KEY (id_visita) REFERENCES visita(id_visita),
    CONSTRAINT fk_resultado_item   FOREIGN KEY (id_item)   REFERENCES item_chequeo(id_item)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_resultado_visita ON resultado_chequeo(id_visita);


-- =====================  Financiero (por empresa)  =====================

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


CREATE TABLE plan_de_pago (
    id_plan              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa           BIGINT        NOT NULL,
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
    CONSTRAINT fk_plan_empresa     FOREIGN KEY (id_empresa)     REFERENCES empresa(id_empresa),
    CONSTRAINT fk_plan_mensualidad FOREIGN KEY (id_mensualidad) REFERENCES mensualidad(id_mensualidad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE pago (
    id_pago              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_plan              BIGINT        NOT NULL,
    numero_cuota         INT           NOT NULL,
    monto                DECIMAL(10,2) NOT NULL,
    fecha_emision        DATE          NOT NULL,
    fecha_vencimiento    DATE          NOT NULL,
    fecha_pago           DATE,
    medio_pago           VARCHAR(40),
    webpay_token         VARCHAR(100),
    webpay_orden_compra  VARCHAR(50),
    estado_pago          VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    alerta_enviada       BOOLEAN       NOT NULL DEFAULT FALSE,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_pago_plan FOREIGN KEY (id_plan) REFERENCES plan_de_pago(id_plan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_pago_estado      ON pago(estado_pago);
CREATE INDEX idx_pago_vencimiento ON pago(fecha_vencimiento);


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


-- =====================  Capacitaciones (relator = profesional)  =====================

CREATE TABLE capacitacion (
    id_capacitacion            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa                 BIGINT        NOT NULL,
    curso                      VARCHAR(150)  NOT NULL,
    id_relator                 BIGINT        NOT NULL,
    fecha_programada           DATE          NOT NULL,
    hora_programada            TIME          NOT NULL,
    lugar                      VARCHAR(150)  NOT NULL,
    cupos                      INT           NOT NULL,
    objetivo                   VARCHAR(500),
    fecha_realizacion          DATE,
    estado                     VARCHAR(20)   NOT NULL DEFAULT 'PROGRAMADA',
    es_capacitacion_extra      BOOLEAN       NOT NULL DEFAULT FALSE,
    observacion_acta           VARCHAR(1000),
    recordatorio_enviado       BOOLEAN       NOT NULL DEFAULT FALSE,
    incumplimiento_notificado  BOOLEAN       NOT NULL DEFAULT FALSE,
    activo                     BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion             DATETIME,
    fecha_actualizacion        DATETIME,
    creado_por                 VARCHAR(80),
    actualizado_por            VARCHAR(80),
    CONSTRAINT fk_capacitacion_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa),
    CONSTRAINT fk_capacitacion_relator FOREIGN KEY (id_relator) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_cap_empresa ON capacitacion(id_empresa);
CREATE INDEX idx_cap_relator ON capacitacion(id_relator);
CREATE INDEX idx_cap_estado  ON capacitacion(estado);


CREATE TABLE asistente (
    id_asistente         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa           BIGINT        NOT NULL,
    rut                  VARCHAR(12)   NOT NULL UNIQUE,
    nombre               VARCHAR(80)   NOT NULL,
    apellidos            VARCHAR(120)  NOT NULL,
    cargo                VARCHAR(80),
    area                 VARCHAR(80),
    email                VARCHAR(120),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_asistente_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE asistencia (
    id_asistencia        BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_capacitacion      BIGINT        NOT NULL,
    id_asistente         BIGINT        NOT NULL,
    confirmado           BOOLEAN       NOT NULL DEFAULT FALSE,
    asistio              BOOLEAN       NOT NULL DEFAULT FALSE,
    fecha_confirmacion   DATETIME,
    firma_digital        VARCHAR(300),
    observaciones        VARCHAR(500),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT uk_asistencia            UNIQUE (id_capacitacion, id_asistente),
    CONSTRAINT fk_asistencia_capacit    FOREIGN KEY (id_capacitacion) REFERENCES capacitacion(id_capacitacion),
    CONSTRAINT fk_asistencia_asistente  FOREIGN KEY (id_asistente)    REFERENCES asistente(id_asistente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =====================  Asesorías, informe y cumplimiento  =====================

CREATE TABLE asesoria (
    id_asesoria          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa           BIGINT        NOT NULL,
    id_profesional       BIGINT        NOT NULL,
    fecha_solicitud      DATE          NOT NULL,
    fecha_atencion       DATE,
    motivo               VARCHAR(500)  NOT NULL,
    tipo                 VARCHAR(20)   NOT NULL,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'SOLICITADA',
    es_asesoria_extra    BOOLEAN       NOT NULL DEFAULT FALSE,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_asesoria_empresa     FOREIGN KEY (id_empresa)     REFERENCES empresa(id_empresa),
    CONSTRAINT fk_asesoria_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_asesoria_empresa ON asesoria(id_empresa);
CREATE INDEX idx_asesoria_estado  ON asesoria(estado);


-- Informe único: sirve tanto a visitas (id_visita) como a asesorías (id_asesoria).
CREATE TABLE informe (
    id_informe           BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_visita            BIGINT,
    id_asesoria          BIGINT,
    fecha_emision        DATE          NOT NULL,
    contenido            VARCHAR(4000),
    hallazgos            VARCHAR(2000),
    estado               VARCHAR(20)   NOT NULL DEFAULT 'GENERADO',
    url_pdf              VARCHAR(300),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_informe_visita   FOREIGN KEY (id_visita)   REFERENCES visita(id_visita),
    CONSTRAINT fk_informe_asesoria FOREIGN KEY (id_asesoria) REFERENCES asesoria(id_asesoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_informe_visita ON informe(id_visita);


CREATE TABLE propuesta_mejora (
    id_propuesta         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_informe           BIGINT        NOT NULL,
    descripcion          VARCHAR(1000) NOT NULL,
    fecha_propuesta      DATE,
    fecha_limite         DATE,
    fecha_verificacion   DATE,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    responsable          VARCHAR(120),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_propuesta_informe FOREIGN KEY (id_informe) REFERENCES informe(id_informe)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE accidente (
    id_accidente             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_asesoria              BIGINT        NOT NULL,
    fecha_ocurrencia         DATE          NOT NULL,
    descripcion              VARCHAR(2000),
    gravedad                 VARCHAR(20),
    trabajador_afectado      VARCHAR(150),
    dias_perdidos            INT,
    fue_reportado_susseso    BOOLEAN       NOT NULL DEFAULT FALSE,
    activo                   BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion           DATETIME,
    fecha_actualizacion      DATETIME,
    creado_por               VARCHAR(80),
    actualizado_por          VARCHAR(80),
    CONSTRAINT fk_accidente_asesoria FOREIGN KEY (id_asesoria) REFERENCES asesoria(id_asesoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE fiscalizacion (
    id_fiscalizacion         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_asesoria              BIGINT        NOT NULL,
    fecha                    DATE          NOT NULL,
    entidad_fiscalizadora    VARCHAR(30),
    motivo                   VARCHAR(500),
    resultado                VARCHAR(20),
    observaciones            VARCHAR(2000),
    activo                   BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion           DATETIME,
    fecha_actualizacion      DATETIME,
    creado_por               VARCHAR(80),
    actualizado_por          VARCHAR(80),
    CONSTRAINT fk_fiscal_asesoria FOREIGN KEY (id_asesoria) REFERENCES asesoria(id_asesoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE multa (
    id_multa                 BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_fiscalizacion         BIGINT        NOT NULL,
    fecha_emision            DATE          NOT NULL,
    monto                    DECIMAL(12,2) NOT NULL,
    motivo                   VARCHAR(1000),
    normativa_infringida     VARCHAR(150),
    estado_pago              VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    activo                   BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion           DATETIME,
    fecha_actualizacion      DATETIME,
    creado_por               VARCHAR(80),
    actualizado_por          VARCHAR(80),
    CONSTRAINT fk_multa_fiscal FOREIGN KEY (id_fiscalizacion) REFERENCES fiscalizacion(id_fiscalizacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =====================  Comunicaciones, actividades y reportes (por empresa)  =====================

-- Centro de llamados / consultas (RF30-RF32).
CREATE TABLE consulta (
    id_consulta          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa           BIGINT        NOT NULL,
    id_profesional       BIGINT,
    fecha_hora           DATETIME      NOT NULL,
    motivo               VARCHAR(500)  NOT NULL,
    detalle              VARCHAR(1000),
    fuera_horario        BOOLEAN       NOT NULL DEFAULT FALSE,
    costo_adicional      BOOLEAN       NOT NULL DEFAULT FALSE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_consulta_empresa     FOREIGN KEY (id_empresa)     REFERENCES empresa(id_empresa),
    CONSTRAINT fk_consulta_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_consulta_empresa_fecha ON consulta(id_empresa, fecha_hora);
CREATE INDEX idx_consulta_profesional   ON consulta(id_profesional);


CREATE TABLE actividad_preventiva (
    id_actividad          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa            BIGINT        NOT NULL,
    titulo                VARCHAR(160)  NOT NULL,
    descripcion           VARCHAR(1000),
    normativa             VARCHAR(120),
    responsable           VARCHAR(120),
    fecha_planificada     DATE          NOT NULL,
    fecha_compromiso      DATE          NOT NULL,
    fecha_cumplimiento    DATE,
    estado                VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    observaciones         VARCHAR(1000),
    alerta_enviada        BOOLEAN       NOT NULL DEFAULT FALSE,
    reportado_por_cliente BOOLEAN       NOT NULL DEFAULT FALSE,
    fecha_reporte_cliente DATETIME,
    comentario_cliente    VARCHAR(500),
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80),
    CONSTRAINT fk_actividad_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_actividad_empresa         ON actividad_preventiva(id_empresa);
CREATE INDEX idx_actividad_reporte_cliente ON actividad_preventiva(reportado_por_cliente);


CREATE TABLE reporte_mensual (
    id_reporte                BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa                BIGINT        NOT NULL,
    mes                       INT           NOT NULL,
    anio                      INT           NOT NULL,
    fecha_emision             DATE          NOT NULL,
    total_visitas             INT           NOT NULL DEFAULT 0,
    total_capacitaciones      INT           NOT NULL DEFAULT 0,
    total_asesorias           INT           NOT NULL DEFAULT 0,
    total_llamados            INT           NOT NULL DEFAULT 0,
    total_accidentes          INT           NOT NULL DEFAULT 0,
    total_multas              INT           NOT NULL DEFAULT 0,
    costos_extra              DECIMAL(10,2) NOT NULL DEFAULT 0,
    url_pdf                   VARCHAR(300),
    es_actualizacion_extra    BOOLEAN       NOT NULL DEFAULT FALSE,
    activo                    BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion            DATETIME,
    fecha_actualizacion       DATETIME,
    creado_por                VARCHAR(80),
    actualizado_por           VARCHAR(80),
    CONSTRAINT uk_reporte_periodo UNIQUE (id_empresa, mes, anio),
    CONSTRAINT fk_reporte_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_reporte_periodo ON reporte_mensual(anio, mes);


-- Bandeja de notificaciones in-app: cada notificacion cuelga de un usuario
-- destinatario y refleja el mismo evento que dispara un correo transaccional.
CREATE TABLE notificacion (
    id_notificacion      BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_usuario           BIGINT        NOT NULL,
    tipo                 VARCHAR(40)   NOT NULL,
    titulo               VARCHAR(160)  NOT NULL,
    mensaje              VARCHAR(500)  NOT NULL,
    enlace               VARCHAR(200),
    leida                BOOLEAN       NOT NULL DEFAULT FALSE,
    fecha_leida          DATETIME,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_notificacion_usuario_leida ON notificacion(id_usuario, leida);
CREATE INDEX idx_notificacion_activo        ON notificacion(activo);


-- Solicitudes del cliente desde el portal (canal web): asesoria / capacitacion
-- / visita. El admin aprueba (creando el recurso real) o rechaza.
CREATE TABLE solicitud (
    id_solicitud         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa           BIGINT        NOT NULL,
    tipo                 VARCHAR(20)   NOT NULL,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    descripcion          VARCHAR(500)  NOT NULL,
    fecha_preferida      DATE,
    es_extra             BOOLEAN       NOT NULL DEFAULT FALSE,
    respuesta_admin      VARCHAR(500),
    fecha_respuesta      DATETIME,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_solicitud_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_solicitud_empresa       ON solicitud(id_empresa);
CREATE INDEX idx_solicitud_estado        ON solicitud(estado);
CREATE INDEX idx_solicitud_activo        ON solicitud(activo);
-- RNF14: indices compuestos para las consultas administrativas de solicitudes.
CREATE INDEX idx_solicitud_empresa_fecha ON solicitud(id_empresa, fecha_creacion);
CREATE INDEX idx_solicitud_estado_fecha  ON solicitud(estado, fecha_creacion);


-- =====================  Auditoría (Hibernate Envers)  =====================

CREATE TABLE revinfo (
    rev       INT     NOT NULL AUTO_INCREMENT,
    revtstmp  BIGINT,
    PRIMARY KEY (rev)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE empresa_aud (
    id_empresa            BIGINT   NOT NULL,
    rev                   INT      NOT NULL,
    revtype               TINYINT,
    razon_social          VARCHAR(200),
    rut                   VARCHAR(12),
    direccion             VARCHAR(200),
    comuna                VARCHAR(80),
    id_rubro              BIGINT,
    plan                  VARCHAR(40),
    cantidad_trabajadores INT,
    estado                VARCHAR(20),
    id_profesional        BIGINT,
    activo                BOOLEAN,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80),
    PRIMARY KEY (id_empresa, rev),
    CONSTRAINT fk_empresa_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE profesional_aud (
    id_profesional       BIGINT   NOT NULL,
    rev                  INT      NOT NULL,
    revtype              TINYINT,
    id_usuario           BIGINT,
    rut                  VARCHAR(12),
    telefono             VARCHAR(20),
    especialidad         VARCHAR(120),
    latitud              DECIMAL(9,6),
    longitud             DECIMAL(9,6),
    estado               VARCHAR(30),
    activo               BOOLEAN,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    PRIMARY KEY (id_profesional, rev),
    CONSTRAINT fk_profesional_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


SET FOREIGN_KEY_CHECKS = 1;
