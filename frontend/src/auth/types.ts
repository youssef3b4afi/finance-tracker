// Mirror the backend's auth/dto records, field for field.

/** Body of POST /auth/login and POST /auth/register. */
export interface Credentials {
  email: string
  password: string
}

/** Returned by login. `expiresAt` is an ISO-8601 instant, e.g. "2026-10-27T12:00:00Z". */
export interface TokenResponse {
  token: string
  expiresAt: string
}

/** Returned by register and /auth/me. */
export interface User {
  id: number
  email: string
}
