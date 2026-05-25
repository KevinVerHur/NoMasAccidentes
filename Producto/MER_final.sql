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
    ultima_ubicacion     DATETIME,
    fecha_ingreso        DATE,
    fecha_salida         DATE,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT uq_profesional_usuario UNIQUE (id_usuario),
    CONSTRAINT uq_profesional_rut     UNIQUE (rut),
    CONSTRAINT fk_profesional_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE rubro (
    id_rubro              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    nombre                VARCHAR(80)   NOT NULL UNIQUE,
    descripcion           VARCHAR(500),
    tasa_accidentabilidad DECIMAL(4,2),
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion        DATETIME,
    fecha_actualizacion   DATETIME,
    creado_por            VARCHAR(80),
    actualizado_por       VARCHAR(80)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE empresa (
    id_empresa             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_rubro               BIGINT        NOT NULL,
    razon_social           VARCHAR(200)  NOT NULL,
    rut                    VARCHAR(12)   NOT NULL UNIQUE,
    direccion              VARCHAR(200),
    comuna                 VARCHAR(80),
    telefono               VARCHAR(20),
    email_contacto         VARCHAR(120),
    cantidad_trabajadores  INT,
    fecha_registro         DATE,
    activo                 BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion         DATETIME,
    fecha_actualizacion    DATETIME,
    creado_por             VARCHAR(80),
    actualizado_por        VARCHAR(80),
    CONSTRAINT fk_empresa_rubro FOREIGN KEY (id_rubro) REFERENCES rubro(id_rubro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_empresa_rut    ON empresa(rut);
CREATE INDEX idx_empresa_activo ON empresa(activo);


CREATE TABLE cliente (
    id_cliente             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_empresa             BIGINT        NOT NULL,
    nombre_contacto        VARCHAR(120)  NOT NULL,
    cargo_contacto         VARCHAR(80),
    email                  VARCHAR(120)  NOT NULL,
    telefono               VARCHAR(20),
    plan                   VARCHAR(40)   NOT NULL DEFAULT 'BASICO',
    estado                 VARCHAR(20)   NOT NULL DEFAULT 'ACTIVO',
    fecha_inicio_servicio  DATE,
    fecha_suspension       DATE,
    id_profesional         BIGINT,
    id_usuario             BIGINT,
    activo                 BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion         DATETIME,
    fecha_actualizacion    DATETIME,
    creado_por             VARCHAR(80),
    actualizado_por        VARCHAR(80),
    CONSTRAINT uq_cliente_usuario     UNIQUE (id_usuario),
    CONSTRAINT fk_cliente_empresa     FOREIGN KEY (id_empresa)     REFERENCES empresa(id_empresa),
    CONSTRAINT fk_cliente_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional),
    CONSTRAINT fk_cliente_usuario     FOREIGN KEY (id_usuario)     REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_cliente_estado ON cliente(estado);
CREATE INDEX idx_cliente_activo ON cliente(activo);


CREATE TABLE asistente (
    id_asistente         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_cliente           BIGINT        NOT NULL,
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
    CONSTRAINT fk_asistente_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE lista_checkeo (
    id_lista_checkeo           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    id_cliente                 BIGINT       NOT NULL UNIQUE,
    nombre                     VARCHAR(120),
    cambios_realizados_anio    INT          NOT NULL DEFAULT 0,
    anio_vigente               INT,
    fecha_ultima_modificacion  DATE,
    activo                     BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion             DATETIME,
    fecha_actualizacion        DATETIME,
    creado_por                 VARCHAR(80),
    actualizado_por            VARCHAR(80),
    CONSTRAINT fk_lista_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE item_checkeo (
    id_item              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_lista_checkeo     BIGINT        NOT NULL,
    descripcion          VARCHAR(250)  NOT NULL,
    categoria            VARCHAR(80),
    obligatorio          BOOLEAN       NOT NULL DEFAULT TRUE,
    orden                INT,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_item_lista FOREIGN KEY (id_lista_checkeo) REFERENCES lista_checkeo(id_lista_checkeo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE visita (
    id_visita            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_cliente           BIGINT        NOT NULL,
    id_profesional       BIGINT        NOT NULL,
    id_lista_checkeo     BIGINT        NOT NULL,
    tipo_revision        VARCHAR(20),
    fecha_programada     DATE          NOT NULL,
    fecha_inicio         DATETIME,
    fecha_fin            DATETIME,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'PROGRAMADA',
    latitud              DECIMAL(9,6),
    longitud             DECIMAL(9,6),
    observaciones        VARCHAR(2000),
    es_visita_extra      BOOLEAN       NOT NULL DEFAULT FALSE,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_visita_cliente      FOREIGN KEY (id_cliente)       REFERENCES cliente(id_cliente),
    CONSTRAINT fk_visita_profesional  FOREIGN KEY (id_profesional)   REFERENCES profesional(id_profesional),
    CONSTRAINT fk_visita_lista        FOREIGN KEY (id_lista_checkeo) REFERENCES lista_checkeo(id_lista_checkeo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_visita_estado ON visita(estado);
CREATE INDEX idx_visita_fecha  ON visita(fecha_programada);


CREATE TABLE capacitador (
    id_capacitador       BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_profesional       BIGINT,
    es_externo           BOOLEAN       NOT NULL,
    rut_externo          VARCHAR(12),
    nombre_externo       VARCHAR(80),
    apellidos_externo    VARCHAR(120),
    email_externo        VARCHAR(120),
    telefono_externo     VARCHAR(20),
    especialidad         VARCHAR(120),
    certificaciones      VARCHAR(500),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_capacitador_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE capacitacion (
    id_capacitacion         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_cliente              BIGINT        NOT NULL,
    id_capacitador          BIGINT        NOT NULL,
    tema                    VARCHAR(150)  NOT NULL,
    fecha_programacion      DATE          NOT NULL,
    fecha_realizacion       DATE,
    duracion_minutos        INT,
    lugar                   VARCHAR(200),
    estado                  VARCHAR(20)   NOT NULL DEFAULT 'PROGRAMADA',
    es_capacitacion_extra   BOOLEAN       NOT NULL DEFAULT FALSE,
    activo                  BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion          DATETIME,
    fecha_actualizacion     DATETIME,
    creado_por              VARCHAR(80),
    actualizado_por         VARCHAR(80),
    CONSTRAINT fk_capacitacion_cliente     FOREIGN KEY (id_cliente)     REFERENCES cliente(id_cliente),
    CONSTRAINT fk_capacitacion_capacitador FOREIGN KEY (id_capacitador) REFERENCES capacitador(id_capacitador)
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


CREATE TABLE asesoria (
    id_asesoria          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_cliente           BIGINT        NOT NULL,
    id_profesional       BIGINT        NOT NULL,
    fecha_solicitud      DATE,
    fecha_atencion       DATE,
    motivo               VARCHAR(500),
    tipo                 VARCHAR(20),
    estado               VARCHAR(20)   NOT NULL DEFAULT 'SOLICITADA',
    es_asesoria_extra    BOOLEAN       NOT NULL DEFAULT FALSE,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_asesoria_cliente      FOREIGN KEY (id_cliente)     REFERENCES cliente(id_cliente),
    CONSTRAINT fk_asesoria_profesional  FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE informe_asesoria (
    id_informe           BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_visita            BIGINT,
    id_asesoria          BIGINT,
    fecha_emision        DATE          NOT NULL,
    contenido            VARCHAR(4000),
    hallazgos            VARCHAR(2000),
    estado               VARCHAR(20)   NOT NULL DEFAULT 'BORRADOR',
    url_pdf              VARCHAR(300),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_informe_visita   FOREIGN KEY (id_visita)   REFERENCES visita(id_visita),
    CONSTRAINT fk_informe_asesoria FOREIGN KEY (id_asesoria) REFERENCES asesoria(id_asesoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


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
    CONSTRAINT fk_propuesta_informe FOREIGN KEY (id_informe) REFERENCES informe_asesoria(id_informe)
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
    entidad_fiscalizadora    VARCHAR(20),
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


CREATE TABLE llamado (
    id_llamado             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_cliente             BIGINT        NOT NULL,
    id_profesional         BIGINT        NOT NULL,
    fecha_hora             DATETIME      NOT NULL,
    motivo                 VARCHAR(500),
    resolucion             VARCHAR(1000),
    duracion_minutos       INT,
    fuera_horario          BOOLEAN       NOT NULL DEFAULT FALSE,
    genero_costo_extra     BOOLEAN       NOT NULL DEFAULT FALSE,
    activo                 BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion         DATETIME,
    fecha_actualizacion    DATETIME,
    creado_por             VARCHAR(80),
    actualizado_por        VARCHAR(80),
    CONSTRAINT fk_llamado_cliente     FOREIGN KEY (id_cliente)     REFERENCES cliente(id_cliente),
    CONSTRAINT fk_llamado_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE mensualidad (
    id_mensualidad               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    nombre_plan                  VARCHAR(80),
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
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_pago_plan FOREIGN KEY (id_plan) REFERENCES plan_de_pago(id_plan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_pago_estado ON pago(estado_pago);


CREATE TABLE cobro_extra (
    id_cobro_extra       BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_pago              BIGINT        NOT NULL,
    tipo_cobro           VARCHAR(30)   NOT NULL,
    id_origen            BIGINT        NOT NULL,
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


CREATE TABLE reporte_mensual (
    id_reporte                BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_cliente                BIGINT        NOT NULL,
    mes                       INT           NOT NULL,
    anio                      INT           NOT NULL,
    fecha_emision             DATE          NOT NULL,
    total_visitas             INT,
    total_capacitaciones      INT,
    total_asesorias           INT,
    total_llamados            INT,
    total_accidentes          INT,
    total_multas              INT,
    costos_extra              DECIMAL(10,2),
    url_pdf                   VARCHAR(300),
    es_actualizacion_extra    BOOLEAN       NOT NULL DEFAULT FALSE,
    activo                    BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion            DATETIME,
    fecha_actualizacion       DATETIME,
    creado_por                VARCHAR(80),
    actualizado_por           VARCHAR(80),
    CONSTRAINT uk_reporte_periodo UNIQUE (id_cliente, mes, anio),
    CONSTRAINT fk_reporte_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE historial (
    id_historial         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    id_profesional       BIGINT        NOT NULL,
    fecha_accion         DATETIME      NOT NULL,
    tipo_accion          VARCHAR(60),
    entidad_afectada     VARCHAR(40),
    id_referencia        BIGINT,
    descripcion          VARCHAR(500),
    genero_costo_extra   BOOLEAN       NOT NULL DEFAULT FALSE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT fk_historial_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_historial_fecha ON historial(fecha_accion);


SET FOREIGN_KEY_CHECKS = 1;
