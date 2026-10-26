import { useState, type FormEvent } from 'react'
import { getApiError } from '../api/errors'
import ErrorMessage from '../components/ErrorMessage'
import Field from '../components/Field'
import type { CategoryRequest, CategoryType } from './types'

interface CategoryFormProps {
  /** Throws on failure; the form shows the backend's message. */
  onCreate: (request: CategoryRequest) => Promise<void>
}

/** "Add category" form: a name and INCOME or EXPENSE. */
export default function CategoryForm({ onCreate }: CategoryFormProps) {
  const [name, setName] = useState('')
  const [type, setType] = useState<CategoryType>('EXPENSE')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    setFieldErrors({})
    try {
      await onCreate({ name, type })
      // Keep the type: people often add several categories of the same kind in a row.
      setName('')
    } catch (err) {
      const apiError = getApiError(err)
      setError(apiError.message)
      setFieldErrors(apiError.fieldErrors)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="card form" onSubmit={handleSubmit}>
      <h2>Add category</h2>
      <ErrorMessage message={error} />
      <div className="form-row">
        <Field label="Name" error={fieldErrors.name}>
          <input required maxLength={50} value={name} onChange={(e) => setName(e.target.value)} />
        </Field>
        <Field label="Type" error={fieldErrors.type}>
          <select value={type} onChange={(e) => setType(e.target.value as CategoryType)}>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>
        </Field>
        <button type="submit" disabled={submitting}>
          Add
        </button>
      </div>
    </form>
  )
}
