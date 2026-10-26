import { useEffect, useState } from 'react'
import { getApiError } from '../api/errors'
import { listCategories } from '../categories/api'
import type { Category } from '../categories/types'
import ErrorMessage from '../components/ErrorMessage'
import Spinner from '../components/Spinner'
import { currentMonth } from '../dates'
import { formatMonth } from '../format'
import { getExpensesByCategory, getMonthly, getSummary } from './api'
import ExpensesDonut from './charts/ExpensesDonut'
import MonthlyBarChart from './charts/MonthlyBarChart'
import MonthPicker from './MonthPicker'
import SummaryCards from './SummaryCards'
import type { CategoryExpense, MonthlyTotal, Summary } from './types'

/** How many months the bar chart shows, ending with the chosen month. */
const MONTHS_SHOWN = 6

interface DashboardData {
  summary: Summary
  byCategory: CategoryExpense[]
  monthly: MonthlyTotal[]
  categories: Category[]
}

export default function DashboardPage() {
  const [month, setMonth] = useState(currentMonth)
  const [data, setData] = useState<DashboardData | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let ignore = false
    // The four requests don't depend on each other, so send them at the same time
    // and wait for all of them: the page then updates in one go.
    Promise.all([getSummary(month), getExpensesByCategory(month), getMonthly(month, MONTHS_SHOWN), listCategories()])
      .then(([summary, byCategory, monthly, categories]) => {
        if (!ignore) {
          setData({ summary, byCategory, monthly, categories })
          setError(null)
        }
      })
      .catch((err) => {
        if (!ignore) setError(getApiError(err).message)
      })
    return () => {
      ignore = true
    }
  }, [month])

  return (
    <>
      <div className="page-header">
        <h1>Dashboard</h1>
        <MonthPicker month={month} onChange={setMonth} />
      </div>
      <ErrorMessage message={error} />
      {data === null ? (
        !error && <Spinner />
      ) : (
        // While a new month loads, the previous month's numbers stay visible instead of flashing a spinner.
        <>
          <SummaryCards summary={data.summary} />
          <div className="grid-2">
            <section className="card">
              <h2>Expenses by category · {formatMonth(month)}</h2>
              <ExpensesDonut data={data.byCategory} categories={data.categories} totalExpenses={data.summary.expenses} />
            </section>
            <section className="card">
              <h2>Income vs expenses · last {MONTHS_SHOWN} months</h2>
              <MonthlyBarChart data={data.monthly} />
            </section>
          </div>
        </>
      )}
    </>
  )
}
