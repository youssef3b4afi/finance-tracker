import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import * as authApi from './api'
import { AuthContext, type AuthState } from './AuthContext'
import { clearToken, getExpiresAt, getToken, saveToken } from './tokenStorage'
import type { Credentials, User } from './types'

/** Holds who is logged in and shares it with the whole app through AuthContext. */
export default function AuthProvider({ children }: { children: ReactNode }) {
  // Read once on start-up, so a page reload keeps the user logged in.
  const [token, setToken] = useState(getToken)
  const [user, setUser] = useState<User | null>(null)

  // Whenever the token changes (start-up, login), ask the backend who it belongs to.
  useEffect(() => {
    if (!token) return
    let ignore = false
    authApi
      .me()
      .then((me) => {
        if (!ignore) setUser(me)
      })
      // A 401 is already handled by the client interceptor; anything else just leaves user unknown.
      .catch(() => {})
    // Cleanup: if the token changes again before the answer arrives, drop the stale answer.
    return () => {
      ignore = true
    }
  }, [token])

  // Log out by itself the moment the token expires, so no page keeps showing stale data.
  useEffect(() => {
    const expiresAt = getExpiresAt()
    if (!token || expiresAt === null) return
    const delay = expiresAt - Date.now()
    // setTimeout can't wait longer than ~24.8 days (2^31 - 1 ms); such a token is checked on the next reload.
    if (delay > 2 ** 31 - 1) return
    const timer = setTimeout(() => {
      clearToken()
      setToken(null)
    }, delay)
    return () => clearTimeout(timer)
  }, [token])

  const login = useCallback(async (credentials: Credentials) => {
    const response = await authApi.login(credentials)
    saveToken(response)
    setToken(response.token)
  }, [])

  const logout = useCallback(() => {
    clearToken()
    setToken(null)
    setUser(null)
  }, [])

  // useMemo keeps the same object between renders, so components using the context
  // only re-render when something in it actually changed.
  const value = useMemo<AuthState>(
    () => ({ isLoggedIn: token !== null, user: token ? user : null, login, logout }),
    [token, user, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
