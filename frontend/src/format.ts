// Created once and reused: building an Intl formatter is relatively expensive.
// `undefined` locale = the browser's language, so digits and separators look familiar to the user.
const moneyFormat = new Intl.NumberFormat(undefined, { style: 'currency', currency: 'EGP' })
const dateFormat = new Intl.DateTimeFormat(undefined, { day: 'numeric', month: 'short', year: 'numeric' })

/** 1234.5 -> "EGP 1,234.50". For display only; totals always come from the backend. */
export function formatMoney(amount: number): string {
  return moneyFormat.format(amount)
}

/**
 * "2026-10-26" -> "26 Oct 2026". The date is split by hand because new Date("2026-10-26")
 * means midnight UTC, which is still the previous day in time zones west of UTC.
 */
export function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-').map(Number)
  return dateFormat.format(new Date(year, month - 1, day))
}

const monthFormat = new Intl.DateTimeFormat(undefined, { month: 'short', year: 'numeric' })

/** "2026-10" -> "Oct 2026". Built from local parts for the same time-zone reason as formatDate. */
export function formatMonth(yearMonth: string): string {
  const [year, month] = yearMonth.split('-').map(Number)
  return monthFormat.format(new Date(year, month - 1, 1))
}
