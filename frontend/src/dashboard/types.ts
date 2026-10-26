// Mirror the backend's dashboard/dto records. Months are "yyyy-MM" strings (Java YearMonth).

/** Cards for one month (US-10). balance = income - expenses, and can be negative. */
export interface Summary {
  month: string
  income: number
  expenses: number
  balance: number
}

/** One donut slice (US-11). */
export interface CategoryExpense {
  categoryId: number
  categoryName: string
  total: number
}

/** One bar group (US-12); 0 for months without transactions. */
export interface MonthlyTotal {
  month: string
  income: number
  expenses: number
}
