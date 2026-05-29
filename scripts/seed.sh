#!/usr/bin/env bash
# 데모 영상 시드를 docker compose MySQL 컨테이너에 주입합니다.
# 사용법: ./scripts/seed.sh  (또는 make seed)
# 사전 조건: `make up`으로 스택이 떠 있고, 앱이 한 번 기동되어 videos 테이블이 생성된 상태.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${PROJECT_DIR}"

docker compose exec -T mysql \
  sh -lc 'exec mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' \
  < "${SCRIPT_DIR}/seed_videos.sql"

echo "Seeded demo videos."
