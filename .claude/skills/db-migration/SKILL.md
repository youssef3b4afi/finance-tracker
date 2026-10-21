---
name: db-migration
description: Use when creating or changing database tables, columns, indexes or seed data for the Postgres database — writing Flyway migrations under backend/src/main/resources/db/migration.
---

# Database migrations (Flyway)

Flyway owns the schema; Hibernate only checks it (`spring.jpa.hibernate.ddl-auto=validate`).
Why: `update` silently alters tables, can't rename or drop columns, and gives no history. A migration file is reviewable, repeatable on every machine, and runs the same way in tests.

## Setup (once)

- `backend/pom.xml`: `spring-boot-starter-flyway` and `org.flywaydb:flyway-database-postgresql` (Flyway needs the database-specific module for Postgres).
- Files go in `backend/src/main/resources/db/migration/`.
- Postgres must be running: `docker compose up -d db` from the repo root.

## Rules

- Name: `V<n>__<snake_case_description>.sql` — two underscores, `n` = next integer (`V1__init.sql`, `V2__add_transaction_note.sql`).
- **Never edit a migration that has already run** (Flyway stores a checksum and refuses to start). Fix mistakes with a new migration. Only while a migration exists solely on your machine may you edit it — then reset with `docker compose down -v` (deletes all local data).
- One logical change per file.

## Conventions

| Thing | Convention |
|---|---|
| Table names | plural snake_case: `users`, `categories`, `transactions` |
| Primary key | `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY` (matches `GenerationType.IDENTITY`) |
| Money | `NUMERIC(12,2) NOT NULL` — never `REAL`/`DOUBLE` (floating point loses cents) |
| Dates | `DATE` for transaction dates, `TIMESTAMPTZ` for created/updated times |
| Enums | `VARCHAR(10)` + `CHECK (type IN ('INCOME','EXPENSE'))` (matches `@Enumerated(STRING)`) |
| Foreign keys | `user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE` |
| Indexes | index every FK and the columns you filter/sort on, e.g. `(user_id, date DESC)` on transactions |
| Uniqueness | per-user uniqueness: `UNIQUE (user_id, name)` |

Postgres does not index foreign key columns automatically — add them yourself.

## Example

```sql
-- V2__create_categories.sql
CREATE TABLE categories (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name    VARCHAR(50) NOT NULL,
    type    VARCHAR(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    UNIQUE (user_id, name)
);
CREATE INDEX idx_categories_user ON categories (user_id);
```

Default categories for new users (US-4) are created in `AuthService` at registration, not as SQL seed data, because they belong to each user.

## Check

Start the backend: Flyway logs `Successfully applied N migrations`, and Hibernate validation fails fast if an entity doesn't match the table.
