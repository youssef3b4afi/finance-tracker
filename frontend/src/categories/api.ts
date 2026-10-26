import client from '../api/client'
import type { Category, CategoryRequest } from './types'

export function listCategories(): Promise<Category[]> {
  return client.get<Category[]>('/categories').then((r) => r.data)
}

export function createCategory(request: CategoryRequest): Promise<Category> {
  return client.post<Category>('/categories', request).then((r) => r.data)
}

/** Only the name can change; the type is fixed once transactions may depend on it. */
export function renameCategory(id: number, name: string): Promise<Category> {
  return client.put<Category>(`/categories/${id}`, { name }).then((r) => r.data)
}

/** Fails with 409 while the category still has transactions. */
export function deleteCategory(id: number): Promise<void> {
  return client.delete(`/categories/${id}`).then(() => undefined)
}
