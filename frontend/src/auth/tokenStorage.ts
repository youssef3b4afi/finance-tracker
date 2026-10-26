import type { TokenResponse } from './types'

// The only code that touches the token in localStorage; the axios client and AuthProvider both use it.
const TOKEN_KEY = 'token'
const EXPIRES_AT_KEY = 'tokenExpiresAt'

export function saveToken({ token, expiresAt }: TokenResponse): void {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(EXPIRES_AT_KEY, expiresAt)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(EXPIRES_AT_KEY)
}

/** When the stored token expires, in milliseconds since 1970 (like Date.now()), or null when there is none. */
export function getExpiresAt(): number | null {
  const expiresAt = localStorage.getItem(EXPIRES_AT_KEY)
  return expiresAt ? Date.parse(expiresAt) : null
}

/** The stored token, or null when there is none or it has expired (an expired one is removed). */
export function getToken(): string | null {
  const token = localStorage.getItem(TOKEN_KEY)
  const expiresAt = localStorage.getItem(EXPIRES_AT_KEY)
  if (!token || !expiresAt || Date.parse(expiresAt) <= Date.now()) {
    clearToken()
    return null
  }
  return token
}
