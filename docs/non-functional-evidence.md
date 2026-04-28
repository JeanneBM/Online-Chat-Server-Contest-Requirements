# Non-functional evidence

## 1) Load test for 300 concurrent users

Script: `load-tests/chat-300-users.js`.

Automated collection command:

```bash
./scripts/perf/run-k6-and-report.sh
```

Generated artifacts (committed path):
- `docs/perf/results/k6-summary-YYYY-MM-DD.json`
- `docs/perf/results/non-functional-report-YYYY-MM-DD.md`

Validated threshold in script:
- `http_req_duration p(95) < 3000ms`
- `http_req_failed rate < 0.05`

## 2) Message delivery <= 3 seconds

Manual evidence section is generated in the report template and must be filled after a real two-user websocket run.

Recommended proof:
- browser DevTools timestamps (sender action vs receiver event),
- at least 30 sample events,
- summary with p95 and max values.

## 3) Presence update below 2 seconds

Presence is calculated from one source of truth (`ActiveSession`) with a 1-second scheduler and websocket heartbeats emitted only for active tabs (frontend interaction-driven ping).

Recommended proof:
- two clients connected,
- stop activity in all tabs for one user,
- record transition times ONLINE -> AFK and AFK -> ONLINE,
- include p95 from sampled transitions.

## 4) History for >= 10 000 messages

Recommended proof:
1. Generate >=10k messages in one room.
2. Query `/api/messages/room/{roomId}`.
3. Confirm chronological order and no backend errors.
4. Store execution log and summary in `docs/perf/results/`.
