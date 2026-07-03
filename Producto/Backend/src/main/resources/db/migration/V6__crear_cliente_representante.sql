-- =====================================================
-- V6: cliente = representante / persona de contacto de una empresa
-- (RF06, RF30). Una empresa puede tener varios representantes.
--   - id_empresa: empresa a la que pertenece (obligatorio).
--   - id_usuario: cuenta de acceso al portal (rol CLIENTE). Nullable:
--     un contacto puede existir sin login. La credencial es de la
--     persona, no de la empresa.
-- =====================================================

CREATE TABLE cliente (
    id_cliente           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    id_empresa           BIGINT       NOT NULL,
    nombre               VARCHAR(120) NOT NULL,
    cargo                VARCHAR(80),
    email                VARCHAR(120) NOT NULL,
    telefono             VARCHAR(20),
    id_usuario           BIGINT,
    activo               BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME,
    fecha_actualizacion  DATETIME,
    creado_por           VARCHAR(80),
    actualizado_por      VARCHAR(80),
    CONSTRAINT uq_cliente_usuario UNIQUE (id_usuario),
    CONSTRAINT fk_cliente_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cliente_empresa ON cliente(id_empresa);
CREATE INDEX idx_cliente_email   ON cliente(email);
CREATE INDEX idx_cliente_activo  ON cliente(activo);
