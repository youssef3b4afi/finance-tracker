import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatMonth, formatMoney } from '../../format'
import type { MonthlyTotal } from '../types'
import { axisTick, tooltipStyles } from './chartStyles'

// Axis labels only need the size of the number: 15000 -> "15K".
const compactFormat = new Intl.NumberFormat(undefined, { notation: 'compact' })

/** Income vs expenses per month (US-12); one bar pair per month, oldest on the left. */
export default function MonthlyBarChart({ data }: { data: MonthlyTotal[] }) {
  const rows = data.map((d) => ({ ...d, label: formatMonth(d.month) }))

  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart data={rows} barGap={2}>
        {/* Horizontal grid lines only, in the border color: enough to read values, not to compete with bars. */}
        <CartesianGrid vertical={false} stroke="var(--border)" />
        <XAxis dataKey="label" tick={axisTick} tickLine={false} axisLine={{ stroke: 'var(--border)' }} />
        <YAxis tick={axisTick} tickLine={false} axisLine={false} width={48} tickFormatter={(v) => compactFormat.format(v)} />
        <Tooltip
          formatter={(value) => formatMoney(Number(value))}
          cursor={{ fill: 'var(--border)', opacity: 0.4 }}
          {...tooltipStyles}
        />
        {/* itemSorter null: keep the bars' order (Income, Expenses) instead of Recharts' alphabetical default. */}
        <Legend itemSorter={null} formatter={(value) => <span style={{ color: 'var(--text)' }}>{value}</span>} />
        {/* Rounded tops only: the bottom sits flat on the axis. maxBarSize keeps bars slim with few months. */}
        <Bar dataKey="income" name="Income" fill="var(--income)" radius={[4, 4, 0, 0]} maxBarSize={28} />
        <Bar dataKey="expenses" name="Expenses" fill="var(--expense)" radius={[4, 4, 0, 0]} maxBarSize={28} />
      </BarChart>
    </ResponsiveContainer>
  )
}
