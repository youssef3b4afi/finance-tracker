# Frontend plan

Same workflow as the backend: one `feature/frontend-<name>` branch per feature off `main`, one commit per layer,
merged with `--no-ff`. Steps are numbered `(digit)(letter)`, continuing after the backend (1 Auth, 2 Categories,
3 Transactions, 4 Dashboard). Tick user stories in `docs/user-stories.md` only once their UI ships.

Conventions: see the `frontend-feature` skill. Decisions for this project:
- **Currency:** Egyptian pounds — `Intl.NumberFormat(undefined, { style: 'currency', currency: 'EGP' })`, in one helper.
- **Styling:** plain CSS (no UI library).
- **Data loading:** `useEffect` + `useState` (data / loading / error); no query library yet.

Check after every step (no test runner): `npm run lint`, `npm run build` (type-checks), then `npm run dev` with the
backend running (`.\mvnw.cmd spring-boot:run` in `backend/`) and try it in the browser.

## 5 · Foundation (`feature/frontend-foundation`)
- [x] **5a API layer**
  - `api/client.ts`: the one axios instance, `baseURL: '/api'`; request interceptor adds `Authorization: Bearer <token>`;
    a 401 response clears the token and redirects to `/login`.
  - `api/types.ts`: `ProblemDetail` (`title, status, detail, errors?`) and `PageResponse<T>`
    (`content, page, size, totalElements, totalPages`), mirroring the backend.
  - `api/errors.ts`: one helper that extracts `detail` and per-field `errors` from a failed request.
- [x] **5b Shared UI**
  - `components/`: `Layout` (nav: Dashboard, Transactions, Categories, Logout), `ErrorMessage`, `Spinner`.
  - `format.ts`: money (EGP) and date formatters.
  - Remove the Vite starter page and its CSS.

## 6 · Auth UI (`feature/frontend-auth`) — US-1, US-2, US-3
- [ ] **6a API**: `auth/types.ts`, `auth/api.ts` — `register` (`POST /auth/register`), `login` (`POST /auth/login` →
  `{ token, expiresAt }`), `me` (`GET /auth/me` → `{ id, email }`).
- [ ] **6b State**: `AuthContext` keeps token + `expiresAt` in localStorage, loads the user from `/me`,
  exposes `login()` / `logout()`; `RequireAuth` redirects to `/login` when there is no token or it has expired.
- [ ] **6c Pages and routes**: `LoginPage`, `RegisterPage` (logs in automatically after registering);
  `App.tsx` with `createBrowserRouter` — public `/login`, `/register`; everything else inside `RequireAuth`.

## 7 · Categories UI (`feature/frontend-categories`) — US-4, US-5, US-6
- [ ] **7a API**: types + `listCategories`, `createCategory` (`{ name, type }`), `renameCategory` (`PUT`, `{ name }`),
  `deleteCategory`.
- [ ] **7b Page**: `CategoriesPage` — Income and Expense lists, create form, inline rename, delete. The backend's
  409 message ("Category 'Food' still has transactions") is shown as is.

## 8 · Transactions UI (`feature/frontend-transactions`) — US-7, US-8, US-9
- [ ] **8a API**: types + list (`from, to, categoryId, type, page, size`), get, create, update, delete.
- [ ] **8b Form**: `TransactionForm` for create and edit — category select grouped by type, amount, date
  (default today), optional note; backend 400 field errors shown under each input.
- [ ] **8c Page**: `TransactionsPage` — filter bar (from/to, type, category), table, paging, edit/delete per row.

## 9 · Dashboard UI (`feature/frontend-dashboard`) — US-10, US-11, US-12
- [ ] **9a API**: types + `getSummary(month)`, `getExpensesByCategory(month)`, `getMonthly(month, months)`.
- [ ] **9b Summary**: `SummaryCards` (income, expenses, balance) and a month picker.
- [ ] **9c Charts**: expenses donut (`PieChart` + `Pie innerRadius` + `Cell`), monthly `BarChart`
  (income green, expenses red), each in `<ResponsiveContainer width="100%" height={300}>`.
- [ ] **9d Page**: `DashboardPage` at `/` composing the above.

## Decisions worth knowing
- **Month picker = `<input type="month">`**: its value is already `yyyy-MM`, the backend's format. The frontend always
  sends the month, so "this month" follows the user's clock, not the server's time zone.
- **Amounts arrive as JSON numbers** (`2000.00` arrives as `2000`): format for display only, never sum in the browser —
  all totals come from the backend.
- **Token in localStorage**: simple and survives reloads; the trade-off is that any script on the page (XSS) could
  read it. Acceptable here; switching to an httpOnly cookie later only touches `client.ts` and `AuthContext`.
