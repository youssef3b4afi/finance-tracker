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

  function changeType(value: string) {
    const type = (value || undefined) as CategoryType | undefined
    const category = categories.find((c) => c.id === filter.categoryId)
    // Drop a selected category of the other type; together they would always match nothing.
    const categoryId = type && category && category.type !== type ? undefined : filter.categoryId
    onChange({ ...filter, type, categoryId })
  }

  return (
    <div className="card form-row">
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
      <button type="button" className="button-secondary" onClick={() => onChange({})}>
        Clear
      </button>
    </div>
  )
}
