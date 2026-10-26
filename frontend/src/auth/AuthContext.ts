import { createContext, useContext } from 'react'
import type { Credentials, User } from './types'

export interface AuthState {
  /** True while a valid token is stored. */
  isLoggedIn: boolean
  /** Loaded from /auth/me after login; null until it arrives. */
  user: User | null
  login: (credentials: Credentials) => Promise<void>
  logout: () => void
}

// Kept apart from AuthProvider.tsx: Vite's hot reload only works for files that export just components.
export const AuthContext = createContext<AuthState | null>(null)

/** Auth state for any component below <AuthProvider>. */
export function useAuth(): AuthState {
  const auth = useContext(AuthContext)
  if (!auth) {
    throw new Error('useAuth must be used inside <AuthProvider>')
  }
  return auth
}
