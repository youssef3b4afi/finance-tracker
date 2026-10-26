import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'

/** Shows its children only to a logged-in user; everyone else is sent to /login. */
export default function RequireAuth({ children }: { children: ReactNode }) {
  const { isLoggedIn } = useAuth()
  const location = useLocation()

  if (!isLoggedIn) {
    // Remember where the user was going, so the login page can send them back there.
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  return children
}
