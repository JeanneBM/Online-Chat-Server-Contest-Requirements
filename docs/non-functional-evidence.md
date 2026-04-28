# Non-functional evidence

## 1) Load test for 300 concurrent users

Prepared script: `load-tests/chat-300-users.js`.

Run command:

```bash
k6 run -e BASE_URL=http://localhost:8080 load-tests/chat-300-users.js
```

What it validates:
- 300 VUs in parallel.
- threshold `p(95)<3000ms` for HTTP request duration.
- basic auth flow and authenticated API call.

## 2) Message delivery <= 3 seconds

Measurement method:
- open two browser sessions/users,
- send a room message from user A,
- verify user B receives websocket event quickly,
- optionally capture browser DevTools timestamps.

Target:
- end-to-end delivery up to 3 seconds.

## 3) Presence update below 2 seconds

Implementation proof:
- AFK/ONLINE recalculation scheduler reduced to 1 second.
- client sends heartbeat `/app/presence.ping` every 1 second after STOMP connection.

Validation:
- run two users, stop interaction in all tabs of one user,
- verify status transitions to AFK in approximately 60-62 seconds,
- verify active interaction in any tab returns ONLINE quickly (around 1-2 seconds).

## 4) History for >= 10 000 messages

Scenario:
1. Create room.
2. Use a script (or repeated API calls) to produce 10k text messages.
3. Verify room history endpoint `/api/messages/room/{roomId}` returns ordered history.
4. Verify UI can render and scroll without backend errors.

Expected result:
- persistent, chronological history with at least 10 000 records.
