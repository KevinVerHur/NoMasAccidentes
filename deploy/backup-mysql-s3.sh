#!/bin/bash
# =====================================================
# RNF11 — Respaldo periodico de la base de datos hacia AWS S3.
#
# Hace un mysqldump consistente del contenedor MySQL del despliegue de produccion
# (docker-compose.prod.yml), lo comprime y lo sube a S3. Mantiene ademas una copia
# local reciente como segunda linea de defensa.
#
# Pensado para correr por cron/systemd-timer en la instancia EC2 (Amazon Linux 2023).
# Ver deploy/BACKUP.md para instalacion, permisos IAM, retencion y restauracion.
#
# Uso manual:
#   ./deploy/backup-mysql-s3.sh
#
# Variables (se leen de .env.prod; se pueden sobreescribir por entorno):
#   BACKUP_S3_BUCKET      bucket destino (default: AWS_S3_BUCKET de .env.prod)
#   BACKUP_S3_PREFIX      prefijo/carpeta en el bucket (default: backups/mysql)
#   LOCAL_BACKUP_DIR      carpeta local de copias (default: /home/ec2-user/backups)
#   LOCAL_RETENTION_DAYS  dias que se conservan las copias locales (default: 7)
# =====================================================
set -euo pipefail

# --- Ubicacion: este script vive en <repo>/deploy, el repo es el directorio padre ---
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(dirname "$SCRIPT_DIR")"
ENV_FILE="${ENV_FILE:-$REPO_DIR/.env.prod}"
COMPOSE_FILE="$REPO_DIR/docker-compose.prod.yml"

if [ ! -f "$ENV_FILE" ]; then
  echo "ERROR: no se encontro $ENV_FILE. Copia .env.prod.example y complétalo." >&2
  exit 1
fi

# --- Lee una variable de .env.prod sin ejecutar su contenido (mas seguro que source) ---
get_env() { grep -E "^$1=" "$ENV_FILE" | tail -n1 | cut -d= -f2- || true; }

DB_NAME="$(get_env MYSQL_DATABASE)";        DB_NAME="${DB_NAME:-no_mas_accidentes}"
ROOT_PW="$(get_env MYSQL_ROOT_PASSWORD)"
AWS_REGION="${AWS_REGION:-$(get_env AWS_REGION)}"; AWS_REGION="${AWS_REGION:-us-east-1}"

BACKUP_S3_BUCKET="${BACKUP_S3_BUCKET:-$(get_env AWS_S3_BUCKET)}"
BACKUP_S3_PREFIX="${BACKUP_S3_PREFIX:-backups/mysql}"
LOCAL_BACKUP_DIR="${LOCAL_BACKUP_DIR:-/home/ec2-user/backups}"
LOCAL_RETENTION_DAYS="${LOCAL_RETENTION_DAYS:-7}"

if [ -z "$ROOT_PW" ]; then
  echo "ERROR: MYSQL_ROOT_PASSWORD no esta definido en $ENV_FILE." >&2
  exit 1
fi

# --- Nombre del archivo con fecha en hora de Chile (coincide con TZ del contenedor) ---
STAMP="$(TZ='America/Santiago' date +%Y%m%d-%H%M%S)"
mkdir -p "$LOCAL_BACKUP_DIR"
DUMP_FILE="$LOCAL_BACKUP_DIR/${DB_NAME}_${STAMP}.sql.gz"

echo "[$(date '+%F %T')] Respaldo iniciado -> $DUMP_FILE"

# --- Dump consistente sin bloquear (InnoDB), incluye rutinas/triggers/eventos ---
#   -T  desactiva el TTY (necesario para pipe/cron)
#   MYSQL_PWD evita la advertencia de "password en linea de comandos"
cd "$REPO_DIR"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T \
  -e MYSQL_PWD="$ROOT_PW" mysql \
  mysqldump -u root \
    --single-transaction --routines --triggers --events \
    --no-tablespaces --set-gtid-purged=OFF \
    --databases "$DB_NAME" \
  | gzip -9 > "$DUMP_FILE"

# --- Verifica que el gzip sea valido y no este vacio (falla temprano si el dump se corto) ---
if ! gzip -t "$DUMP_FILE" 2>/dev/null || [ ! -s "$DUMP_FILE" ]; then
  echo "ERROR: el respaldo salio vacio o corrupto, se elimina: $DUMP_FILE" >&2
  rm -f "$DUMP_FILE"
  exit 1
fi
SIZE="$(du -h "$DUMP_FILE" | cut -f1)"
echo "[$(date '+%F %T')] Dump OK ($SIZE)"

# --- Sube a S3 (si hay bucket configurado) ---
if [ -n "$BACKUP_S3_BUCKET" ]; then
  S3_URI="s3://${BACKUP_S3_BUCKET}/${BACKUP_S3_PREFIX}/$(basename "$DUMP_FILE")"
  aws s3 cp "$DUMP_FILE" "$S3_URI" --region "$AWS_REGION" \
    --storage-class STANDARD_IA --only-show-errors
  echo "[$(date '+%F %T')] Subido a $S3_URI"
else
  echo "AVISO: AWS_S3_BUCKET vacio -> respaldo solo en disco local ($LOCAL_BACKUP_DIR)." >&2
fi

# --- Poda copias locales antiguas (la retencion en S3 la maneja el lifecycle, ver BACKUP.md) ---
find "$LOCAL_BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -type f \
  -mtime +"$LOCAL_RETENTION_DAYS" -print -delete || true

echo "[$(date '+%F %T')] Respaldo finalizado."
