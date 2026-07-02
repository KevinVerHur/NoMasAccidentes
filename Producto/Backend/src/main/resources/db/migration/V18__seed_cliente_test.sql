-- =====================================================
-- V18: empresa de prueba + representante con login + datos demo.
--   - cp06: login con un rol NO-ADMIN (CLIENTE) que existe siempre.
--   - cp13: reactivar una empresa que arranca en estado SUSPENDIDO.
--   - Demo RF38-42: la empresa tiene un año de actividad (feb–jun 2026)
--     repartida en visitas, capacitaciones, asesorías, accidentes,
--     fiscalización/multa, consultas y cobros extra, para que los
--     reportes mensuales y los gráficos de indicadores tengan datos.
-- Modelo separado: empresa (jurídica) + cliente/representante (persona
-- con la credencial). La actividad operativa cuelga de la empresa.
-- Credenciales representante:  cliente.test@nma.cl / 123456
-- Credenciales profesionales:  ana.prev@nma.cl, carlos.prev@nma.cl / 123456
-- (mismo hash bcrypt cost 12 del admin). El login solo valida usuario.activo,
-- no empresa.estado, por lo que una empresa suspendida igual permite iniciar sesión.
-- =====================================================

-- ---------- Usuario de acceso del representante ----------
INSERT INTO usuario (email, password_hash, nombre, apellido, id_rol, activo, creado_por)
VALUES (
    'cliente.test@nma.cl',
    '$2a$12$GUHcAStrkXr5LjtHLNsiuemnlK2lC/JvDMeEkviVKAHmC/8K1ooX2', -- 123456
    'Cliente', 'Test',
    (SELECT id_rol FROM rol WHERE nombre = 'CLIENTE'),
    TRUE, 'flyway-seed'
);

-- ---------- Empresa cliente (persona jurídica) ----------
INSERT INTO empresa (razon_social, rut, direccion, comuna, id_rubro, plan, cantidad_trabajadores, estado, activo, creado_por)
VALUES (
    'Empresa Cliente Test SpA', '99999999-9', 'Av. Siempre Viva 742', 'Santiago',
    (SELECT id_rubro FROM rubro WHERE nombre = 'Transporte'),
    'BASICO', 50, 'SUSPENDIDO', TRUE, 'flyway-seed'
);

SET @eid := (SELECT id_empresa FROM empresa WHERE rut = '99999999-9');

-- ---------- Representante de la empresa (con acceso al portal) ----------
INSERT INTO cliente (id_empresa, nombre, cargo, email, telefono, id_usuario, activo, creado_por)
VALUES (
    @eid, 'Cliente Test', 'Encargado de Prevención',
    'cliente.test@nma.cl', '912340000',
    (SELECT id_usuario FROM usuario WHERE email = 'cliente.test@nma.cl'),
    TRUE, 'flyway-seed'
);

-- ---------- Profesionales demo ----------
INSERT INTO usuario (email, password_hash, nombre, apellido, id_rol, activo, creado_por) VALUES
 ('ana.prev@nma.cl',    '$2a$12$GUHcAStrkXr5LjtHLNsiuemnlK2lC/JvDMeEkviVKAHmC/8K1ooX2', 'Ana',    'Prevención', (SELECT id_rol FROM rol WHERE nombre = 'PROFESIONAL'), TRUE, 'flyway-seed'),
 ('carlos.prev@nma.cl', '$2a$12$GUHcAStrkXr5LjtHLNsiuemnlK2lC/JvDMeEkviVKAHmC/8K1ooX2', 'Carlos', 'Riesgos',    (SELECT id_rol FROM rol WHERE nombre = 'PROFESIONAL'), TRUE, 'flyway-seed');

INSERT INTO profesional (id_usuario, rut, telefono, especialidad, estado, activo, creado_por) VALUES
 ((SELECT id_usuario FROM usuario WHERE email = 'ana.prev@nma.cl'),    '11111111-1', '900000001', 'Prevención de riesgos', 'DISPONIBLE', TRUE, 'flyway-seed'),
 ((SELECT id_usuario FROM usuario WHERE email = 'carlos.prev@nma.cl'), '22222222-2', '900000002', 'Higiene ocupacional',   'DISPONIBLE', TRUE, 'flyway-seed');

SET @pa := (SELECT id_profesional FROM profesional WHERE rut = '11111111-1');
SET @pb := (SELECT id_profesional FROM profesional WHERE rut = '22222222-2');

-- ---------- Lista de chequeo de la empresa ----------
INSERT INTO lista_chequeo (id_empresa, nombre, cambios_realizados_anio, anio_vigente, activo, creado_por)
VALUES (@eid, 'Lista por defecto', 0, 2026, TRUE, 'flyway-seed');
SET @lid := (SELECT id_lista_chequeo FROM lista_chequeo WHERE id_empresa = @eid);

-- ---------- Plan de pago ----------
INSERT INTO mensualidad (nombre_plan, monto_base, visitas_incluidas, asesorias_incluidas, capacitaciones_incluidas,
                         costo_visita_extra, costo_asesoria_extra, costo_capacitacion_extra, costo_llamado_fuera_horario, activo, creado_por)
VALUES ('PLAN_DEMO', 120000.00, 2, 10, 1, 45000.00, 60000.00, 80000.00, 15000.00, TRUE, 'flyway-seed');
SET @mid := (SELECT id_mensualidad FROM mensualidad WHERE nombre_plan = 'PLAN_DEMO');

INSERT INTO plan_de_pago (id_empresa, id_mensualidad, fecha_inicio, fecha_termino, cuotas_totales, periodicidad, activo, creado_por)
VALUES (@eid, @mid, '2026-01-01', '2026-12-31', 12, 'MENSUAL', TRUE, 'flyway-seed');
SET @planid := (SELECT id_plan FROM plan_de_pago WHERE id_empresa = @eid AND creado_por = 'flyway-seed' LIMIT 1);

INSERT INTO pago (id_plan, numero_cuota, monto, fecha_emision, fecha_vencimiento, fecha_pago, medio_pago, estado_pago, alerta_enviada, activo, creado_por)
VALUES (@planid, 6, 120000.00, '2026-06-01', '2026-06-10', '2026-06-05', 'TRANSFERENCIA', 'PAGADO', FALSE, TRUE, 'flyway-seed');
SET @pagoid := (SELECT id_pago FROM pago WHERE id_plan = @planid AND creado_por = 'flyway-seed' LIMIT 1);

-- ---------- Visitas realizadas (feb–jun) + 1 programada (no cuenta) ----------
INSERT INTO visita (id_empresa, id_profesional, id_lista_chequeo, tipo_revision, fecha_programada, fecha_inicio, fecha_fin, estado, observaciones, es_visita_extra, recordatorio_enviado, activo, creado_por) VALUES
 (@eid, @pa, @lid, 'PERIODICA', '2026-02-12', '2026-02-12 09:00:00', '2026-02-12 11:00:00', 'REALIZADA', 'Visita feb', FALSE, FALSE, TRUE, 'flyway-seed'),
 (@eid, @pa, @lid, 'PERIODICA', '2026-03-15', '2026-03-15 09:00:00', '2026-03-15 10:30:00', 'REALIZADA', 'Visita mar', FALSE, FALSE, TRUE, 'flyway-seed'),
 (@eid, @pb, @lid, 'PERIODICA', '2026-04-10', '2026-04-10 09:00:00', '2026-04-10 11:15:00', 'REALIZADA', 'Visita abr', FALSE, FALSE, TRUE, 'flyway-seed'),
 (@eid, @pa, @lid, 'PERIODICA', '2026-05-20', '2026-05-20 09:00:00', '2026-05-20 10:45:00', 'REALIZADA', 'Visita may', FALSE, FALSE, TRUE, 'flyway-seed'),
 (@eid, @pa, @lid, 'PERIODICA', '2026-06-05', '2026-06-05 09:00:00', '2026-06-05 11:00:00', 'REALIZADA', 'Visita jun 1', FALSE, FALSE, TRUE, 'flyway-seed'),
 (@eid, @pb, @lid, 'PERIODICA', '2026-06-18', '2026-06-18 09:00:00', '2026-06-18 10:30:00', 'REALIZADA', 'Visita jun 2', FALSE, FALSE, TRUE, 'flyway-seed'),
 (@eid, @pa, @lid, 'PERIODICA', '2026-06-25', '2026-06-25 09:00:00', '2026-06-25 12:00:00', 'REALIZADA', 'Visita jun 3', FALSE, FALSE, TRUE, 'flyway-seed'),
 (@eid, @pa, @lid, 'PERIODICA', '2026-06-30', NULL, NULL, 'PROGRAMADA', 'No debe contar', FALSE, FALSE, TRUE, 'flyway-seed');

-- ---------- Capacitaciones realizadas ----------
INSERT INTO capacitacion (id_empresa, curso, id_relator, fecha_programada, hora_programada, lugar, cupos, objetivo, fecha_realizacion, estado, es_capacitacion_extra, activo, creado_por) VALUES
 (@eid, 'Uso de EPP',           @pa, '2026-03-20', '10:00:00', 'Sala A', 20, 'Capacitar en EPP',          '2026-03-20', 'REALIZADA', FALSE, TRUE, 'flyway-seed'),
 (@eid, 'Prevención de caídas', @pb, '2026-06-15', '10:00:00', 'Sala B', 25, 'Trabajo en altura seguro',  '2026-06-15', 'REALIZADA', FALSE, TRUE, 'flyway-seed'),
 (@eid, 'Manejo de extintores', @pa, '2026-06-22', '15:00:00', 'Patio',  30, 'Respuesta ante incendios',  '2026-06-22', 'REALIZADA', FALSE, TRUE, 'flyway-seed');

-- ---------- Asesorías atendidas (insumo de accidentes/fiscalización) ----------
INSERT INTO asesoria (id_empresa, id_profesional, fecha_solicitud, fecha_atencion, motivo, tipo, estado, es_asesoria_extra, activo, creado_por) VALUES
 (@eid, @pa, '2026-02-07', '2026-02-08', 'Accidente en bodega',     'ACCIDENTE',     'CERRADA', FALSE, TRUE, 'flyway-seed'),
 (@eid, @pb, '2026-03-04', '2026-03-05', 'Accidentes en línea',     'ACCIDENTE',     'CERRADA', FALSE, TRUE, 'flyway-seed'),
 (@eid, @pa, '2026-04-17', '2026-04-18', 'Accidente en patio',      'ACCIDENTE',     'CERRADA', FALSE, TRUE, 'flyway-seed'),
 (@eid, @pa, '2026-06-09', '2026-06-10', 'Accidente y fiscalización','FISCALIZACION','CERRADA', FALSE, TRUE, 'flyway-seed'),
 (@eid, @pb, '2026-06-19', '2026-06-20', 'Asesoría preventiva',     'ACCIDENTE',     'CERRADA', FALSE, TRUE, 'flyway-seed');

SET @as_feb := (SELECT id_asesoria FROM asesoria WHERE id_empresa = @eid AND fecha_atencion = '2026-02-08' LIMIT 1);
SET @as_mar := (SELECT id_asesoria FROM asesoria WHERE id_empresa = @eid AND fecha_atencion = '2026-03-05' LIMIT 1);
SET @as_apr := (SELECT id_asesoria FROM asesoria WHERE id_empresa = @eid AND fecha_atencion = '2026-04-18' LIMIT 1);
SET @as_jun := (SELECT id_asesoria FROM asesoria WHERE id_empresa = @eid AND fecha_atencion = '2026-06-10' LIMIT 1);

-- ---------- Accidentes (curva de accidentabilidad: feb=1, mar=2, abr=1, jun=1) ----------
INSERT INTO accidente (id_asesoria, fecha_ocurrencia, descripcion, gravedad, trabajador_afectado, dias_perdidos, fue_reportado_susseso, activo, creado_por) VALUES
 (@as_feb, '2026-02-06', 'Corte en bodega',        'LEVE',  'Juan Pérez',    2,  FALSE, TRUE, 'flyway-seed'),
 (@as_mar, '2026-03-03', 'Caída de altura',        'GRAVE', 'Pedro Soto',    10, TRUE,  TRUE, 'flyway-seed'),
 (@as_mar, '2026-03-10', 'Golpe por objeto',       'LEVE',  'Luis Rojas',    3,  FALSE, TRUE, 'flyway-seed'),
 (@as_apr, '2026-04-16', 'Resbalón en patio',      'LEVE',  'María Díaz',    1,  FALSE, TRUE, 'flyway-seed'),
 (@as_jun, '2026-06-08', 'Contacto eléctrico',     'GRAVE', 'Ana Torres',    8,  TRUE,  TRUE, 'flyway-seed');

-- ---------- Fiscalización + multa (junio) ----------
INSERT INTO fiscalizacion (id_asesoria, fecha, entidad_fiscalizadora, motivo, resultado, observaciones, activo, creado_por)
VALUES (@as_jun, '2026-06-12', 'DIRECCION_TRABAJO', 'Inspección programada', 'CON_OBSERVACIONES', 'Observaciones de seguridad', TRUE, 'flyway-seed');
SET @fid := (SELECT id_fiscalizacion FROM fiscalizacion WHERE id_asesoria = @as_jun LIMIT 1);

INSERT INTO multa (id_fiscalizacion, fecha_emision, monto, motivo, normativa_infringida, estado_pago, activo, creado_por)
VALUES (@fid, '2026-06-14', 500000.00, 'Falta de EPP', 'DS 44', 'PENDIENTE', TRUE, 'flyway-seed');

-- ---------- Consultas al centro de llamados ----------
INSERT INTO consulta (id_empresa, fecha_hora, motivo, detalle, fuera_horario, costo_adicional, creado_por) VALUES
 (@eid, '2026-05-10 11:00:00', 'Consulta normativa',  'Detalle may', FALSE, FALSE, 'flyway-seed'),
 (@eid, '2026-06-03 10:00:00', 'Consulta normativa',  'Detalle 1',   FALSE, FALSE, 'flyway-seed'),
 (@eid, '2026-06-17 19:30:00', 'Consulta urgente',    'Detalle 2',   TRUE,  TRUE,  'flyway-seed');

-- ---------- Cobros extra (junio): 45.000 + 60.000 = 105.000 ----------
INSERT INTO cobro_extra (id_pago, tipo_cobro, id_origen, descripcion, monto, fecha_generacion, activo, creado_por) VALUES
 (@pagoid, 'VISITA_EXTRA',   NULL, 'Visita adicional',   45000.00, '2026-06-22', TRUE, 'flyway-seed'),
 (@pagoid, 'ASESORIA_EXTRA', NULL, 'Asesoría adicional', 60000.00, '2026-06-25', TRUE, 'flyway-seed');
