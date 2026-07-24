#!/usr/bin/env bash

set -euo pipefail

IMAGE_TAG="$1"
GIT_SHA="$2"

APP_NAME="finance-api"
DB_NAME="finance-postgres"

HEALTH_URL="http://127.0.0.1:8080/actuator/health"

MAX_ATTEMPTS=30
SLEEP_SECONDS=5

CURRENT_VERSION_FILE=".current-version"
PREVIOUS_COMPOSE_FILE="docker-compose.previous.yml"

echo "==> Preparing production configuration"

git fetch origin main
git checkout --detach "$GIT_SHA"

echo "==> Validating production Compose configuration"
docker compose config >/dev/null

echo "==> Pulling target image"
IMAGE_TAG="$IMAGE_TAG" docker compose pull

echo "==> Backing up current runtime Compose configuration"
cp docker-compose.yml "$PREVIOUS_COMPOSE_FILE"

echo "==> Synchronizing production Compose configuration"
cp docker-compose.prod.yml docker-compose.yml

CURRENT_VERSION="unknown"

if [[ -f "$CURRENT_VERSION_FILE" ]]; then
    CURRENT_VERSION="$(cat "$CURRENT_VERSION_FILE")"
fi

echo "==> Starting versioned production deployment"
echo "==> Current version: $CURRENT_VERSION"
echo "==> Target version: $IMAGE_TAG"

echo "==> Deploying target version"

IMAGE_TAG="$IMAGE_TAG" docker compose up -d --force-recreate

echo "==> Waiting for application health check"

for ((attempt=1; attempt<=MAX_ATTEMPTS; attempt++)); do

    RESPONSE=$(curl \
        --silent \
        --show-error \
        --write-out "\nHTTP_STATUS:%{http_code}" \
        "$HEALTH_URL" || true)

    echo
    echo "========== Health Check Attempt ${attempt}/${MAX_ATTEMPTS} =========="
    echo "$RESPONSE"

    if echo "$RESPONSE" | grep -q '"status":"UP"' &&
       echo "$RESPONSE" | grep -q 'HTTP_STATUS:200'; then

        echo "$IMAGE_TAG" > "$CURRENT_VERSION_FILE"

        echo "==> Deployment completed successfully: $IMAGE_TAG"

        exit 0
    fi

    if [[ $attempt -lt $MAX_ATTEMPTS ]]; then
        echo "==> Health check failed. Retrying in ${SLEEP_SECONDS}s..."
        sleep "$SLEEP_SECONDS"
    fi
done

echo
echo "======================================================"
echo "DEPLOY FAILED"
echo "======================================================"

echo
echo "========== Docker PS =========="
docker ps || true

echo
echo "========== Finance API Logs =========="
docker logs "$APP_NAME" --tail=200 || true

echo
echo "========== PostgreSQL Logs =========="
docker logs "$DB_NAME" --tail=100 || true

echo
echo "========== Finance API Inspect =========="
docker inspect "$APP_NAME" || true

echo
echo "========== Rolling back to previous version =========="

cp "$PREVIOUS_COMPOSE_FILE" docker-compose.yml

echo "==> Validating restored Compose configuration"
docker compose config >/dev/null

echo "==> Recreating previous production version"

IMAGE_TAG="$CURRENT_VERSION" docker compose up -d --force-recreate

echo "==> Waiting for rollback health check"

for ((attempt=1; attempt<=MAX_ATTEMPTS; attempt++)); do

    RESPONSE=$(curl \
        --silent \
        --show-error \
        --write-out "\nHTTP_STATUS:%{http_code}" \
        "$HEALTH_URL" || true)

    echo
    echo "========== Rollback Health Check ${attempt}/${MAX_ATTEMPTS} =========="
    echo "$RESPONSE"

    if echo "$RESPONSE" | grep -q '"status":"UP"' &&
       echo "$RESPONSE" | grep -q 'HTTP_STATUS:200'; then

        echo "==> Rollback completed successfully: $CURRENT_VERSION"

        docker ps

        exit 1
    fi

    if [[ $attempt -lt $MAX_ATTEMPTS ]]; then
        echo "==> Rollback health check failed. Retrying in ${SLEEP_SECONDS}s..."
        sleep "$SLEEP_SECONDS"
    fi
done

echo
echo "========== Rollback Finance API Logs =========="
docker logs "$APP_NAME" --tail=200 || true

echo
echo "========== Rollback PostgreSQL Logs =========="
docker logs "$DB_NAME" --tail=100 || true

echo "Rollback failed."

exit 1