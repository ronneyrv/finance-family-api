#!/usr/bin/env bash

set -euo pipefail

APP_DIR="${APP_DIR:-/opt/finance-api}"
COMPOSE_DIR="$APP_DIR/compose"
BACKUP_DIR="$APP_DIR/backups"

CONTAINER_NAME="finance-postgres"

if [[ ! -f "$COMPOSE_DIR/.env" ]]; then
    echo "Compose environment file not found."

    exit 1
fi

if ! docker inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
    echo "Container '$CONTAINER_NAME' not found."

    exit 1
fi

set -a
source "$COMPOSE_DIR/.env"
set +a

TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")

BACKUP_FILE="$BACKUP_DIR/finance-$TIMESTAMP.sql.gz"

mkdir -p "$BACKUP_DIR"

echo "==> Starting PostgreSQL backup"
echo "Database : $POSTGRES_DB"
echo "Container: $CONTAINER_NAME"

docker exec \
    "$CONTAINER_NAME" \
    pg_dump \
    -U "$POSTGRES_USER" \
    -d "$POSTGRES_DB" \
| gzip > "$BACKUP_FILE"

if [[ ! -s "$BACKUP_FILE" ]]; then
    echo "Backup file was not created."

    exit 1
fi

echo "==> Validating backup integrity"

if ! gzip -t "$BACKUP_FILE"; then
    echo "Backup validation failed."

    rm -f "$BACKUP_FILE"

    exit 1
fi

echo "==> Applying backup retention policy"

find \
    "$BACKUP_DIR" \
    -maxdepth 1 \
    -name "finance-*.sql.gz" \
    -type f \
    | sort \
    | head -n -7 \
    | xargs -r rm -f

echo "==> Backup completed successfully"
echo "==> Backup file: $BACKUP_FILE"