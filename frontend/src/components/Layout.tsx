import { NavLink, Outlet } from 'react-router-dom'

interface LayoutProps {
  /** Passed in rather than read from AuthContext here, so this component doesn't depend on auth. */
  onLogout: () => void
}

/** Frame for every logged-in page: nav bar on top, the current route's page below (<Outlet />). */
export default function Layout({ onLogout }: LayoutProps) {
  return (
    <>
      <header className="nav">
        <span className="nav-brand">Finance Tracker</span>
        <nav>
          {/* NavLink adds class "active" to the link of the current page; `end` stops "/" matching every URL. */}
          <NavLink to="/" end>
            Dashboard
          </NavLink>
          <NavLink to="/transactions">Transactions</NavLink>
          <NavLink to="/categories">Categories</NavLink>
        </nav>
        <button type="button" className="button-secondary" onClick={onLogout}>
          Log out
        </button>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </>
  )
}
