#!/usr/bin/env bash

set -euo pipefail

COMPOSE_FILE="/opt/finance-api/compose/docker-compose.yml"
ENV_FILE="/opt/finance-api/compose/.env"

echo "Pulling latest production image..."
docker compose \
  -f "$COMPOSE_FILE" \
  --env-file "$ENV_FILE" \
  pull

echo "Starting production container..."
docker compose \
  -f "$COMPOSE_FILE" \
  --env-file "$ENV_FILE" \
  up -d

echo "Checking container status..."
docker compose \
  -f "$COMPOSE_FILE" \
  --env-file "$ENV_FILE" \
  ps

echo "Production deployment completed."
