-- =====================================================
-- V2: usuario admin semilla para bootstrap
-- Email: admin@nma.cl  /  Password: 123456 (bcrypt cost 12)
-- =====================================================

INSERT INTO usuario (email, password_hash, nombre, apellido, id_rol, activo, creado_por)
VALUES (
    'admin@nma.cl',
    '$2a$12$GUHcAStrkXr5LjtHLNsiuemnlK2lC/JvDMeEkviVKAHmC/8K1ooX2',
    'Admin',
    'Root',
    (SELECT id_rol FROM rol WHERE nombre = 'ADMIN'),
    TRUE,
    'flyway-seed'
);
