#!/usr/bin/env bash

set -euo pipefail

IMAGE_TAG="$1"
GIT_SHA="$2"

APP_NAME="finance-api"
DB_NAME="finance-postgres"

APP_DIR="/opt/finance-api/app"
COMPOSE_DIR="/opt/finance-api/compose"
COMPOSE_FILE="$COMPOSE_DIR/docker-compose.yml"

HEALTH_URL="http://127.0.0.1:8080/actuator/health"

MAX_ATTEMPTS=30
SLEEP_SECONDS=5

CURRENT_VERSION_FILE="$APP_DIR/.current-version"

compose() {
    (
        cd "$COMPOSE_DIR"
        IMAGE_TAG="$IMAGE_TAG" docker compose "$@"
    )
}

wait_for_postgres() {

    echo
    echo "==> Waiting for PostgreSQL health check"

    for ((attempt=1; attempt<=MAX_ATTEMPTS; attempt++)); do

        STATUS=$(
            docker inspect \
                -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}starting{{end}}' \
                "$DB_NAME" \
                2>/dev/null || echo "not-found"
        )

        echo "PostgreSQL [$attempt/$MAX_ATTEMPTS]: $STATUS"

        if [[ "$STATUS" == "healthy" ]]; then
            echo "==> PostgreSQL is healthy"
            return 0
        fi

        sleep "$SLEEP_SECONDS"
    done

    echo "==> PostgreSQL did not become healthy."

    echo
    echo "========== PostgreSQL =========="

    docker ps -a

    docker logs "$DB_NAME" --tail=100 || true

    return 1
}

health_check() {

    set +e

    RESPONSE=$(
        curl \
            --silent \
            --show-error \
            --write-out "\nHTTP_STATUS:%{http_code}" \
            "$HEALTH_URL"
    )

    CURL_EXIT=$?

    set -e

    echo "Curl exit code: $CURL_EXIT"
    echo "$RESPONSE"

    [[ $CURL_EXIT -eq 0 ]] &&
    echo "$RESPONSE" | grep -q '"status":"UP"' &&
    echo "$RESPONSE" | grep -q 'HTTP_STATUS:200'
}

echo "==> Preparing production configuration"

cd "$APP_DIR"

git fetch origin main
git checkout --detach "$GIT_SHA"

echo "==> Updating production Compose configuration"

install -m 644 \
    "$APP_DIR/docker-compose.prod.yml" \
    "$COMPOSE_FILE"

echo "==> Validating production Compose configuration"

compose config >/dev/null

echo "==> Pulling target image"

compose pull finance-api

CURRENT_VERSION="unknown"

if [[ -f "$CURRENT_VERSION_FILE" ]]; then
    CURRENT_VERSION="$(cat "$CURRENT_VERSION_FILE")"
fi

echo
echo "======================================================"
echo "Deploy"
echo "======================================================"

echo "Current version : $CURRENT_VERSION"
echo "Target version  : $IMAGE_TAG"

echo
echo "==> Starting PostgreSQL"

compose up -d postgres

wait_for_postgres

echo
echo "==> Starting Finance API"

compose up -d --force-recreate finance-api

echo "Waiting for finance-api container..."

until docker inspect "$APP_NAME" >/dev/null 2>&1
do
    sleep 1
done

until [ "$(docker inspect -f '{{.State.Running}}' "$APP_NAME")" = "true" ]
do
    sleep 1
done

echo
echo "==> Waiting for Finance API"

for ((attempt=1; attempt<=MAX_ATTEMPTS; attempt++)); do

    echo
    echo "======================================================"
    echo "Attempt ${attempt}/${MAX_ATTEMPTS}"
    echo "======================================================"

    docker inspect -f \
        'Running={{.State.Running}} RestartCount={{.RestartCount}} StartedAt={{.State.StartedAt}}' \
        "$APP_NAME"

    if health_check; then

        echo "$IMAGE_TAG" > "$CURRENT_VERSION_FILE"

        echo
        echo "======================================================"
        echo "DEPLOY SUCCESS"
        echo "======================================================"

        exit 0
    fi

    if [[ $attempt -lt $MAX_ATTEMPTS ]]; then
        echo
        echo "Retrying in ${SLEEP_SECONDS}s..."
        sleep "$SLEEP_SECONDS"
    fi
done

echo
echo "======================================================"
echo "DEPLOY FAILED"
echo "======================================================"

docker ps

echo
echo "========== Finance API =========="
docker logs "$APP_NAME" --tail=200 || true

echo
echo "========== PostgreSQL =========="
docker logs "$DB_NAME" --tail=100 || true

echo
echo "========== Rolling back =========="

if [[ "$CURRENT_VERSION" == "unknown" ]]; then
    echo "No previous version available."

    exit 1
fi

IMAGE_TAG="$CURRENT_VERSION"

compose pull finance-api

echo
echo "==> Starting PostgreSQL"

compose up -d postgres

wait_for_postgres

echo
echo "==> Restoring previous Finance API"

compose up -d --force-recreate finance-api

echo
echo "==> Waiting rollback health"

for ((attempt=1; attempt<=MAX_ATTEMPTS; attempt++)); do

    if health_check; then

        echo
        echo "Rollback completed successfully."

        exit 1
    fi

    sleep "$SLEEP_SECONDS"

done

echo
echo "Rollback failed."

docker logs "$APP_NAME" --tail=200 || true

exit 1