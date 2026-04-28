#!/usr/bin/env bash
set -euo pipefail

COMPOSE_CMD="docker compose"
if ! docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD="docker-compose"
fi

cleanup() {
  ${COMPOSE_CMD} down -v >/dev/null 2>&1 || true
}
trap cleanup EXIT

${COMPOSE_CMD} up -d --build

for i in {1..30}; do
  if curl -fsS http://localhost:8080/ >/dev/null 2>&1; then
    echo "OK: app responds on http://localhost:8080/"
    exit 0
  fi
  sleep 2
  echo "waiting for app... ($i/30)"
done

echo "ERROR: app did not become reachable on http://localhost:8080/" >&2
exit 1
