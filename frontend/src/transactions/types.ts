import type { CategoryType } from '../categories/types'

// Mirror the backend's transaction/dto records.

export interface Transaction {
  id: number
  /** BigDecimal arrives as a JSON number (2000.00 arrives as 2000); format for display only. */
  amount: number
  /** ISO date, "2026-10-26". */
  date: string
  note: string | null
  categoryId: number
  categoryName: string
  /** Taken from the category: a transaction has no type of its own. */
  type: CategoryType
}

/** Body of POST and PUT /transactions. */
export interface TransactionRequest {
  categoryId: number
  amount: number
  date: string
  note: string | null
}

/** Optional list filters; a missing field means "don't filter on this". */
export interface TransactionFilter {
  from?: string
  to?: string
  categoryId?: number
  type?: CategoryType
}
