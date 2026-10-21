---
name: auth-security
description: Use when working on registration, login, JWT tokens, Spring Security config, getting the current user, or enforcing that users only access their own data (backend auth/ and user/ packages, frontend token handling).
---

# Auth and data ownership

## Approach

Stateless JWT with Spring Security's built-in resource server — no extra JWT library.
- Dependencies: `spring-boot-starter-security`, `spring-boot-starter-oauth2-resource-server` (brings Nimbus for signing/verifying).
- Tokens are HS256-signed with a secret from config: `app.jwt.secret=${JWT_SECRET:<dev-only 32+ byte value>}`, `app.jwt.ttl=PT8H`.
- Token `sub` = the user's numeric id (as a string). `email` is an extra claim.

Why this over sessions: the React app and API are separate; a bearer token in the `Authorization` header works the same in dev (Vite proxy) and production, and needs no CSRF handling.

## Endpoints

| Method | Path | Auth | Result |
|---|---|---|---|
| POST | `/api/auth/register` | public | 201; creates user + default categories (US-4) |
| POST | `/api/auth/login` | public | 200 `{ token, expiresAt }`; 401 on bad credentials |
| GET | `/api/auth/me` | token | current user's id + email |
| GET | `/api/health` | public | |
| everything else under `/api/**` | token | |

## Backend pieces (`auth/`, `user/`)

- `User` entity: `id`, `email` (unique, stored lowercase), `passwordHash`, `createdAt`.
- `PasswordEncoder` bean: `PasswordEncoderFactories.createDelegatingPasswordEncoder()` (bcrypt). Never store or log raw passwords.
- `JwtService`: builds tokens with `JwtEncoder` (`NimbusJwtEncoder` over an `ImmutableSecret`); `JwtDecoder` bean is `NimbusJwtDecoder.withSecretKey(key).build()`.
- `SecurityConfig`: `SecurityFilterChain` with `csrf(disable)` (stateless API), `sessionManagement(STATELESS)`, `permitAll` for the public paths above, `anyRequest().authenticated()`, `oauth2ResourceServer(o -> o.jwt(withDefaults()))`.
- `common/CurrentUser.id(Jwt jwt)` → `Long.valueOf(jwt.getSubject())`. Controllers take `@AuthenticationPrincipal Jwt jwt` and pass the id to services.

## Ownership rules (US-3)

- Every user-owned query includes the user id: `findByIdAndUserId(id, userId)`. Services never trust ids from the request body for ownership.
- Another user's resource returns **404, not 403** — 403 would confirm the id exists.
- Request DTOs never contain `userId`.
- Cross-references are checked too: a transaction's `categoryId` must belong to the same user.

## Frontend side

- Store the token in `localStorage` (simple; acceptable for this learning project — note that it is readable by any script on the page, so avoid injecting untrusted HTML).
- The shared axios client adds `Authorization: Bearer <token>`; on a 401 response it clears the token and redirects to `/login` (see the frontend-feature skill).
- No CORS config needed in dev: the Vite proxy makes `/api` same-origin.

## Tests

Cover: register duplicate email → 409, wrong password → 401, missing token → 401, user B reading user A's data → 404. See the backend-testing skill.
