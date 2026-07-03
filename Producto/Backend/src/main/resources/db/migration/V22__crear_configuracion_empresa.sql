CREATE TABLE configuracion_empresa (
    id_configuracion_empresa BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_empresa VARCHAR(200) NOT NULL,
    rut VARCHAR(12) NOT NULL,
    email_contacto VARCHAR(120) NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    region VARCHAR(80),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME,
    fecha_actualizacion DATETIME,
    creado_por VARCHAR(80),
    actualizado_por VARCHAR(80)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO configuracion_empresa (
    nombre_empresa,
    rut,
    email_contacto,
    telefono,
    direccion,
    region,
    activo
) VALUES (
    'No Mas Accidentes SpA',
    '77111222-3',
    'info@nomasaccidentes.cl',
    '+56 2 2222 2222',
    'Av. Principal 456',
    'Metropolitana',
    TRUE
);