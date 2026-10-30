# Online Chat Server

This repository contains an implementation of a web chat server prepared based on workshop requirements.

> The full, original task description is available in `README.txt`.

## Project scope

The application implements a classic internet messenger scenario, including:

- user registration and login,
- public and private rooms,
- 1:1 messages,
- friends list and invitations,
- user presence (online/AFK/offline),
- message history,
- file sharing,
- basic moderation features.

## Technical requirements

- Docker + Docker Compose
- Java 21 for local Maven builds (Docker supplies its own Java runtime)

## Quick start

1. Clone the repository.
2. Copy `.env.example` to `.env` locally and set NEW, unique `POSTGRES_PASSWORD` and `JWT_SECRET` values (JWT secret: at least 32 random bytes). Never commit `.env`. Then run the application from the repository root:

```bash
docker compose up --build
```

3. Open http://localhost:8080. Register an account first; the form does not contain a pre-created demo account.
   The `public` room is created on the first registration or login. Use a second account in a private browser window to test conversations.
   For direct messages, send a friend invitation and accept it from the second account under **Received invitations**.
   Room message deletion requires the room owner or an administrator.

After updating an existing installation, rebuild with `docker compose up --build -d` and reload the page.
If a previous session is no longer valid, sign in again. Keep the database volume to preserve existing data.
<img width="1313" height="1006" alt="obraz" src="https://github.com/user-attachments/assets/76de8368-2991-4d8c-8862-eec6617cb995" />

## Repository structure

- `src/main/java` – application code (Spring Boot),
- `src/main/resources` – configuration and static frontend,
- `src/test/java` – tests,
- `scripts/` – helper scripts (e.g., smoke/perf),
- `docs/` – supplementary documentation and requirements analysis.

## Tests and validation

Example local commands:

```bash
./mvnw test
./mvnw verify
node --test src/test/frontend/chat.test.cjs
```

Backend regression tests use an isolated H2 database and do not require a running PostgreSQL server.
They cover registration/login identity, room history and deletion, invitations, direct messages, file access, and WebSocket authentication.

## Requirements documentation

- `README.txt` – full contest task description,
- `docs/requirements-gap-analysis.md` – requirements coverage map,
- `docs/non-functional-evidence.md` – evidence of meeting non-functional requirements,
- `docs/perf/README.md` – performance testing information.

## Previously committed local credentials

Committed `.env` values were removed and shared runtime password/JWT defaults were removed. Treat previously published values as compromised if used anywhere; rotate them. For an existing PostgreSQL volume, changing `.env` alone does not change the database password: rotate it in the database and update clients together. Rotating the JWT signing secret invalidates existing sessions. Earlier Git commits may still contain the old values; history cleanup is separate.
