import client from '../api/client'
import type { Credentials, TokenResponse, User } from './types'

/** Creates the account. Returns the new user, not a token, so the caller logs in afterwards. */
export function register(credentials: Credentials): Promise<User> {
  return client.post<User>('/auth/register', credentials).then((r) => r.data)
}

export function login(credentials: Credentials): Promise<TokenResponse> {
  return client.post<TokenResponse>('/auth/login', credentials).then((r) => r.data)
}

/** The user the current token belongs to. */
export function me(): Promise<User> {
  return client.get<User>('/auth/me').then((r) => r.data)
}
