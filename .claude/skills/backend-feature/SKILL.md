---
name: backend-feature
description: Use when adding or changing a backend feature in the Spring Boot app — a new entity, repository, service, DTO, mapper, or REST controller (e.g. categories, transactions, dashboard). Gives the package layout, layer rules and build order.
---

# Backend feature slice

## Package layout (package by feature)

```
com.youssef3b4afi.financetracker
├── <feature>/            e.g. category/, transaction/, dashboard/
│   ├── <Name>.java               JPA entity
│   ├── <Name>Repository.java     Spring Data interface
│   ├── <Name>Service.java        business rules, @Transactional
│   ├── <Name>Controller.java     HTTP only, under /api/<plural>
│   ├── <Name>Mapper.java         hand-written entity <-> DTO
│   └── dto/                      Java records: <Name>Request, <Name>Response
├── auth/, user/                  see the auth-security skill
└── common/                       GlobalExceptionHandler, NotFoundException
```

Why package by feature: everything about "transactions" sits in one folder, so a change touches one place; global `controller/` + `service/` folders grow into long unrelated lists.

## Build order (one vertical slice)

1. **Migration** — `db/migration/V<n>__<desc>.sql` (see the db-migration skill). Schema first, because `ddl-auto=validate` fails startup if the entity and table disagree.
2. **Entity** — `@Entity`, `@Table(name = "...")`, `Long id` with `@GeneratedValue(strategy = IDENTITY)`, money as `BigDecimal` (`@Column(precision = 12, scale = 2)`), dates as `LocalDate`, relations `@ManyToOne(fetch = FetchType.LAZY)`. Every user-owned entity has a `user` relation.
3. **Repository** — `JpaRepository<Entity, Long>`. Always scope by user: `findByIdAndUserId`, `findAllByUserId(Long userId, Pageable p)`. Never call the plain `findById` for user-owned data.
4. **DTOs** — `record`s with Jakarta validation (`@NotNull`, `@NotBlank`, `@Positive`, `@Digits(integer = 10, fraction = 2)`). Separate `Request` (no id, no userId) and `Response` records.
5. **Mapper** — a `@Component` (or static methods) with `toResponse(entity)` and `apply(request, entity)`. Hand-written on purpose so the mapping is visible; no MapStruct for now.
6. **Service** — takes the current user id (from the controller), loads with user-scoped queries, throws `NotFoundException` when missing (→ 404). `@Transactional(readOnly = true)` on reads, `@Transactional` on writes.
7. **Controller** — `@RestController @RequestMapping("/api/<plural>")`. `@Valid @RequestBody` requests, returns Response DTOs or `ResponseEntity` (201 + Location for create, 204 for delete). **Never return an entity** — it leaks fields and triggers lazy-loading errors during JSON serialization.
8. **Tests** — see the backend-testing skill.

## Layer rules

- Controller → Service → Repository only; a controller never touches a repository.
- Business rules (e.g. "can't delete a category with transactions") live in the service, not the controller or the DB alone.
- Errors: throw domain exceptions; `common/GlobalExceptionHandler` (`@RestControllerAdvice`) turns them into `ProblemDetail` responses (404, 409, 400 with field errors).
- List endpoints return `Page<Response>` via `page.map(mapper::toResponse)`; default sort newest first.

## Spring Boot 4 notes (tutorials for Boot 3 differ)

- Jackson 3: `tools.jackson.databind.*` (e.g. `tools.jackson.databind.json.JsonMapper`). Annotations such as `@JsonIgnore` stay in `com.fasterxml.jackson.annotation`.
- Starters are modular: features need their own starter (`spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-flyway`, ...). If an auto-configuration seems missing, a starter is missing.
- Use `jakarta.*` (never `javax.*`) for persistence and validation.

See `templates.md` in this folder for a full example slice.
