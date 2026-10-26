# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Personal finance tracker monorepo: a Spring Boot REST API (`backend/`), a React + TypeScript SPA (`frontend/`), and a PostgreSQL database run via Docker (`docker-compose.yml`). The MVP (auth, categories, transactions, dashboard; `docs/user-stories.md`) is built end to end in both backend and frontend.

## Commands

Run everything from the repo root unless noted. On Windows PowerShell use `.\mvnw.cmd` instead of `./mvnw`.

Database (Postgres 17 on port 5432, database `finance_tracker`, user and password `finance`):
```
docker compose up -d db
```

Backend (`cd backend`, Java 21, Maven wrapper):
```
./mvnw spring-boot:run                                   # serves on http://localhost:8081
./mvnw test                                              # all tests
./mvnw test -Dtest=BackendApplicationTests               # single test class
./mvnw test -Dtest=BackendApplicationTests#contextLoads  # single test method
./mvnw package                                           # build jar into target/
```

Frontend (`cd frontend`, npm):
```
npm install
npm run dev       # Vite dev server, proxies /api -> http://localhost:8081
npm run build     # tsc -b type-check, then vite build
npm run lint      # eslint
```
There is no frontend test runner configured.

## Architecture

- **Frontend → backend wiring:** the frontend calls relative `/api/...` URLs; `frontend/vite.config.ts` proxies `/api` to the backend on port **8081** (not Spring's default 8080, set via `server.port` in `application.properties`). Keep all backend routes under `/api` so the proxy works. All requests go through the one axios instance in `frontend/src/api/client.ts`, which adds the JWT and sends the user to `/login` on a 401.
- **Backend:** Spring Boot 4.1 with `spring-boot-starter-webmvc`. All code lives in package `com.youssef3b4afi.financetracker`; `BackendApplication` is the entry point.
- **Database:** `application.properties` points at the docker-compose Postgres (credentials overridable via `DB_USERNAME`/`DB_PASSWORD`). Flyway migrations own the schema; Hibernate only validates it (`ddl-auto=validate`). Tests start their own Postgres with Testcontainers, so Docker must be running for `./mvnw test`.

## Roadmap and conventions

- MVP user stories and build order: `docs/user-stories.md` (tick them off as they ship).
- Detailed conventions live in project skills under `.claude/skills/` and load only when relevant: `backend-feature` (package-by-feature layers, DTOs, mappers), `db-migration` (Flyway), `backend-testing`, `auth-security` (JWT, data ownership), `frontend-feature` (axios, routing, Recharts). Keep this file short; put new detailed guidance in a skill.

## Repo quirks

- `.mcp.json` configures the JetBrains IDE MCP server (`idea`), so IntelliJ tools (build, run configurations, inspections, database) are available when the IDE is open.

## User Preferences
- Backend: Spring Boot 4, Java 21, layered architecture (controller → service → repository), DTOs for all API responses
- Frontend: React + TypeScript + Vite, Axios for API calls, Recharts for charts
- I'm learning: explain non-obvious decisions and don't write large features without a plan first