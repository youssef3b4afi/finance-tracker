# User stories (MVP)

Build order: Auth → Categories → Transactions → Dashboard. Each story is a vertical slice: migration → API → UI → tests.

## Auth
- [ ] **US-1** As a visitor, I can register with email and password so I have my own private ledger.
- [ ] **US-2** As a user, I can log in and stay logged in (JWT) until I log out or the token expires.
- [ ] **US-3** As a user, I can only see and change my own data (other users' ids return 404).

## Categories
- [ ] **US-4** As a new user, I get default categories (Salary, Food, Rent, Transport, …) so I can start right away.
- [ ] **US-5** As a user, I can create, rename and delete my own categories, each typed INCOME or EXPENSE.
- [ ] **US-6** I can't delete a category that still has transactions (clear error), so no data is orphaned.

## Transactions
- [ ] **US-7** As a user, I can record a transaction with amount, date, category and optional note.
- [ ] **US-8** I can edit and delete a transaction.
- [ ] **US-9** I can list transactions, paged and newest first, filtered by date range, category and type.

Validation: amount > 0 with 2 decimals (`BigDecimal`); income/expense comes from the category type; date required.

## Dashboard
- [ ] **US-10** I see this month's total income, total expenses and balance.
- [ ] **US-11** I see a donut chart of expenses by category for a chosen month.
- [ ] **US-12** I see a bar chart of income vs expense for the last 6–12 months.

## Later (not MVP)
Budgets per category, multiple accounts and transfers, recurring transactions, CSV import/export.
