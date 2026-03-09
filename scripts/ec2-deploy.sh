#!/usr/bin/env bash
set -euo pipefail

DEPLOY_BACKEND=false
DEPLOY_FRONTEND=false
RUN_TESTS=false

BACKEND_REPO_PATH="${BACKEND_REPO_PATH:-/opt/mts-purchase-service}"
BACKEND_BRANCH="${BACKEND_BRANCH:-main}"
BACKEND_SERVICE_NAME="${BACKEND_SERVICE_NAME:-mts-purchase-service}"
BACKEND_HEALTH_URL="${BACKEND_HEALTH_URL:-http://127.0.0.1:8080/actuator/health}"
BACKEND_FALLBACK_HEALTH_URL="${BACKEND_FALLBACK_HEALTH_URL:-http://127.0.0.1:8080/swagger-ui/index.html}"

FRONTEND_REPO_PATH="${FRONTEND_REPO_PATH:-/opt/mts-finance-dashboard}"
FRONTEND_BRANCH="${FRONTEND_BRANCH:-main}"
FRONTEND_DEPLOY_PATH="${FRONTEND_DEPLOY_PATH:-/var/www/mts-finance-dashboard}"
FRONTEND_WEB_SERVICE="${FRONTEND_WEB_SERVICE:-nginx}"

usage() {
  cat <<'EOF'
Usage: ec2-deploy.sh [--backend] [--frontend] [--run-tests]

Options:
  --backend     Build and deploy Spring Boot backend (systemd restart + health check)
  --frontend    Pull and publish frontend files to Nginx web root
  --run-tests   Run backend tests before packaging (default: skip tests)
  -h, --help    Show this help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --backend)
      DEPLOY_BACKEND=true
      shift
      ;;
    --frontend)
      DEPLOY_FRONTEND=true
      shift
      ;;
    --run-tests)
      RUN_TESTS=true
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

if [[ "$DEPLOY_BACKEND" == "false" && "$DEPLOY_FRONTEND" == "false" ]]; then
  echo "No deployment target selected. Use --backend and/or --frontend." >&2
  exit 1
fi

log() {
  printf '[deploy] %s\n' "$*"
}

deploy_backend() {
  log "Deploying backend from ${BACKEND_REPO_PATH} (${BACKEND_BRANCH})"
  cd "${BACKEND_REPO_PATH}"

  git fetch origin "${BACKEND_BRANCH}"
  git checkout "${BACKEND_BRANCH}"
  git pull --ff-only origin "${BACKEND_BRANCH}"

  chmod +x mvnw
  if [[ "$RUN_TESTS" == "true" ]]; then
    ./mvnw clean package
  else
    ./mvnw clean package -DskipTests
  fi

  JAR_PATH="$(ls target/*.jar | grep -v 'original-' | head -n 1 || true)"
  if [[ -z "${JAR_PATH}" ]]; then
    echo "Could not find built jar in target/." >&2
    exit 1
  fi

  cp "${JAR_PATH}" "${BACKEND_REPO_PATH}/app.jar"

  sudo systemctl restart "${BACKEND_SERVICE_NAME}"
  sudo systemctl is-active --quiet "${BACKEND_SERVICE_NAME}"

  if ! curl -fsS "${BACKEND_HEALTH_URL}" >/dev/null \
    && ! curl -fsS "${BACKEND_FALLBACK_HEALTH_URL}" >/dev/null; then
    echo "Backend health check failed at ${BACKEND_HEALTH_URL} and ${BACKEND_FALLBACK_HEALTH_URL}" >&2
    exit 1
  fi

  log "Backend deployment finished successfully."
}

deploy_frontend() {
  log "Deploying frontend from ${FRONTEND_REPO_PATH} (${FRONTEND_BRANCH})"
  cd "${FRONTEND_REPO_PATH}"

  git fetch origin "${FRONTEND_BRANCH}"
  git checkout "${FRONTEND_BRANCH}"
  git pull --ff-only origin "${FRONTEND_BRANCH}"

  sudo rsync -av --delete "${FRONTEND_REPO_PATH}/" "${FRONTEND_DEPLOY_PATH}/"
  sudo systemctl restart "${FRONTEND_WEB_SERVICE}"
  sudo systemctl is-active --quiet "${FRONTEND_WEB_SERVICE}"

  log "Frontend deployment finished successfully."
}

if [[ "$DEPLOY_BACKEND" == "true" ]]; then
  deploy_backend
fi

if [[ "$DEPLOY_FRONTEND" == "true" ]]; then
  deploy_frontend
fi

log "Deployment completed."
