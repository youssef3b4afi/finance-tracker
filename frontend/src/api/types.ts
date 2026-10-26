/**
 * Error body for every failed request (RFC 9457 problem detail), built by the backend's
 * GlobalExceptionHandler. `errors` is only present on validation failures: field name -> message.
 */
export interface ProblemDetail {
  type?: string
  title: string
  status: number
  detail: string
  instance?: string
  errors?: Record<string, string>
}

/** Mirrors the backend's common/PageResponse record. `page` is zero-based. */
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
