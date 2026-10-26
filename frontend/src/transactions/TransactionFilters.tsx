import type { Category, CategoryType } from '../categories/types'
import Field from '../components/Field'
import type { TransactionFilter } from './types'

interface TransactionFiltersProps {
  categories: Category[]
  filter: TransactionFilter
  onChange: (filter: TransactionFilter) => void
}

/** From / to / type / category inputs. Controlled: the page owns the filter, this only edits it. */
export default function TransactionFilters({ categories, filter, onChange }: TransactionFiltersProps) {
  // Once a type is chosen, only offer categories of that type.
  const categoryOptions = filter.type ? categories.filter((c) => c.type === filter.type) : categories
  // Cleared filters are stored as undefined (see the inputs below), so any defined value is an active filter.
  const isFiltered = Object.values(filter).some((value) => value !== undefined)

  function changeType(value: string) {
    const type = (value || undefined) as CategoryType | undefined
    const category = categories.find((c) => c.id === filter.categoryId)
    // Drop a selected category of the other type; together they would always match nothing.
    const categoryId = type && category && category.type !== type ? undefined : filter.categoryId
    onChange({ ...filter, type, categoryId })
  }

  return (
    <section className="card">
      <h2>Filter transactions</h2>
      {/* The hint changes once a filter is set, so it's clear the table below is not the full list. */}
      <p className="muted card-hint">
        {isFiltered
          ? 'Showing only the transactions that match these filters. Clear them to see everything again.'
          : 'Showing all transactions. Pick dates, a type or a category to narrow the list; leave a box empty to ignore it.'}
      </p>
      <div className="form-row">
        <Field label="From">
          <input type="date" value={filter.from ?? ''} onChange={(e) => onChange({ ...filter, from: e.target.value || undefined })} />
        </Field>
        <Field label="To">
          <input type="date" value={filter.to ?? ''} onChange={(e) => onChange({ ...filter, to: e.target.value || undefined })} />
        </Field>
        <Field label="Type">
          <select value={filter.type ?? ''} onChange={(e) => changeType(e.target.value)}>
            <option value="">All</option>
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expense</option>
          </select>
        </Field>
        <Field label="Category">
          <select
            value={filter.categoryId ?? ''}
            onChange={(e) => onChange({ ...filter, categoryId: e.target.value ? Number(e.target.value) : undefined })}
          >
            <option value="">All</option>
            {categoryOptions.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </Field>
        <button type="button" className="button-secondary" disabled={!isFiltered} onClick={() => onChange({})}>
          Clear filters
        </button>
      </div>
    </section>
  )
}
