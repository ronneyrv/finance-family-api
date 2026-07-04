#!/usr/bin/env bash

set -euo pipefail

COMPOSE_DIR="/opt/finance-api/compose"
HEALTH_URL="http://localhost:8080/actuator/health"
MAX_ATTEMPTS=18
SLEEP_SECONDS=5

echo "==> Starting production deployment"

cd "$COMPOSE_DIR"

echo "==> Pulling latest image from GHCR"
docker compose pull

echo "==> Updating application container"
docker compose up -d

echo "==> Waiting for application health check"

for attempt in $(seq 1 "$MAX_ATTEMPTS"); do
  if curl --fail --silent "$HEALTH_URL" | grep -q '"status":"UP"'; then
    echo "==> Application is healthy"
    docker compose ps
    echo "==> Deployment completed successfully"
    exit 0
  fi

  echo "==> Health check attempt $attempt/$MAX_ATTEMPTS failed. Retrying in ${SLEEP_SECONDS}s..."
  sleep "$SLEEP_SECONDS"
done

echo "==> Deployment failed: application did not become healthy"
echo "==> Last container logs:"
docker compose logs --tail=50 finance-api

exit 1