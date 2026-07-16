#!/usr/bin/env bash
# Local-deployment alternative to the GitHub Actions workflow
# (.github/workflows/deploy.yml), for anyone who can't use Actions.
#
# Usage: ./deploy-local.sh
# Requires: Maven, Docker, and a git repository (for the commit SHA tag).

set -euo pipefail

echo "==> Running tests"
mvn -B test

echo "==> Building jar"
mvn -B package -DskipTests

SHA="$(git rev-parse HEAD)"
IMAGE_NAME="gameflix"

echo "==> Building Docker image ${IMAGE_NAME}:${SHA}"
docker build -t "${IMAGE_NAME}:${SHA}" -t "${IMAGE_NAME}:latest" .

echo "==> Deploy (stub) - no real target environment configured yet."
echo "Would deploy ${IMAGE_NAME}:${SHA} here, e.g.:"
echo "  docker compose up -d --build"
