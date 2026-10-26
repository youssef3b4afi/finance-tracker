import client from '../api/client'
import type { CategoryExpense, MonthlyTotal, Summary } from './types'

// `month` is always sent ("yyyy-MM"): left out, the backend would use the month on the server's clock,
// which can differ from the user's around midnight on the 1st.

export function getSummary(month: string): Promise<Summary> {
  return client.get<Summary>('/dashboard/summary', { params: { month } }).then((r) => r.data)
}

export function getExpensesByCategory(month: string): Promise<CategoryExpense[]> {
  return client.get<CategoryExpense[]>('/dashboard/expenses-by-category', { params: { month } }).then((r) => r.data)
}

/** The `months` months ending with `month`, oldest first. */
export function getMonthly(month: string, months: number): Promise<MonthlyTotal[]> {
  return client.get<MonthlyTotal[]>('/dashboard/monthly', { params: { month, months } }).then((r) => r.data)
}
