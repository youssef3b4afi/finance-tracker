/** Loading indicator shown while a request is in flight. */
export default function Spinner() {
  return (
    <div className="spinner" role="status">
      <span className="visually-hidden">Loading…</span>
    </div>
  )
}
