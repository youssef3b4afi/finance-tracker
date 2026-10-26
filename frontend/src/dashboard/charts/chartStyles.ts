// Shared Recharts styling. Colors are CSS variables from index.css, so charts follow light/dark mode
// like the rest of the page (SVG fill/stroke attributes accept var(...)).

/** Tooltip props: page colors, and item text in normal text color rather than the series color. */
export const tooltipStyles = {
  contentStyle: {
    background: 'var(--surface)',
    border: '1px solid var(--border)',
    borderRadius: 6,
  },
  labelStyle: { color: 'var(--text-strong)', fontWeight: 600 },
  itemStyle: { color: 'var(--text)' },
}

/** Axis ticks: small and muted, so the data stands out more than the frame. */
export const axisTick = { fill: 'var(--muted)', fontSize: 12 }
