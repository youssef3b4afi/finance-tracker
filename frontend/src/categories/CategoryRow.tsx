import { useState, type FormEvent } from 'react'
import { getApiError } from '../api/errors'
import type { Category } from './types'

interface CategoryRowProps {
  category: Category
  /** Throws on failure; the row shows the backend's message. */
  onRename: (name: string) => Promise<void>
  onDelete: () => void
}

/** One category in the list; switches to an inline input while being renamed. */
export default function CategoryRow({ category, onRename, onDelete }: CategoryRowProps) {
  const [editing, setEditing] = useState(false)
  const [name, setName] = useState(category.name)
  const [error, setError] = useState<string | null>(null)

  function startEditing() {
    // Start from the current name, not a half-typed one from an earlier cancelled edit.
    setName(category.name)
    setError(null)
    setEditing(true)
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    try {
      await onRename(name)
      setEditing(false)
    } catch (err) {
      setError(getApiError(err).message)
    }
  }

  if (editing) {
    return (
      <li className="list-row">
        <form className="inline-form" onSubmit={handleSubmit}>
          {/* autoFocus: the user clicked Rename, so they want to type straight away. */}
          <input autoFocus required maxLength={50} value={name} onChange={(e) => setName(e.target.value)} />
          <button type="submit" className="button-small">
            Save
          </button>
          <button type="button" className="button-small button-secondary" onClick={() => setEditing(false)}>
            Cancel
          </button>
        </form>
        {error && <span className="field-error">{error}</span>}
      </li>
    )
  }

  return (
    <li className="list-row">
      <span>{category.name}</span>
      <span className="row-actions">
        <button type="button" className="button-small button-secondary" onClick={startEditing}>
          Rename
        </button>
        <button type="button" className="button-small button-danger" onClick={onDelete}>
          Delete
        </button>
      </span>
    </li>
  )
}
