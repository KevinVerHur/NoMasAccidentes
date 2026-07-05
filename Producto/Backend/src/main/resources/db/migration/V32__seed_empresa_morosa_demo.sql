-- =====================================================
-- V32: empresa demo para ejercitar el control de morosidad (RF11/RF12).
--   Arranca en estado ACTIVO con 2 cuotas PENDIENTE ya vencidas, de modo que
--   el flujo completo sea demostrable sobre una MISMA empresa:
--     1) "Evaluar morosidad"  -> marca las 2 cuotas ATRASADO y pasa ACTIVO -> MOROSO.
--     2) "Suspender morosos"   -> con >= 2 cuotas atrasadas, pasa MOROSO -> SUSPENDIDO.
--   (La empresa de V18 arranca SUSPENDIDA por el caso cp13, por lo que no sirve
--    para ver esas transiciones; por eso se usa una empresa aparte.)
-- =====================================================

-- ---------- Empresa cliente (persona jurídica) en estado ACTIVO ----------
INSERT INTO empresa (razon_social, rut, direccion, comuna, id_rubro, plan, cantidad_trabajadores, estado, activo, creado_por)
VALUES (
    'Empresa Morosa Demo SpA', '88888888-8', 'Camino Real 1500', 'Maipú',
    (SELECT id_rubro FROM rubro WHERE nombre = 'Transporte'),
    'BASICO', 30, 'ACTIVO', TRUE, 'flyway-seed'
);

SET @meid := (SELECT id_empresa FROM empresa WHERE rut = '88888888-8');

-- ---------- Representante (contacto sin acceso al portal) ----------
INSERT INTO cliente (id_empresa, nombre, cargo, email, telefono, id_usuario, activo, creado_por)
VALUES (
    @meid, 'Contacto Morosa', 'Administración',
    'contacto.morosa@nma.cl', '911110000', NULL, TRUE, 'flyway-seed'
);

-- ---------- Plan de pago (reutiliza el PLAN_BASICO sembrado en V26) ----------
SET @mbid := (SELECT id_mensualidad FROM mensualidad WHERE nombre_plan = 'PLAN_BASICO');

INSERT INTO plan_de_pago (id_empresa, id_mensualidad, fecha_inicio, fecha_termino, cuotas_totales, periodicidad, activo, creado_por)
VALUES (@meid, @mbid, '2026-01-01', '2026-12-31', 12, 'MENSUAL', TRUE, 'flyway-seed');
SET @mplanid := (SELECT id_plan FROM plan_de_pago WHERE id_empresa = @meid AND creado_por = 'flyway-seed' LIMIT 1);

-- ---------- 2 cuotas PENDIENTE ya vencidas (para que "Evaluar morosidad" las tome) ----------
INSERT INTO pago (id_plan, numero_cuota, monto, fecha_emision, fecha_vencimiento, fecha_pago, medio_pago, estado_pago, alerta_enviada, activo, creado_por) VALUES
 (@mplanid, 5, 120000.00, '2026-05-01', '2026-05-10', NULL, NULL, 'PENDIENTE', FALSE, TRUE, 'flyway-seed'),
 (@mplanid, 6, 120000.00, '2026-06-01', '2026-06-10', NULL, NULL, 'PENDIENTE', FALSE, TRUE, 'flyway-seed');
