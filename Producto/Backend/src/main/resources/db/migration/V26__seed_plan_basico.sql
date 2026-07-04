INSERT INTO mensualidad (
    nombre_plan,
    monto_base,
    visitas_incluidas,
    asesorias_incluidas,
    capacitaciones_incluidas,
    costo_visita_extra,
    costo_asesoria_extra,
    costo_capacitacion_extra,
    costo_llamado_fuera_horario,
    activo,
    creado_por
)
SELECT
    'PLAN_BASICO',
    120000.00,
    2,
    10,
    1,
    45000.00,
    25000.00,
    80000.00,
    15000.00,
    TRUE,
    'flyway-seed'
WHERE NOT EXISTS (
    SELECT 1 FROM mensualidad WHERE nombre_plan = 'PLAN_BASICO'
);