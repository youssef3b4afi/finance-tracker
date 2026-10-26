import { useEffect, useState } from 'react'
import { getApiError } from '../api/errors'
import ErrorMessage from '../components/ErrorMessage'
import Spinner from '../components/Spinner'
import { createCategory, deleteCategory, listCategories, renameCategory } from './api'
import CategoryForm from './CategoryForm'
import CategoryRow from './CategoryRow'
import type { Category, CategoryRequest, CategoryType } from './types'

const SECTIONS: { type: CategoryType; title: string }[] = [
  { type: 'INCOME', title: 'Income' },
  { type: 'EXPENSE', title: 'Expenses' },
]

export default function CategoriesPage() {
  const [categories, setCategories] = useState<Category[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  // Bumping this number re-runs the loading effect: a simple way to say "fetch again".
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let ignore = false
    listCategories()
      .then((data) => {
        if (!ignore) setCategories(data)
      })
      .catch((err) => {
        if (!ignore) setError(getApiError(err).message)
      })
    return () => {
      ignore = true
    }
  }, [reloadKey])

  // After every change we reload the list instead of patching it locally:
  // one extra request, but the screen always shows exactly what the server stored.
  const reload = () => setReloadKey((key) => key + 1)

  async function handleCreate(request: CategoryRequest) {
    await createCategory(request)
    reload()
  }

  async function handleRename(id: number, name: string) {
    await renameCategory(id, name)
    reload()
  }

  async function handleDelete(category: Category) {
    if (!window.confirm(`Delete the category "${category.name}"?`)) return
    setError(null)
    try {
      await deleteCategory(category.id)
      reload()
    } catch (err) {
      // e.g. 409 "Category 'Food' still has transactions", shown as the backend wrote it.
      setError(getApiError(err).message)
    }
  }

  return (
    <>
      <h1>Categories</h1>
      <CategoryForm onCreate={handleCreate} />
      <ErrorMessage message={error} />
      {categories === null ? (
        !error && <Spinner />
      ) : (
        <div className="grid-2">
          {SECTIONS.map(({ type, title }) => (
            <section key={type} className="card">
              <h2 className={type === 'INCOME' ? 'income' : 'expense'}>{title}</h2>
              <ul className="list">
                {categories
                  .filter((category) => category.type === type)
                  .map((category) => (
                    <CategoryRow
                      key={category.id}
                      category={category}
                      onRename={(name) => handleRename(category.id, name)}
                      onDelete={() => handleDelete(category)}
                    />
                  ))}
              </ul>
            </section>
          ))}
        </div>
      )}
    </>
  )
}
