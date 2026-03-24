---
name: docker-up
description: Start or stop the full OBS stack with Docker Compose. Use when you need the full system running (postgres + backend + frontend). Handles build, up, down, and logs commands.
---

# OBS Docker Stack

Manage the full stack via Docker Compose from the project root.

## Start (with rebuild)

```bash
docker compose -f infrastructure/docker-compose.yml up --build
```

## Start (cached images)

```bash
docker compose -f infrastructure/docker-compose.yml up
```

## Stop and remove containers

```bash
docker compose -f infrastructure/docker-compose.yml down
```

## View logs

```bash
# All services
docker compose -f infrastructure/docker-compose.yml logs -f

# Single service
docker compose -f infrastructure/docker-compose.yml logs -f backend
docker compose -f infrastructure/docker-compose.yml logs -f frontend
docker compose -f infrastructure/docker-compose.yml logs -f postgres
```

## Services and URLs

| Service  | Container name | URL / Port |
|----------|---------------|------------|
| Frontend | obs-frontend  | http://localhost:4200 |
| Backend  | obs-backend   | http://localhost:8080/api/books |
| Postgres | obs-postgres  | localhost:5433 |

## Startup Order

1. `postgres` starts and passes healthcheck (`pg_isready`)
2. `backend` starts after postgres is healthy
3. `frontend` starts after backend is up

## Troubleshooting

**Backend fails to connect to postgres:** Check postgres healthcheck passed — wait for `obs-postgres` to log `database system is ready to accept connections`.

**Frontend shows blank page / API errors:** Confirm the Dockerfile uses `--configuration production` so `environment.prod.ts` is active (backend URL = `http://backend:8080/api`).

**Port conflicts:** Host port 5433 (not 5432) is used for postgres to avoid conflict with a local PostgreSQL installation.
