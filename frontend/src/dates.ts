/** Two digits: 3 -> "03". */
function pad(n: number): string {
  return String(n).padStart(2, '0')
}

/**
 * Today as "2026-10-26" in the user's own time zone. Not toISOString(): that is in UTC,
 * so shortly after midnight in Cairo it would still say yesterday.
 */
export function todayIso(): string {
  const now = new Date()
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}

/** The current month as "2026-10", in the user's own time zone. */
export function currentMonth(): string {
  return todayIso().slice(0, 7)
}
