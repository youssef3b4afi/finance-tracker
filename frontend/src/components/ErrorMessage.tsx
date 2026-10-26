interface ErrorMessageProps {
  message: string | null
}

/** Red error box; renders nothing when there is no message, so callers can always include it. */
export default function ErrorMessage({ message }: ErrorMessageProps) {
  if (!message) return null
  // role="alert" makes screen readers announce the error as soon as it appears.
  return (
    <p className="error-message" role="alert">
      {message}
    </p>
  )
}
