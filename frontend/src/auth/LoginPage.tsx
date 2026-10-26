import { Link, Navigate, useLocation } from 'react-router-dom'
import AuthForm from './AuthForm'
import { useAuth } from './AuthContext'

export default function LoginPage() {
  const { isLoggedIn, login } = useAuth()
  const location = useLocation()
  // Set by RequireAuth: the page the user wanted before being sent here.
  const from = (location.state as { from?: string } | null)?.from ?? '/'

  // Also covers a successful login: it makes isLoggedIn true, and this re-render navigates away.
  if (isLoggedIn) {
    return <Navigate to={from} replace />
  }

  return (
    <AuthForm
      title="Log in"
      submitLabel="Log in"
      onSubmit={login}
      footer={
        <>
          No account yet? <Link to="/register">Register</Link>
        </>
      }
    />
  )
}
