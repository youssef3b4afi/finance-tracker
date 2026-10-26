import client from '../api/client'
import type { PageResponse } from '../api/types'
import type { Transaction, TransactionFilter, TransactionRequest } from './types'

/** One page of transactions, newest first. `page` is zero-based. */
export function listTransactions(filter: TransactionFilter, page: number, size: number): Promise<PageResponse<Transaction>> {
  // Leave out empty filters entirely, so the URL only says what is actually being filtered
  // (axios skips undefined values but would send an empty string as "from=").
  const params = Object.fromEntries(Object.entries({ ...filter, page, size }).filter(([, value]) => value !== undefined && value !== ''))
  return client.get<PageResponse<Transaction>>('/transactions', { params }).then((r) => r.data)
}

export function getTransaction(id: number): Promise<Transaction> {
  return client.get<Transaction>(`/transactions/${id}`).then((r) => r.data)
}

export function createTransaction(request: TransactionRequest): Promise<Transaction> {
  return client.post<Transaction>('/transactions', request).then((r) => r.data)
}

export function updateTransaction(id: number, request: TransactionRequest): Promise<Transaction> {
  return client.put<Transaction>(`/transactions/${id}`, request).then((r) => r.data)
}

export function deleteTransaction(id: number): Promise<void> {
  return client.delete(`/transactions/${id}`).then(() => undefined)
}
