-- =====================================================
-- V6: vincular cliente con su cuenta de usuario (rol CLIENTE)
-- para acceso al portal del cliente (RF06, RF30).
-- Nullable: los clientes creados antes de esta versión no tienen cuenta.
-- =====================================================

ALTER TABLE cliente
    ADD COLUMN id_usuario BIGINT NULL,
    ADD CONSTRAINT uq_cliente_usuario UNIQUE (id_usuario),
    ADD CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario);
