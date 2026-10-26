import { formatMoney } from '../format'
import type { Summary } from './types'

/** Income, expenses and balance for one month (US-10). All three numbers come from the backend. */
export default function SummaryCards({ summary }: { summary: Summary }) {
  const cards = [
    { label: 'Income', value: summary.income, className: 'stat-income' },
    { label: 'Expenses', value: summary.expenses, className: 'stat-expense' },
    { label: 'Balance', value: summary.balance, className: '' },
  ]
  return (
    <div className="stats">
      {cards.map(({ label, value, className }) => (
        // The colored top edge says income/expense; the number itself stays in normal text color.
        <div key={label} className={`card stat ${className}`}>
          <span className="muted">{label}</span>
          <strong className="stat-value">{formatMoney(value)}</strong>
        </div>
      ))}
    </div>
  )
}
