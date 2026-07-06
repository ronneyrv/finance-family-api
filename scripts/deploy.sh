#!/usr/bin/env bash

set -euo pipefail

APP_DIR="/opt/finance-api/app"
COMPOSE_DIR="/opt/finance-api/compose"
SOURCE_COMPOSE="$APP_DIR/docker-compose.prod.yml"
RUNTIME_COMPOSE="$COMPOSE_DIR/docker-compose.yml"
BACKUP_COMPOSE="$COMPOSE_DIR/docker-compose.yml.bak"

HEALTH_URL="http://localhost:8080/actuator/health"
MAX_ATTEMPTS=18
SLEEP_SECONDS=5

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <image-tag> <git-sha>"
  echo "Example: $0 sha-e2c0592 e2c0592"
  exit 1
fi

NEW_TAG="$1"
GIT_SHA="$2"

echo "==> Preparing production configuration"

cd "$APP_DIR"

git fetch origin main

if ! git cat-file -e "${GIT_SHA}^{commit}"; then
  echo "==> Git commit not found: $GIT_SHA"
  exit 1
fi

git checkout --detach "$GIT_SHA"

echo "==> Validating production Compose configuration"

IMAGE_TAG="$NEW_TAG" docker compose \
  -f "$SOURCE_COMPOSE" \
  --env-file "$COMPOSE_DIR/.env" \
  config --quiet

  echo "==> Pulling target image"

  IMAGE_TAG="$NEW_TAG" docker compose \
    -f "$SOURCE_COMPOSE" \
    --env-file "$COMPOSE_DIR/.env" \
    pull

echo "==> Backing up current runtime Compose configuration"

cp "$RUNTIME_COMPOSE" "$BACKUP_COMPOSE"

echo "==> Synchronizing production Compose configuration"

cp "$SOURCE_COMPOSE" "$RUNTIME_COMPOSE"

cd "$COMPOSE_DIR"

CURRENT_IMAGE=$(docker inspect finance-api \
  --format '{{.Config.Image}}' 2>/dev/null || true)

CURRENT_TAG="${CURRENT_IMAGE##*:}"

echo "==> Starting versioned production deployment"
echo "==> Current version: ${CURRENT_TAG:-unknown}"
echo "==> Target version: $NEW_TAG"

echo "==> Deploying target version"
IMAGE_TAG="$NEW_TAG" docker compose up -d --force-recreate

echo "==> Waiting for application health check"

for attempt in $(seq 1 "$MAX_ATTEMPTS"); do
  if curl --fail --silent "$HEALTH_URL" | grep -q '"status":"UP"'; then
    echo "==> Application is healthy"
    IMAGE_TAG="$NEW_TAG" docker compose ps
    echo "==> Deployment completed successfully: $NEW_TAG"
    exit 0
  fi

  echo "==> Health check attempt $attempt/$MAX_ATTEMPTS failed. Retrying in ${SLEEP_SECONDS}s..."
  sleep "$SLEEP_SECONDS"
done

echo "==> Deployment failed for version: $NEW_TAG"

if [ -z "$CURRENT_TAG" ]; then
  echo "==> Rollback unavailable: previous version could not be determined"
  IMAGE_TAG="$NEW_TAG" docker compose logs --tail=50 finance-api
  exit 1
fi

echo "==> Rolling back to previous version: $CURRENT_TAG"

echo "==> Restoring previous Compose configuration"

cp "$BACKUP_COMPOSE" "$RUNTIME_COMPOSE"

echo "==> Validating restored Compose configuration"

IMAGE_TAG="$CURRENT_TAG" docker compose \
  -f "$RUNTIME_COMPOSE" \
  --env-file "$COMPOSE_DIR/.env" \
  config --quiet

echo "==> Recreating previous production version"

IMAGE_TAG="$CURRENT_TAG" docker compose up -d --force-recreate

echo "==> Waiting for rollback health check"

for attempt in $(seq 1 "$MAX_ATTEMPTS"); do
  if curl --fail --silent "$HEALTH_URL" | grep -q '"status":"UP"'; then
    echo "==> Rollback completed successfully: $CURRENT_TAG"
    IMAGE_TAG="$CURRENT_TAG" docker compose ps
    exit 1
  fi

  echo "==> Rollback health check attempt $attempt/$MAX_ATTEMPTS failed. Retrying in ${SLEEP_SECONDS}s..."
  sleep "$SLEEP_SECONDS"
done

echo "==> Critical failure: rollback version is also unhealthy"
IMAGE_TAG="$CURRENT_TAG" docker compose logs --tail=50 finance-api

exit 2