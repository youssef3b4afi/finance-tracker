// Mirror the backend's category/dto records and the CategoryType enum.

export type CategoryType = 'INCOME' | 'EXPENSE'

export interface Category {
  id: number
  name: string
  type: CategoryType
}

/** Body of POST /categories. */
export interface CategoryRequest {
  name: string
  type: CategoryType
}
