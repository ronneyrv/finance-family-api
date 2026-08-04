#!/usr/bin/env bash

set -euo pipefail

APP_DIR="${APP_DIR:-/opt/finance-api}"
COMPOSE_DIR="$APP_DIR/compose"

CONTAINER_NAME="finance-postgres"

RESTORE_SUCCESS=false

compose() {
    (
        cd "$COMPOSE_DIR"
        docker compose "$@"
    )
}

wait_for_api() {

    local retries=60

    local delay=2

    echo
    echo "==> Waiting for Finance API"

    while (( retries > 0 )); do

        if curl \
            --silent \
            --show-error \
            --fail \
            http://127.0.0.1:8080/actuator/health \
            | grep -q '"status":"UP"'; then

            echo "==> Finance API is healthy"

            return 0
        fi

        sleep "$delay"

        ((retries--))

    done

    echo "==> Timeout waiting for Finance API health."

    return 1
}

cleanup() {

    echo

    if [[ "$RESTORE_SUCCESS" == "true" ]]; then
        echo "==> Starting Finance API"

        compose start finance-api

        wait_for_api
    else
        echo "==> Restore failed. Finance API remains stopped."
    fi
}

trap cleanup EXIT

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <backup.sql.gz>"

    exit 1
fi

BACKUP_FILE="$1"

if [[ ! -f "$BACKUP_FILE" ]]; then
    echo "Backup file not found: $BACKUP_FILE"

    exit 1
fi

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

echo
echo "==> Stopping Finance API"

compose stop finance-api

until [[ "$(docker inspect -f '{{.State.Running}}' finance-api)" == "false" ]]; do
    sleep 1
done

echo
echo "==> Terminating active database connections"

docker exec \
    "$CONTAINER_NAME" \
    psql \
    -U "$POSTGRES_USER" \
    -d postgres \
    -v ON_ERROR_STOP=1 \
    -c "
        SELECT pg_terminate_backend(pid)
        FROM pg_stat_activity
        WHERE datname = '$POSTGRES_DB'
          AND pid <> pg_backend_pid();
    "

echo
echo "==> Recreating database"

docker exec \
    "$CONTAINER_NAME" \
    psql \
    -U "$POSTGRES_USER" \
    -d postgres \
    -v ON_ERROR_STOP=1 \
    -c "DROP DATABASE IF EXISTS \"$POSTGRES_DB\";"

docker exec \
    "$CONTAINER_NAME" \
    psql \
    -U "$POSTGRES_USER" \
    -d postgres \
    -v ON_ERROR_STOP=1 \
    -c "CREATE DATABASE \"$POSTGRES_DB\";"

echo
echo "==> Restoring PostgreSQL database"
echo "Backup file: $BACKUP_FILE"

gzip -dc "$BACKUP_FILE" | docker exec -i \
    "$CONTAINER_NAME" \
    psql \
    -U "$POSTGRES_USER" \
    -d "$POSTGRES_DB" \
    -v ON_ERROR_STOP=1

echo
echo "==> Validating restored database"

docker exec \
    "$CONTAINER_NAME" \
    psql \
    -U "$POSTGRES_USER" \
    -d "$POSTGRES_DB" \
    -c '\dt'

RESTORE_SUCCESS=true

echo
echo "======================================================"
echo "RESTORE COMPLETED SUCCESSFULLY"
echo "======================================================"