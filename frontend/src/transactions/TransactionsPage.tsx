import { useEffect, useState } from 'react'
import { getApiError } from '../api/errors'
import type { PageResponse } from '../api/types'
import { listCategories } from '../categories/api'
import type { Category } from '../categories/types'
import ErrorMessage from '../components/ErrorMessage'
import Spinner from '../components/Spinner'
import { formatDate, formatMoney } from '../format'
import { createTransaction, deleteTransaction, listTransactions, updateTransaction } from './api'
import TransactionFilters from './TransactionFilters'
import TransactionForm from './TransactionForm'
import type { Transaction, TransactionFilter, TransactionRequest } from './types'

const PAGE_SIZE = 20

export default function TransactionsPage() {
  const [categories, setCategories] = useState<Category[] | null>(null)
  const [filter, setFilter] = useState<TransactionFilter>({})
  const [page, setPage] = useState(0)
  const [data, setData] = useState<PageResponse<Transaction> | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)
  // What the form is doing: closed (null), adding ('new') or editing one transaction.
  const [editing, setEditing] = useState<Transaction | 'new' | null>(null)

  // Categories are needed by both the filter bar and the form; load them once.
  useEffect(() => {
    listCategories()
      .then(setCategories)
      .catch((err) => setError(getApiError(err).message))
  }, [])

  // Re-fetch whenever the filter, the page or reloadKey changes.
  useEffect(() => {
    let ignore = false
    listTransactions(filter, page, PAGE_SIZE)
      .then((result) => {
        if (!ignore) setData(result)
      })
      .catch((err) => {
        if (!ignore) setError(getApiError(err).message)
      })
    // Typing a date fires several requests; this drops answers that arrive after a newer request started.
    return () => {
      ignore = true
    }
  }, [filter, page, reloadKey])

  const reload = () => setReloadKey((key) => key + 1)

  function changeFilter(next: TransactionFilter) {
    setFilter(next)
    // Page 3 of the old results means nothing for the new ones.
    setPage(0)
  }

  async function handleSubmit(request: TransactionRequest) {
    if (editing === 'new') {
      await createTransaction(request)
      // New transactions are usually today's, which is the top of page 0 (newest first).
      setPage(0)
    } else if (editing) {
      await updateTransaction(editing.id, request)
    }
    setEditing(null)
    reload()
  }

  async function handleDelete(transaction: Transaction) {
    if (!window.confirm(`Delete this ${formatMoney(transaction.amount)} transaction?`)) return
    setError(null)
    try {
      await deleteTransaction(transaction.id)
      // Deleting the only row on the last page would leave an empty page: step back one.
      if (data?.content.length === 1 && page > 0) {
        setPage(page - 1)
      } else {
        reload()
      }
    } catch (err) {
      setError(getApiError(err).message)
    }
  }

  if (categories === null || data === null) {
    return error ? <ErrorMessage message={error} /> : <Spinner />
  }

  return (
    <>
      <div className="page-header">
        <h1>Transactions</h1>
        {editing === null && <button onClick={() => setEditing('new')}>Add transaction</button>}
      </div>

      {editing !== null && (
        <TransactionForm
          // A new key per transaction makes React start the form fresh instead of keeping the old values.
          key={editing === 'new' ? 'new' : editing.id}
          categories={categories}
          transaction={editing === 'new' ? undefined : editing}
          onSubmit={handleSubmit}
          onCancel={() => setEditing(null)}
        />
      )}

      <TransactionFilters categories={categories} filter={filter} onChange={changeFilter} />
      <ErrorMessage message={error} />

      {data.content.length === 0 ? (
        <p className="muted">No transactions found.</p>
      ) : (
        // On narrow screens the table scrolls sideways inside this box instead of stretching the page.
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Category</th>
                <th>Note</th>
                <th className="number">Amount</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {data.content.map((t) => (
                <tr key={t.id}>
                  <td>{formatDate(t.date)}</td>
                  <td>{t.categoryName}</td>
                  <td className="muted">{t.note}</td>
                  {/* Expenses shown negative: the sign and the color both say which way the money went. */}
                  <td className={`number ${t.type === 'INCOME' ? 'income' : 'expense'}`}>
                    {formatMoney(t.type === 'INCOME' ? t.amount : -t.amount)}
                  </td>
                  <td className="row-actions">
                    <button className="button-small button-secondary" onClick={() => setEditing(t)}>
                      Edit
                    </button>
                    <button className="button-small button-danger" onClick={() => handleDelete(t)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="pager">
        <button className="button-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>
          Previous
        </button>
        <span className="muted">
          Page {page + 1} of {Math.max(data.totalPages, 1)} · {data.totalElements} transactions
        </span>
        <button className="button-secondary" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>
          Next
        </button>
      </div>
    </>
  )
}
