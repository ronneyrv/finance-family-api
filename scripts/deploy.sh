#!/usr/bin/env bash

set -euo pipefail

COMPOSE_DIR="/opt/finance-api/compose"

echo "==> Starting production deployment"

cd "$COMPOSE_DIR"

echo "==> Pulling latest image from GHCR"
docker compose pull

echo "==> Updating application container"
docker compose up -d

echo "==> Container status"
docker compose ps

echo "==> Deployment completed successfully"