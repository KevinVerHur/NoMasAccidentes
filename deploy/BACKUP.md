# Respaldo y restauración de la base de datos (RNF11)

Respaldos periódicos de la BD MySQL del despliegue de producción hacia **AWS S3**,
con copia local reciente como segunda línea de defensa. Cubre el requisito
**RNF11 — "El sistema debe evitar pérdida de información mediante respaldos periódicos"**.

| Script | Función |
|--------|---------|
| `deploy/backup-mysql-s3.sh`  | `mysqldump` consistente → `gzip` → subida a S3 + poda local |
| `deploy/restore-mysql-s3.sh` | Descarga un respaldo de S3 (o local) y lo restaura en MySQL |

Todo se ejecuta **en la instancia EC2**, donde vive `.env.prod` y corre el stack
`docker-compose.prod.yml`. Las credenciales de la BD se leen de `.env.prod`
(no se hardcodean).

---

## 1. Prerrequisitos (una sola vez en la instancia)

```bash
# AWS CLI v2 (Amazon Linux 2023 suele traerla; si no):
sudo dnf install -y awscli

# cron (Amazon Linux 2023 no lo trae por defecto):
sudo dnf install -y cronie
sudo systemctl enable --now crond

# Permisos de ejecución de los scripts:
chmod +x deploy/backup-mysql-s3.sh deploy/restore-mysql-s3.sh
```

### Credenciales AWS
Lo recomendado es asociar un **rol IAM a la instancia EC2** (sin llaves en disco).
La política mínima necesaria sobre el bucket de respaldos:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:PutObject", "s3:GetObject", "s3:ListBucket"],
      "Resource": [
        "arn:aws:s3:::TU_BUCKET",
        "arn:aws:s3:::TU_BUCKET/backups/mysql/*"
      ]
    }
  ]
}
```

> Por defecto los respaldos usan el mismo `AWS_S3_BUCKET` de `.env.prod` (el de los
> informes) bajo el prefijo `backups/mysql/`. Para separarlos, exporta
> `BACKUP_S3_BUCKET` antes de correr el script.

---

## 2. Probar el respaldo manualmente

```bash
cd ~/NoMasAccidentes        # donde clonaste el repo en la instancia
./deploy/backup-mysql-s3.sh
```

Debe terminar con `Subido a s3://.../backups/mysql/no_mas_accidentes_YYYYMMDD-HHMMSS.sql.gz`.
Verifica en S3:

```bash
aws s3 ls s3://TU_BUCKET/backups/mysql/
```

---

## 3. Programar el respaldo (diario 03:00 hora de Chile)

`crontab -e` del usuario `ec2-user` y agrega:

```cron
# Respaldo diario de la BD a S3 (RNF11). TZ del contenedor MySQL = America/Santiago.
0 3 * * * /home/ec2-user/NoMasAccidentes/deploy/backup-mysql-s3.sh >> /home/ec2-user/backups/backup.log 2>&1
```

Ajusta la ruta al lugar real donde clonaste el repo. Revisa el log en
`/home/ec2-user/backups/backup.log`.

---

## 4. Retención automática en S3 (lifecycle)

Para no acumular respaldos indefinidamente, deja que S3 los expire solo.
Guarda esto como `lifecycle.json` y aplícalo al bucket:

```json
{
  "Rules": [
    {
      "ID": "expira-backups-mysql-30d",
      "Filter": { "Prefix": "backups/mysql/" },
      "Status": "Enabled",
      "Expiration": { "Days": 30 }
    }
  ]
}
```

```bash
aws s3api put-bucket-lifecycle-configuration \
  --bucket TU_BUCKET \
  --lifecycle-configuration file://lifecycle.json
```

Las copias **locales** se podan por antigüedad dentro del propio script
(`LOCAL_RETENTION_DAYS`, 7 días por defecto).

---

## 5. Restaurar un respaldo

```bash
# Ver qué respaldos hay disponibles:
./deploy/restore-mysql-s3.sh

# Restaurar uno (pide confirmación escribiendo 'restaurar'):
./deploy/restore-mysql-s3.sh s3://TU_BUCKET/backups/mysql/no_mas_accidentes_20260705-030000.sql.gz
```

Tras restaurar conviene reiniciar el backend:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml restart backend
```

---

## 6. Variables configurables

| Variable | Default | Descripción |
|----------|---------|-------------|
| `BACKUP_S3_BUCKET`     | `AWS_S3_BUCKET` de `.env.prod` | Bucket destino |
| `BACKUP_S3_PREFIX`     | `backups/mysql` | Carpeta dentro del bucket |
| `LOCAL_BACKUP_DIR`     | `/home/ec2-user/backups` | Carpeta local de copias |
| `LOCAL_RETENTION_DAYS` | `7` | Días que se conservan las copias locales |
| `ENV_FILE`             | `<repo>/.env.prod` | Ruta al archivo de entorno |

---

### Para la defensa (RNF11)
- **Respaldo periódico:** cron diario → `mysqldump --single-transaction` (consistente, sin
  bloquear) → S3 con `STANDARD_IA` + expiración a 30 días por lifecycle.
- **Recuperación probada:** `restore-mysql-s3.sh` restaura desde cualquier punto disponible.
- **Doble copia:** S3 (durabilidad 99,999999999%) + copia local reciente.
- **Sin secretos en el código:** credenciales de BD desde `.env.prod`, credenciales AWS
  vía rol IAM de la instancia.
