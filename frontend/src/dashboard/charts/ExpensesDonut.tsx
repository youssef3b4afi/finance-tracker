import { Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import type { Category } from '../../categories/types'
import { formatMoney } from '../../format'
import type { CategoryExpense } from '../types'
import { tooltipStyles } from './chartStyles'

/** --series-1 … --series-8 in index.css: a palette checked to stay distinguishable for color-blind users. */
const SERIES_COLORS = Array.from({ length: 8 }, (_, i) => `var(--series-${i + 1})`)
const OTHER_COLOR = 'var(--series-other)'

interface Slice {
  name: string
  total: number
  /** Recharts colors each slice from its data item's `fill`. */
  fill: string
}

/**
 * Turns the backend totals into slices. A category's color comes from its fixed position among
 * the user's expense categories (by id), not from its rank this month, so "Food" keeps the same
 * color when you switch months. Past the 8 palette colors, categories fold into one gray "Other".
 */
function toSlices(data: CategoryExpense[], categories: Category[]): Slice[] {
  const expenseIds = categories
    .filter((c) => c.type === 'EXPENSE')
    .map((c) => c.id)
    .sort((a, b) => a - b)

  const slices: Slice[] = []
  let otherTotal = 0
  for (const item of data) {
    const position = expenseIds.indexOf(item.categoryId)
    if (position >= 0 && position < SERIES_COLORS.length) {
      slices.push({ name: item.categoryName, total: item.total, fill: SERIES_COLORS[position] })
    } else {
      // Summing for display only; the per-category totals themselves come from the backend.
      otherTotal += item.total
    }
  }
  if (otherTotal > 0) {
    slices.push({ name: 'Other', total: otherTotal, fill: OTHER_COLOR })
  }
  return slices.sort((a, b) => b.total - a.total)
}

interface ExpensesDonutProps {
  data: CategoryExpense[]
  categories: Category[]
  /** The month's total expenses (from the summary), for the percentages. */
  totalExpenses: number
}

/** Expenses by category for one month (US-11). */
export default function ExpensesDonut({ data, categories, totalExpenses }: ExpensesDonutProps) {
  if (data.length === 0) {
    return <p className="muted">No expenses this month.</p>
  }
  const slices = toSlices(data, categories)

  return (
    <div className="donut">
      <ResponsiveContainer width="100%" height={300}>
        <PieChart>
          {/* innerRadius makes it a donut; the surface-colored stroke leaves a thin gap between slices. */}
          <Pie
            data={slices}
            dataKey="total"
            nameKey="name"
            innerRadius="58%"
            outerRadius="85%"
            stroke="var(--surface)"
            strokeWidth={2}
          />
          <Tooltip formatter={(value) => formatMoney(Number(value))} {...tooltipStyles} />
        </PieChart>
      </ResponsiveContainer>
      {/* Legend and table in one: names and numbers, so nothing depends on telling colors apart. */}
      <table className="legend-table">
        <tbody>
          {slices.map((slice) => (
            <tr key={slice.name}>
              <td>
                <span className="swatch" style={{ background: slice.fill }} />
                {slice.name}
              </td>
              <td className="number">{formatMoney(slice.total)}</td>
              <td className="number muted">
                {totalExpenses > 0 ? `${Math.round((slice.total / totalExpenses) * 100)}%` : ''}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
