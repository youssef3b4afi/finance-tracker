---
name: frontend-feature
description: Use when building or changing React frontend features — pages, routes, forms, API calls with axios, auth token handling, or Recharts dashboard charts in frontend/src.
---

# Frontend feature

Stack: React 19 + TypeScript + Vite, `axios`, `react-router-dom` v7, `recharts`. Dev server proxies `/api` to the backend (`vite.config.ts`), so always call relative `/api/...` URLs.

## Layout (by feature)

```
frontend/src/
├── api/client.ts              the one axios instance (baseURL '/api', token interceptor)
├── auth/                      AuthContext, LoginPage, RegisterPage, RequireAuth
├── categories/                api.ts, types.ts, CategoriesPage.tsx, CategoryForm.tsx
├── transactions/              api.ts, types.ts, TransactionsPage.tsx, TransactionForm.tsx
├── dashboard/                 api.ts, types.ts, DashboardPage.tsx, charts/
├── components/                small shared UI (Button, ErrorMessage, Spinner)
└── App.tsx                    routes
```

## API layer

- `api/client.ts`: `axios.create({ baseURL: '/api' })`; a request interceptor adds `Authorization: Bearer <token>` from localStorage; a response interceptor on 401 clears the token and sends the user to `/login`.
- Each feature's `types.ts` mirrors the backend DTO records **exactly** (same field names). Money arrives as a JSON number or string from `BigDecimal` — keep it as returned and format only for display.
- Each feature's `api.ts` exports typed functions: `listCategories(): Promise<Category[]>` using `client.get<Category[]>('/categories').then(r => r.data)`. Components never call axios directly.
- Backend errors are `ProblemDetail` JSON (`{ title, status, detail, errors? }`); show `detail` to the user.

## Routing

`createBrowserRouter` in `App.tsx`: public `/login`, `/register`; everything else wrapped in `<RequireAuth>` (redirects to `/login` without a token): `/` dashboard, `/transactions`, `/categories`.

## Components

- Function components + hooks. Data loading: `useEffect` + `useState` for `data / loading / error` (keep it explicit while learning; a library like TanStack Query can come later).
- Controlled forms; validate the obvious client-side (required, amount > 0) but treat backend validation as the source of truth.
- Format money with `Intl.NumberFormat(undefined, { style: 'currency', currency: 'EUR' })` in one helper; dates are ISO `YYYY-MM-DD` strings in the API.

## Charts (dashboard, US-10–12)

- Wrap every chart in `<ResponsiveContainer width="100%" height={300}>`.
- Expenses by category: `PieChart` + `Pie` (`innerRadius` for a donut) + `Cell` per slice + `Tooltip` + `Legend`.
- Monthly income vs expense: `BarChart` with `XAxis dataKey="month"`, `YAxis`, two `Bar`s (income, expense), `Tooltip`.
- The backend does the aggregation (dashboard endpoints); the frontend only renders the arrays.
- Use one fixed color per category type (e.g. income green, expense red) and a stable palette for categories.

## Check

`npm run lint` and `npm run build` (type-checks) from `frontend/`; then `npm run dev` with the backend running and try the page.
