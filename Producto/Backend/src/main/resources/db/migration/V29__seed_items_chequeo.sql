-- La lista de chequeo del seed (V18) se creó sin ítems, por lo que las visitas
-- de la empresa de prueba no tenían nada que marcar (RF19). Se agregan los ítems
-- por defecto con su norma legal, solo si la lista aún no tiene ítems.
SET @lid := (SELECT id_lista_chequeo FROM lista_chequeo
             WHERE nombre = 'Lista por defecto' AND creado_por = 'flyway-seed'
             ORDER BY id_lista_chequeo LIMIT 1);

INSERT INTO item_chequeo (id_lista_chequeo, descripcion, categoria, norma_legal, obligatorio, orden, activo, creado_por)
SELECT @lid, t.descripcion, t.categoria, t.norma_legal, TRUE, t.orden, TRUE, 'flyway-seed'
FROM (
    SELECT 'Uso de elementos de protección personal (EPP)'    AS descripcion, 'EPP'                    AS categoria, 'Art. 53 D.S. 594/1999 MINSAL'      AS norma_legal, 1 AS orden
    UNION ALL SELECT 'Señalización de seguridad visible y vigente',    'Señalización',          'Art. 37 D.S. 594/1999 MINSAL',     2
    UNION ALL SELECT 'Extintores operativos y con carga vigente',      'Emergencias',           'Art. 45 y 51 D.S. 594/1999 MINSAL', 3
    UNION ALL SELECT 'Vías de evacuación despejadas',                  'Emergencias',           'Art. 7 D.S. 594/1999 MINSAL',      4
    UNION ALL SELECT 'Orden y limpieza en áreas de trabajo',           'Condiciones generales', 'Art. 11 D.S. 594/1999 MINSAL',     5
    UNION ALL SELECT 'Instalaciones eléctricas sin riesgos visibles',  'Condiciones generales', 'Art. 39 D.S. 594/1999 MINSAL',     6
) AS t
WHERE @lid IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM item_chequeo i WHERE i.id_lista_chequeo = @lid);
