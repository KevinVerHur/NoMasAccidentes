-- =====================================================
-- V18: cliente de prueba para los E2E (Selenium)
--   - cp06: login con un rol NO-ADMIN (CLIENTE) que existe siempre.
--   - cp13: reactivar un cliente que arranca en estado SUSPENDIDO.
-- Credenciales: cliente.test@nma.cl / 123456 (mismo hash bcrypt cost 12 del admin).
-- El login solo valida usuario.activo, no cliente.estado, por lo que un cliente
-- suspendido igual puede iniciar sesión.
-- =====================================================

INSERT INTO usuario (email, password_hash, nombre, apellido, id_rol, activo, creado_por)
VALUES (
    'cliente.test@nma.cl',
    '$2a$12$GUHcAStrkXr5LjtHLNsiuemnlK2lC/JvDMeEkviVKAHmC/8K1ooX2', -- 123456
    'Cliente',
    'Test',
    (SELECT id_rol FROM rol WHERE nombre = 'CLIENTE'),
    TRUE,
    'flyway-seed'
);

INSERT INTO cliente (razon_social, rut, nombre_contacto, email, telefono, rubro, plan, estado, id_usuario, activo, creado_por)
VALUES (
    'Empresa Cliente Test SpA',
    '99999999-9',
    'Cliente Test',
    'cliente.test@nma.cl',
    '912340000',
    'Servicios',
    'BASICO',
    'SUSPENDIDO',
    (SELECT id_usuario FROM usuario WHERE email = 'cliente.test@nma.cl'),
    TRUE,
    'flyway-seed'
);
