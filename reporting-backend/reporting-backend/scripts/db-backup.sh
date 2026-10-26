#!/usr/bin/env bash
# ============================================================================
# Sauvegarde automatique de la base de production reporting_db.
#
# Utilisation manuelle :
#   DB_PASSWORD=xxx ./db-backup.sh
#
# Utilisation automatique (cron, tous les jours a 02:00) :
#   0 2 * * * DB_PASSWORD=xxx /chemin/vers/db-backup.sh >> /var/log/reporting-backend/backup.log 2>&1
#
# Conserve 14 jours de sauvegardes glissantes et supprime les plus anciennes.
# ============================================================================
set -euo pipefail

DB_HOST="${DB_HOST:-localhost}"
DB_NAME="${DB_NAME:-reporting_db}"
DB_USER="${DB_BACKUP_USER:-reporting_app}"
BACKUP_DIR="${BACKUP_DIR:-/var/backups/reporting-db}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

mkdir -p "$BACKUP_DIR"

mysqldump \
  --host="$DB_HOST" \
  --user="$DB_USER" \
  --password="$DB_PASSWORD" \
  --single-transaction \
  --routines \
  --triggers \
  "$DB_NAME" | gzip > "$BACKUP_DIR/${DB_NAME}_${TIMESTAMP}.sql.gz"

echo "Sauvegarde creee : $BACKUP_DIR/${DB_NAME}_${TIMESTAMP}.sql.gz"

# Purge des sauvegardes de plus de RETENTION_DAYS jours
find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -mtime +"$RETENTION_DAYS" -delete

echo "Sauvegardes de plus de ${RETENTION_DAYS} jours purgees."
