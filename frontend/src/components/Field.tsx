import type { ReactNode } from 'react'

interface FieldProps {
  label: string
  /** Backend validation message for this input, shown under it. */
  error?: string
  /** The input, select or textarea. */
  children: ReactNode
}

/** A labelled form control. Wrapping the input in <label> links the two without needing ids. */
export default function Field({ label, error, children }: FieldProps) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
      {error && <span className="field-error">{error}</span>}
    </label>
  )
}
