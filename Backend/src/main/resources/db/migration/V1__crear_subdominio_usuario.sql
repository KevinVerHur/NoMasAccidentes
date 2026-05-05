-- =====================================================
-- V1: subdominio usuario 
-- Tablas: rol, usuario, profesional
-- =====================================================

-- ---------- ROL ----------
CREATE TABLE rol (
    id_rol           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(40) NOT NULL UNIQUE,
    descripcion      VARCHAR(200),
    -- Auditoría heredada de BaseEntity
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- USUARIO ----------
CREATE TABLE usuario (
     id_usuario       BIGINT AUTO_INCREMENT PRIMARY KEY,
     email            VARCHAR(120) NOT NULL UNIQUE,
     password_hash    VARCHAR(255) NOT NULL,
     nombre           VARCHAR(120) NOT NULL,
     apellido         VARCHAR(120) NOT NULL,
     id_rol           BIGINT NOT NULL,
     activo           BOOLEAN NOT NULL DEFAULT TRUE,
     ultimo_acceso    DATETIME,
     fecha_creacion       DATETIME,
     fecha_actualizacion  DATETIME,
     creado_por           VARCHAR(80),
     actualizado_por      VARCHAR(80),
     CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_usuario_email ON usuario(email);
CREATE INDEX idx_usuario_activo ON usuario(activo);

-- ---------- PROFESIONAL ----------
-- Profesionales de prevención de riesgos asociados a un usuario del sistema.
CREATE TABLE profesional (
     id_profesional   BIGINT AUTO_INCREMENT PRIMARY KEY,
     id_usuario       BIGINT NOT NULL UNIQUE,
     rut              VARCHAR(12) NOT NULL UNIQUE,
     telefono         VARCHAR(20),
     especialidad     VARCHAR(120),
     -- Coordenadas de ubicación actual (RF — geolocalización en visitas)
     latitud          DECIMAL(9,6),
     longitud         DECIMAL(9,6),
     activo           BOOLEAN NOT NULL DEFAULT TRUE,
     fecha_creacion       DATETIME,
     fecha_actualizacion  DATETIME,
     creado_por           VARCHAR(80),
     actualizado_por      VARCHAR(80),
     CONSTRAINT fk_profesional_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- Datos semilla: roles iniciales ----------
INSERT INTO rol (nombre, descripcion) VALUES
      ('ADMIN',        'Administrador del sistema'),
      ('PROFESIONAL',  'Profesional de prevención de riesgos'),
      ('CLIENTE',      'Contacto cliente de empresa adherida'),
      ('CAPACITADOR',  'Capacitador interno o externo');
