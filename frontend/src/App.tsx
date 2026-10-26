import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import AuthProvider from './auth/AuthProvider'
import LoginPage from './auth/LoginPage'
import RegisterPage from './auth/RegisterPage'
import RequireAuth from './auth/RequireAuth'
import CategoriesPage from './categories/CategoriesPage'
import Layout from './components/Layout'
import DashboardPage from './dashboard/DashboardPage'
import TransactionsPage from './transactions/TransactionsPage'

/** Layout with the logout button wired to auth (Layout itself doesn't know about auth). */
function AppLayout() {
  const { logout } = useAuth()
  return <Layout onLogout={logout} />
}

// Created once, outside the component, so the route table isn't rebuilt on every render.
const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  {
    // A "layout route": no path of its own; its children render inside Layout's <Outlet />.
    element: (
      <RequireAuth>
        <AppLayout />
      </RequireAuth>
    ),
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'transactions', element: <TransactionsPage /> },
      { path: 'categories', element: <CategoriesPage /> },
    ],
  },
  // Unknown URLs go to the dashboard (or on to /login when logged out).
  { path: '*', element: <Navigate to="/" replace /> },
])

function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  )
}

export default App
