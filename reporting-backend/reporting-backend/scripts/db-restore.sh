#!/usr/bin/env bash
# ============================================================================
# Restauration d'une sauvegarde vers une base de test (JAMAIS directement vers
# la production sans validation prealable).
#
# Utilisation :
#   DB_PASSWORD=xxx ./db-restore.sh /var/backups/reporting-db/reporting_db_20260101_020000.sql.gz
#
# Procedure de test de restauration recommandee (a faire au moins 1 fois avant
# la mise en production, puis periodiquement, ex: 1 fois par mois) :
#   1) Choisir une sauvegarde recente.
#   2) La restaurer vers une base temporaire (reporting_db_test), PAS vers reporting_db.
#   3) Demarrer le backend avec spring.datasource.url pointant vers reporting_db_test
#      et verifier que l'application demarre et que les donnees sont coherentes
#      (nombre d'utilisateurs, de rapports, etc.).
#   4) Supprimer la base de test.
#   5) Noter la date du test dans un journal (ex: CHANGELOG des sauvegardes).
# ============================================================================
set -euo pipefail

BACKUP_FILE="${1:?Usage: db-restore.sh <fichier.sql.gz>}"
DB_HOST="${DB_HOST:-localhost}"
DB_NAME="${DB_NAME:-reporting_db_test}"
DB_USER="${DB_BACKUP_USER:-reporting_app}"

echo "Restauration de $BACKUP_FILE vers la base '$DB_NAME' (base de test, jamais la prod directement)."

mysql --host="$DB_HOST" --user="$DB_USER" --password="$DB_PASSWORD" \
  -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4;"

gunzip -c "$BACKUP_FILE" | mysql --host="$DB_HOST" --user="$DB_USER" --password="$DB_PASSWORD" "$DB_NAME"

echo "Restauration terminee dans '$DB_NAME'. Verifier les donnees avant toute bascule en production."
