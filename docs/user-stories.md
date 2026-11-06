# User stories (MVP)

Build order: Auth → Categories → Transactions → Dashboard. Each story is a vertical slice: migration → API → UI → tests.

## Auth
- [x] **US-1** As a visitor, I can register with email and password so I have my own private ledger.
- [x] **US-2** As a user, I can log in and stay logged in (JWT) until I log out or the token expires.
- [x] **US-3** As a user, I can only see and change my own data (other users' ids return 404).

## Categories
- [x] **US-4** As a new user, I get default categories (Salary, Food, Rent, Transport, …) so I can start right away.
- [x] **US-5** As a user, I can create, rename and delete my own categories, each typed INCOME or EXPENSE.
- [x] **US-6** I can't delete a category that still has transactions (clear error), so no data is orphaned.

## Transactions
- [x] **US-7** As a user, I can record a transaction with amount, date, category and optional note.
- [x] **US-8** I can edit and delete a transaction.
- [x] **US-9** I can list transactions, paged and newest first, filtered by date range, category and type.

Validation: amount > 0 with 2 decimals (`BigDecimal`); income/expense comes from the category type; date required.

## Dashboard
- [x] **US-10** I see this month's total income, total expenses and balance.
- [x] **US-11** I see a donut chart of expenses by category for a chosen month.
- [x] **US-12** I see a bar chart of income vs expense for the last 6–12 months.

## Later (not MVP)

### Product
- Budgets per category, with a warning when a month goes over budget
- Recurring transactions (rent, salary) created automatically each month
- Multiple accounts (cash, bank, card) and transfers between them
- CSV import (bank statements) and export
- Search transactions by note; quick date filters ("this month", "last 3 months")
- Yearly report and savings rate; savings goals
- Tags on transactions, in addition to categories
- Attach a receipt photo to a transaction
- Dark mode, better mobile layout, installable PWA; Arabic translation (right-to-left)

### Accounts and security
- Password reset and email verification (needs an email service)
- Change password; delete account and all its data
- Refresh tokens so users aren't logged out after 8 hours; real logout (revoke tokens)
- Rate-limit login attempts; "Sign in with Google"

### Engineering
- CD: Dockerfiles, images on GitHub Container Registry, deploy with a managed Postgres
- Require CI to pass before merging into `main` (branch protection)
- Frontend tests: Vitest + React Testing Library, then Playwright end-to-end tests in CI
- API docs with Swagger UI (springdoc-openapi)
- Code coverage (JaCoCo) reported in CI
- Dependabot for dependency updates; CodeQL security scanning
- Spring Boot Actuator health/metrics for the deployed app; database backups
