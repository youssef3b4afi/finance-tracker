---
name: backend-testing
description: Use when writing, fixing or running backend tests (JUnit 5, MockMvc, @WebMvcTest, @DataJpaTest, Testcontainers) for the Spring Boot app.
---

# Backend tests

## Which test type

| What you test | Annotation | Database | Speed |
|---|---|---|---|
| Service business rules | plain JUnit + Mockito (`@ExtendWith(MockitoExtension.class)`) | none (mocked repositories) | fastest |
| Controller: status codes, JSON, validation, security | `@WebMvcTest(XController.class)` + `MockMvc`, service as `@MockitoBean` | none | fast |
| Repository: custom queries, user scoping, aggregates | `@DataJpaTest` + Testcontainers Postgres | real Postgres | medium |
| Full flow (register → login → create → list) | `@SpringBootTest` + Testcontainers | real Postgres | slow — use a few |

Why real Postgres instead of H2: our queries and migrations use Postgres SQL (`NUMERIC`, `TIMESTAMPTZ`, identity columns); H2 behaves differently and hides bugs.

## Spring Boot 4 specifics

- Test slices are split into modules. Add `spring-boot-starter-webmvc-test` (for `@WebMvcTest`) and `spring-boot-starter-data-jpa-test` (for `@DataJpaTest`) with `<scope>test</scope>`; their packages are `org.springframework.boot.webmvc.test.autoconfigure` and `org.springframework.boot.data.jpa.test.autoconfigure`.
- `@MockBean` is gone — use `@MockitoBean` (`org.springframework.test.context.bean.override.mockito`).
- Testcontainers: `org.testcontainers:testcontainers-postgresql` + `spring-boot-testcontainers`; declare the container with `@ServiceConnection` so Spring wires the datasource automatically:

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {
    @Bean @ServiceConnection
    PostgreSQLContainer postgres() { return new PostgreSQLContainer("postgres:17"); }
}
```
Import it with `@Import(TestcontainersConfig.class)`. Testcontainers needs Docker Desktop running.

If an import doesn't resolve, check the actual package in the resolved jar (IDE or `./mvnw dependency:tree`) rather than guessing from Boot 3 tutorials.

## Must-have cases per feature

- Happy path for each endpoint (status + body).
- Validation: invalid request → 400 with field errors.
- **Ownership: user B requesting user A's resource → 404.** (US-3, most important security test.)
- Unauthenticated request → 401 (in `@WebMvcTest`, use `.with(jwt().jwt(j -> j.subject("1")))` from `spring-security-test` for authenticated calls).
- Business-rule conflicts → 409 (e.g. deleting a category in use).

## Running

From `backend/` (PowerShell: `.\mvnw.cmd`):
```
./mvnw test                                   # all
./mvnw test -Dtest=CategoryServiceTest        # one class
./mvnw test -Dtest=CategoryServiceTest#delete* # matching methods
```

Naming: `<ClassUnderTest>Test`, methods describe behaviour: `deleteFailsWhenCategoryHasTransactions()`.
