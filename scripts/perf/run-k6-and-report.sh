#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
OUT_DIR="docs/perf/results"
DATE_TAG="$(date -u +%F)"
RAW_JSON="${OUT_DIR}/k6-summary-${DATE_TAG}.json"
REPORT_MD="${OUT_DIR}/non-functional-report-${DATE_TAG}.md"

mkdir -p "${OUT_DIR}"

if ! command -v k6 >/dev/null 2>&1; then
  echo "k6 nie jest zainstalowany" >&2
  exit 1
fi

k6 run \
  -e BASE_URL="${BASE_URL}" \
  --summary-export "${RAW_JSON}" \
  load-tests/chat-300-users.js

python - <<'PY' "${RAW_JSON}" "${REPORT_MD}" "${BASE_URL}" "${DATE_TAG}"
import json,sys
raw,report,base,date_tag = sys.argv[1:5]
with open(raw,'r',encoding='utf-8') as f:
    data=json.load(f)
metrics=data.get('metrics',{})

def v(path, default='n/a'):
    cur=metrics
    for p in path:
        cur=cur.get(p) if isinstance(cur,dict) else None
        if cur is None:
            return default
    return cur

p95=v(['http_req_duration','values','p(95)'])
failed=v(['http_req_failed','values','rate'])
checks=v(['checks','values','rate'])

with open(report,'w',encoding='utf-8') as f:
    f.write(f"# Non-functional report ({date_tag})\n\n")
    f.write(f"Base URL: `{base}`\n\n")
    f.write("## K6 (300 users)\n")
    f.write(f"- http_req_duration p95: `{p95}` ms\n")
    f.write(f"- http_req_failed rate: `{failed}`\n")
    f.write(f"- checks success rate: `{checks}`\n\n")
    f.write("## Kryteria\n")
    f.write("- p95 <= 3000 ms: " + ("✅" if isinstance(p95,(int,float)) and p95 <= 3000 else "❌") + "\n")
    f.write("- failed rate < 0.05: " + ("✅" if isinstance(failed,(int,float)) and failed < 0.05 else "❌") + "\n\n")
    f.write("## Uzupełnij ręcznie\n")
    f.write("- Message delivery <= 3s (websocket E2E): ...\n")
    f.write("- Presence update < 2s: ...\n")
    f.write("- History >= 10k messages: ...\n")
PY

echo "Wygenerowano: ${RAW_JSON}"
echo "Wygenerowano: ${REPORT_MD}"
