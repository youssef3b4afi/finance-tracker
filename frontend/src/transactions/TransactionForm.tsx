import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { getApiError } from '../api/errors'
import type { Category } from '../categories/types'
import ErrorMessage from '../components/ErrorMessage'
import Field from '../components/Field'
import { todayIso } from '../dates'
import type { Transaction, TransactionRequest } from './types'

interface TransactionFormProps {
  categories: Category[]
  /** The transaction being edited; leave out to create a new one. */
  transaction?: Transaction
  /** Throws on failure; the form shows the backend's messages. */
  onSubmit: (request: TransactionRequest) => Promise<void>
  onCancel: () => void
}

/**
 * Create / edit form. Inputs hold strings (that's what the browser gives us); they are turned
 * into numbers only on submit. To edit another transaction the parent changes this form's `key`,
 * which makes React build a fresh form instead of keeping the old values.
 */
export default function TransactionForm({ categories, transaction, onSubmit, onCancel }: TransactionFormProps) {
  const [categoryId, setCategoryId] = useState(transaction ? String(transaction.categoryId) : '')
  const [amount, setAmount] = useState(transaction ? String(transaction.amount) : '')
  const [date, setDate] = useState(() => transaction?.date ?? todayIso())
  const [note, setNote] = useState(transaction?.note ?? '')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    setFieldErrors({})
    try {
      // An empty note is sent as null: the backend stores "no note", not "".
      await onSubmit({ categoryId: Number(categoryId), amount: Number(amount), date, note: note.trim() || null })
    } catch (err) {
      const apiError = getApiError(err)
      setError(apiError.message)
      setFieldErrors(apiError.fieldErrors)
      setSubmitting(false)
    }
  }

  if (categories.length === 0) {
    return (
      <p className="card">
        You need a category first. <Link to="/categories">Add one</Link>.
      </p>
    )
  }

  const income = categories.filter((c) => c.type === 'INCOME')
  const expense = categories.filter((c) => c.type === 'EXPENSE')

  return (
    <form className="card form" onSubmit={handleSubmit}>
      <h2>{transaction ? 'Edit transaction' : 'New transaction'}</h2>
      <ErrorMessage message={error} />
      <div className="form-row">
        <Field label="Category" error={fieldErrors.categoryId}>
          <select required value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
            <option value="" disabled>
              Choose…
            </option>
            {/* Grouped, because the category decides whether this is income or an expense. */}
            <optgroup label="Expense">
              {expense.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </optgroup>
            <optgroup label="Income">
              {income.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </optgroup>
          </select>
        </Field>
        <Field label="Amount (EGP)" error={fieldErrors.amount}>
          {/* step 0.01 matches the backend's 2 decimals; inputMode brings up a number pad on phones. */}
          <input
            type="number"
            inputMode="decimal"
            min="0.01"
            step="0.01"
            required
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
          />
        </Field>
        <Field label="Date" error={fieldErrors.date}>
          <input type="date" required value={date} onChange={(e) => setDate(e.target.value)} />
        </Field>
      </div>
      <Field label="Note (optional)" error={fieldErrors.note}>
        <input maxLength={255} value={note} onChange={(e) => setNote(e.target.value)} />
      </Field>
      <div className="row-actions">
        <button type="submit" disabled={submitting}>
          {transaction ? 'Save' : 'Add'}
        </button>
        <button type="button" className="button-secondary" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  )
}
