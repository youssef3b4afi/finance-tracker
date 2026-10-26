import { Link, Navigate } from 'react-router-dom'
import { register } from './api'
import AuthForm from './AuthForm'
import { useAuth } from './AuthContext'
import type { Credentials } from './types'

export default function RegisterPage() {
  const { isLoggedIn, login } = useAuth()

  if (isLoggedIn) {
    return <Navigate to="/" replace />
  }

  // Register returns the new user but no token, so log in with the same credentials straight after.
  async function registerAndLogin(credentials: Credentials) {
    await register(credentials)
    await login(credentials)
  }

  return (
    <AuthForm
      title="Create an account"
      submitLabel="Register"
      isNewPassword
      onSubmit={registerAndLogin}
      footer={
        <>
          Already registered? <Link to="/login">Log in</Link>
        </>
      }
    />
  )
}
