import axios from 'axios'
import type { ProblemDetail } from './types'

export interface ApiError {
  /** Message to show the user, e.g. "Category 'Food' still has transactions". */
  message: string
  /** Per-field validation messages from a 400, keyed by the request field name. */
  fieldErrors: Record<string, string>
}

/** Turns anything thrown by an API call into a message (and field errors) the UI can show. */
export function getApiError(error: unknown): ApiError {
  if (axios.isAxiosError<ProblemDetail>(error)) {
    if (!error.response) {
      return { message: 'Cannot reach the server. Check your connection and try again.', fieldErrors: {} }
    }
    const problem = error.response.data
    // A proxy or crash can answer with HTML instead of a problem detail, so check before trusting it.
    if (problem && typeof problem === 'object' && problem.detail) {
      return { message: problem.detail, fieldErrors: problem.errors ?? {} }
    }
  }
  return { message: 'Something went wrong. Please try again.', fieldErrors: {} }
}
