import { useState, type FormEvent, type ReactNode } from 'react'
import { getApiError } from '../api/errors'
import ErrorMessage from '../components/ErrorMessage'
import Field from '../components/Field'
import type { Credentials } from './types'

interface AuthFormProps {
  title: string
  submitLabel: string
  /** True on the register page: tells password managers to suggest a new password. */
  isNewPassword?: boolean
  /** Throws on failure; the form shows the backend's message. */
  onSubmit: (credentials: Credentials) => Promise<void>
  /** Link to the other auth page. */
  footer: ReactNode
}

/** Email + password form shared by the login and register pages. */
export default function AuthForm({ title, submitLabel, isNewPassword = false, onSubmit, footer }: AuthFormProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    // Stop the browser's default full-page form submission; we send the request ourselves.
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    setFieldErrors({})
    try {
      await onSubmit({ email, password })
      // On success the page navigates away, so there is nothing to reset.
    } catch (err) {
      const apiError = getApiError(err)
      setError(apiError.message)
      setFieldErrors(apiError.fieldErrors)
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-card">
      <h1>{title}</h1>
      <form className="form" onSubmit={handleSubmit}>
        <ErrorMessage message={error} />
        <Field label="Email" error={fieldErrors.email}>
          <input
            type="email"
            autoComplete="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </Field>
        <Field label="Password" error={fieldErrors.password}>
          <input
            type="password"
            autoComplete={isNewPassword ? 'new-password' : 'current-password'}
            required
            minLength={isNewPassword ? 8 : undefined}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </Field>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Please wait…' : submitLabel}
        </button>
      </form>
      <p className="muted">{footer}</p>
    </div>
  )
}
