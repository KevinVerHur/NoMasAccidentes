-- =====================================================
-- V31: cuotas por pagar para el cliente de prueba, para poder ejercitar el
-- pago en línea Webpay (RF09) desde el portal del cliente.
--   - Cuota 5 (mayo): vencida e impaga -> ATRASADO (caso "Pagar" cuota atrasada).
--   - Cuota 7 (julio): por vencer -> PENDIENTE (caso "Pagar" cuota pendiente).
-- Se cuelgan del mismo plan sembrado en V18 (empresa RUT 99999999-9).
-- =====================================================

SET @eid := (SELECT id_empresa FROM empresa WHERE rut = '99999999-9');
SET @planid := (SELECT id_plan FROM plan_de_pago WHERE id_empresa = @eid AND creado_por = 'flyway-seed' LIMIT 1);

INSERT INTO pago (id_plan, numero_cuota, monto, fecha_emision, fecha_vencimiento, fecha_pago, medio_pago, estado_pago, alerta_enviada, activo, creado_por) VALUES
 (@planid, 5, 120000.00, '2026-05-01', '2026-05-10', NULL, NULL, 'ATRASADO',  FALSE, TRUE, 'flyway-seed'),
 (@planid, 7, 120000.00, '2026-07-01', '2026-07-10', NULL, NULL, 'PENDIENTE', FALSE, TRUE, 'flyway-seed');
