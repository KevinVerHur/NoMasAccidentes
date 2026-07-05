-- =====================================================
-- V33: "Empresa Demo Integral SpA" (RUT 77.777.777-7), en estado ACTIVO.
-- Deja la BD lista para presentar SIN necesidad de un demo en vivo: cubre las
-- reglas de negocio que el seed base (V18) no muestra, con datos visibles al abrir
-- el sistema.
--   RF21    -> lista de chequeo con 2 cambios en el año (al límite: el 3ro se cobra)
--   RF17    -> solo 1 visita en el mes actual (bajo el mínimo de 2 -> incumplimiento)
--   RF34    -> capacitación PROGRAMADA a futuro (alimenta el recordatorio por correo)
--   RF27/28 -> 10 asesorías incluidas usadas + 1 marcada como EXTRA (cobro adicional)
--   RF31/32 -> consulta fuera de horario con costo adicional
--   RF09    -> plan al día: cuota de junio PAGADA, cuota de julio PENDIENTE
--   RF38-42 -> accidentes + fiscalización + multa para que los reportes/indicadores
--              de esta empresa tengan datos
-- Reutiliza los profesionales (ana/carlos) y el PLAN_BASICO de V18/V26.
-- Marca creado_por='demo-seed' para identificar este conjunto.
-- =====================================================

-- ---------- Empresa (persona jurídica) en estado ACTIVO ----------
INSERT INTO empresa (razon_social, rut, direccion, comuna, id_rubro, plan, cantidad_trabajadores, estado, activo, creado_por)
VALUES (
    'Empresa Demo Integral SpA', '77777777-7', 'Ruta 5 Sur 4200', 'San Bernardo',
    (SELECT id_rubro FROM rubro WHERE nombre = 'Transporte'),
    'BASICO', 40, 'ACTIVO', TRUE, 'demo-seed'
);
SET @deid := (SELECT id_empresa FROM empresa WHERE rut = '77777777-7');

-- ---------- Contacto (sin acceso al portal) ----------
INSERT INTO cliente (id_empresa, nombre, cargo, email, telefono, id_usuario, activo, creado_por)
VALUES (@deid, 'Contacto Integral', 'Jefe de Operaciones', 'contacto.integral@nma.cl', '933330000', NULL, TRUE, 'demo-seed');

-- ---------- Profesionales existentes (sembrados en V18) ----------
SET @pa := (SELECT id_profesional FROM profesional WHERE rut = '11111111-1');  -- Ana
SET @pb := (SELECT id_profesional FROM profesional WHERE rut = '22222222-2');  -- Carlos

-- ---------- RF21: lista de chequeo con 2 cambios en el año (al límite) ----------
INSERT INTO lista_chequeo (id_empresa, nombre, cambios_realizados_anio, anio_vigente, fecha_ultima_modificacion, activo, creado_por)
VALUES (@deid, 'Lista transporte', 2, 2026, '2026-06-01', TRUE, 'demo-seed');
SET @dlid := (SELECT id_lista_chequeo FROM lista_chequeo WHERE id_empresa = @deid);

INSERT INTO item_chequeo (id_lista_chequeo, descripcion, categoria, obligatorio, orden, activo, creado_por) VALUES
 (@dlid, 'Extintores vigentes y señalizados', 'Emergencias',     TRUE, 1, TRUE, 'demo-seed'),
 (@dlid, 'Uso de EPP en bodega',              'EPP',             TRUE, 2, TRUE, 'demo-seed'),
 (@dlid, 'Vías de evacuación despejadas',     'Infraestructura', TRUE, 3, TRUE, 'demo-seed');

-- ---------- RF09: plan de pago al día (junio PAGADO, julio PENDIENTE) ----------
SET @mbid := (SELECT id_mensualidad FROM mensualidad WHERE nombre_plan = 'PLAN_BASICO');
INSERT INTO plan_de_pago (id_empresa, id_mensualidad, fecha_inicio, fecha_termino, cuotas_totales, periodicidad, activo, creado_por)
VALUES (@deid, @mbid, '2026-01-01', '2026-12-31', 12, 'MENSUAL', TRUE, 'demo-seed');
SET @dplan := (SELECT id_plan FROM plan_de_pago WHERE id_empresa = @deid AND creado_por = 'demo-seed' LIMIT 1);

INSERT INTO pago (id_plan, numero_cuota, monto, fecha_emision, fecha_vencimiento, fecha_pago, medio_pago, estado_pago, alerta_enviada, activo, creado_por) VALUES
 (@dplan, 6, 120000.00, '2026-06-01', '2026-06-10', '2026-06-07', 'TRANSFERENCIA', 'PAGADO',    FALSE, TRUE, 'demo-seed'),
 (@dplan, 7, 120000.00, '2026-07-01', '2026-07-10', NULL,         NULL,            'PENDIENTE', FALSE, TRUE, 'demo-seed');

-- ---------- RF17: visitas — 2 en meses previos (ok) pero SOLO 1 en el mes actual (incumplimiento) ----------
INSERT INTO visita (id_empresa, id_profesional, id_lista_chequeo, tipo_revision, fecha_programada, fecha_inicio, fecha_fin, estado, observaciones, es_visita_extra, recordatorio_enviado, activo, creado_por) VALUES
 (@deid, @pa, @dlid, 'PERIODICA', '2026-05-14', '2026-05-14 09:00:00', '2026-05-14 10:30:00', 'REALIZADA', 'Visita mayo',        FALSE, FALSE, TRUE, 'demo-seed'),
 (@deid, @pa, @dlid, 'PERIODICA', '2026-06-11', '2026-06-11 09:00:00', '2026-06-11 10:30:00', 'REALIZADA', 'Visita junio 1',     FALSE, FALSE, TRUE, 'demo-seed'),
 (@deid, @pb, @dlid, 'PERIODICA', '2026-06-24', '2026-06-24 09:00:00', '2026-06-24 10:15:00', 'REALIZADA', 'Visita junio 2',     FALSE, FALSE, TRUE, 'demo-seed'),
 (@deid, @pa, @dlid, 'PERIODICA', '2026-07-02', '2026-07-02 09:00:00', '2026-07-02 10:20:00', 'REALIZADA', 'Visita julio (1/2)', FALSE, FALSE, TRUE, 'demo-seed');

-- ---------- RF34: capacitación PROGRAMADA a futuro (dispara recordatorio) + 1 histórica ----------
INSERT INTO capacitacion (id_empresa, curso, id_relator, fecha_programada, hora_programada, lugar, cupos, objetivo, fecha_realizacion, estado, es_capacitacion_extra, activo, creado_por) VALUES
 (@deid, 'Conducción defensiva', @pb, '2026-06-18', '10:00:00', 'Sala central', 20, 'Manejo seguro de flota', '2026-06-18', 'REALIZADA',  FALSE, TRUE, 'demo-seed'),
 (@deid, 'Manejo de cargas',     @pa, '2026-08-01', '10:00:00', 'Sala central', 25, 'Levantamiento seguro',   NULL,         'PROGRAMADA', FALSE, TRUE, 'demo-seed');

-- ---------- RF27/28: 10 asesorías incluidas usadas + 1 EXTRA (la 11a se cobra) ----------
INSERT INTO asesoria (id_empresa, id_profesional, fecha_solicitud, fecha_atencion, motivo, tipo, estado, es_asesoria_extra, activo, creado_por) VALUES
 (@deid, @pa, '2026-01-10', '2026-01-11', 'Asesoría 1 - accidente',      'ACCIDENTE',     'CERRADA', FALSE, TRUE, 'demo-seed'),
 (@deid, @pb, '2026-02-05', '2026-02-06', 'Asesoría 2 - fiscalización',  'FISCALIZACION', 'CERRADA', FALSE, TRUE, 'demo-seed'),
 (@deid, @pa, '2026-02-20', '2026-02-21', 'Asesoría 3 - accidente',      'ACCIDENTE',     'CERRADA', FALSE, TRUE, 'demo-seed'),
 (@deid, @pa, '2026-03-08', '2026-03-09', 'Asesoría 4 - accidente',      'ACCIDENTE',     'CERRADA', FALSE, TRUE, 'demo-seed'),
 (@deid, @pb, '2026-03-25', '2026-03-26', 'Asesoría 5 - fiscalización',  'FISCALIZACION', 'CERRADA', FALSE, TRUE, 'demo-seed'),
 (@deid, @pa, '2026-04-12', '2026-04-13', 'Asesoría 6 - accidente',      'ACCIDENTE',     'CERRADA', FALSE, TRUE, 'demo-seed'),
 (@deid, @pa, '2026-04-28', '2026-04-29', 'Asesoría 7 - accidente',      'ACCIDENTE',     'CERRADA', FALSE, TRUE, 'demo-seed'),
 (@deid, @pb, '2026-05-15', '2026-05-16', 'Asesoría 8 - fiscalización',  'FISCALIZACION', 'CERRADA', FALSE, TRUE, 'demo-seed'),
 (@deid, @pa, '2026-05-30', '2026-05-31', 'Asesoría 9 - accidente',      'ACCIDENTE',     'CERRADA', FALSE, TRUE, 'demo-seed'),
 (@deid, @pa, '2026-06-14', '2026-06-15', 'Asesoría 10 - accidente',     'ACCIDENTE',     'CERRADA', FALSE, TRUE, 'demo-seed'),
 (@deid, @pb, '2026-07-01', '2026-07-02', 'Asesoría 11 - EXTRA (cobro)', 'ACCIDENTE',     'CERRADA', TRUE,  TRUE, 'demo-seed');

-- ---------- Accidentes ligados a asesorías (curva de accidentabilidad) ----------
SET @da_ene := (SELECT id_asesoria FROM asesoria WHERE id_empresa = @deid AND fecha_atencion = '2026-01-11' LIMIT 1);
SET @da_mar := (SELECT id_asesoria FROM asesoria WHERE id_empresa = @deid AND fecha_atencion = '2026-03-09' LIMIT 1);
SET @da_jun := (SELECT id_asesoria FROM asesoria WHERE id_empresa = @deid AND fecha_atencion = '2026-06-15' LIMIT 1);
SET @df_may := (SELECT id_asesoria FROM asesoria WHERE id_empresa = @deid AND fecha_atencion = '2026-05-16' LIMIT 1);

INSERT INTO accidente (id_asesoria, fecha_ocurrencia, descripcion, gravedad, trabajador_afectado, dias_perdidos, fue_reportado_susseso, activo, creado_por) VALUES
 (@da_ene, '2026-01-10', 'Colisión menor en patio de maniobras', 'LEVE',  'Diego Muñoz',  2, FALSE, TRUE, 'demo-seed'),
 (@da_mar, '2026-03-07', 'Caída al descargar mercadería',        'GRAVE', 'Pablo Reyes', 12, TRUE,  TRUE, 'demo-seed'),
 (@da_jun, '2026-06-13', 'Golpe con carga suspendida',           'LEVE',  'Ines Fuentes', 4, FALSE, TRUE, 'demo-seed');

-- ---------- Fiscalización + multa (mayo) ----------
INSERT INTO fiscalizacion (id_asesoria, fecha, entidad_fiscalizadora, motivo, resultado, observaciones, activo, creado_por)
VALUES (@df_may, '2026-05-18', 'DIRECCION_TRABAJO', 'Inspección de flota', 'CON_OBSERVACIONES', 'Falta señalización de cargas', TRUE, 'demo-seed');
SET @dfid := (SELECT id_fiscalizacion FROM fiscalizacion WHERE id_asesoria = @df_may LIMIT 1);

INSERT INTO multa (id_fiscalizacion, fecha_emision, monto, motivo, normativa_infringida, estado_pago, activo, creado_por)
VALUES (@dfid, '2026-05-22', 350000.00, 'Señalización insuficiente', 'DS 44', 'PENDIENTE', TRUE, 'demo-seed');

-- ---------- RF31/32: consulta fuera de horario (L-V 9-18) con costo adicional ----------
INSERT INTO consulta (id_empresa, fecha_hora, motivo, detalle, fuera_horario, costo_adicional, creado_por) VALUES
 (@deid, '2026-06-20 11:30:00', 'Consulta normativa',        'Dentro de horario, sin costo', FALSE, FALSE, 'demo-seed'),
 (@deid, '2026-07-01 20:15:00', 'Consulta urgente nocturna', 'Fuera de horario, se cobra',   TRUE,  TRUE,  'demo-seed');
