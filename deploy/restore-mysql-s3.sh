#!/bin/bash
# =====================================================
# RNF11 — Restauracion de la base de datos desde un respaldo de S3 (o local).
#
# Descarga (si hace falta) un .sql.gz generado por backup-mysql-s3.sh y lo carga
# en el contenedor MySQL del despliegue de produccion.
#
# ADVERTENCIA: sobrescribe los datos actuales de la base indicada. Confirmar antes.
#
# Uso:
#   # Restaurar desde un objeto de S3:
#   ./deploy/restore-mysql-s3.sh s3://mi-bucket/backups/mysql/no_mas_accidentes_20260705-030000.sql.gz
#   # Restaurar desde un archivo local:
#   ./deploy/restore-mysql-s3.sh /home/ec2-user/backups/no_mas_accidentes_20260705-030000.sql.gz
#   # Ver los respaldos disponibles en S3 (sin argumento):
#   ./deploy/restore-mysql-s3.sh
# =====================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(dirname "$SCRIPT_DIR")"
ENV_FILE="${ENV_FILE:-$REPO_DIR/.env.prod}"
COMPOSE_FILE="$REPO_DIR/docker-compose.prod.yml"

get_env() { grep -E "^$1=" "$ENV_FILE" | tail -n1 | cut -d= -f2- || true; }
DB_NAME="$(get_env MYSQL_DATABASE)";        DB_NAME="${DB_NAME:-no_mas_accidentes}"
ROOT_PW="$(get_env MYSQL_ROOT_PASSWORD)"
AWS_REGION="${AWS_REGION:-$(get_env AWS_REGION)}"; AWS_REGION="${AWS_REGION:-us-east-1}"
BACKUP_S3_BUCKET="${BACKUP_S3_BUCKET:-$(get_env AWS_S3_BUCKET)}"
BACKUP_S3_PREFIX="${BACKUP_S3_PREFIX:-backups/mysql}"

# --- Sin argumento: lista lo que hay en S3 y sale ---
SRC="${1:-}"
if [ -z "$SRC" ]; then
  echo "Respaldos disponibles en s3://${BACKUP_S3_BUCKET}/${BACKUP_S3_PREFIX}/ :"
  aws s3 ls "s3://${BACKUP_S3_BUCKET}/${BACKUP_S3_PREFIX}/" --region "$AWS_REGION" || true
  echo ""
  echo "Vuelve a ejecutar pasando la ruta s3:// o local del respaldo a restaurar."
  exit 0
fi

# --- Resuelve el archivo local (descarga si es s3://) ---
if [[ "$SRC" == s3://* ]]; then
  TMP_FILE="/tmp/$(basename "$SRC")"
  echo "Descargando $SRC ..."
  aws s3 cp "$SRC" "$TMP_FILE" --region "$AWS_REGION" --only-show-errors
  LOCAL_FILE="$TMP_FILE"
else
  LOCAL_FILE="$SRC"
fi

if [ ! -s "$LOCAL_FILE" ]; then
  echo "ERROR: no existe o esta vacio: $LOCAL_FILE" >&2
  exit 1
fi
if ! gzip -t "$LOCAL_FILE" 2>/dev/null; then
  echo "ERROR: el archivo no es un .gz valido: $LOCAL_FILE" >&2
  exit 1
fi

# --- Confirmacion explicita (operacion destructiva) ---
echo "Vas a RESTAURAR la base '$DB_NAME' desde:"
echo "   $LOCAL_FILE"
echo "Esto SOBRESCRIBE los datos actuales. Escribe 'restaurar' para continuar:"
read -r CONFIRM
if [ "$CONFIRM" != "restaurar" ]; then
  echo "Cancelado."
  exit 1
fi

# --- Carga el dump en el contenedor MySQL ---
cd "$REPO_DIR"
gunzip -c "$LOCAL_FILE" | docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T \
  -e MYSQL_PWD="$ROOT_PW" mysql \
  mysql -u root

echo "Restauracion completada sobre '$DB_NAME'."
echo "Reinicia el backend si quieres refrescar conexiones:"
echo "   docker compose --env-file $ENV_FILE -f $COMPOSE_FILE restart backend"
