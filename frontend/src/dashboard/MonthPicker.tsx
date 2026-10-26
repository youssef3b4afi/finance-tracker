import Field from '../components/Field'

interface MonthPickerProps {
  /** "yyyy-MM" */
  month: string
  onChange: (month: string) => void
}

/** <input type="month"> already uses "yyyy-MM", the same format as the backend, so no conversion. */
export default function MonthPicker({ month, onChange }: MonthPickerProps) {
  return (
    <Field label="Month">
      <input
        type="month"
        required
        value={month}
        // Clearing the input gives "": keep the last valid month instead of asking the backend for nothing.
        onChange={(e) => e.target.value && onChange(e.target.value)}
      />
    </Field>
  )
}
