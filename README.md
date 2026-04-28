# Online Chat Server – contest solution

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
- Java 17+

## Quick start

1. Clone the repository.
2. Run the application from the repository root:

```bash
docker compose up --build
```

3. Open your browser and go to the application URL defined in `docker-compose.yml`.

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
```

## Requirements documentation

- `README.txt` – full contest task description,
- `docs/requirements-gap-analysis.md` – requirements coverage map,
- `docs/non-functional-evidence.md` – evidence of meeting non-functional requirements,
- `docs/perf/README.md` – performance testing information.
