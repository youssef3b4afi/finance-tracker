import axios from 'axios'
import { clearToken, getToken } from '../auth/tokenStorage'

/**
 * The one axios instance the app uses. Relative baseURL so the Vite dev server
 * proxies /api to the backend (see vite.config.ts).
 */
const client = axios.create({ baseURL: '/api' })

client.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Login and register answer 401 for a wrong password; the form must show that
// message instead of being redirected away.
const AUTH_URLS = ['/auth/login', '/auth/register']

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthRequest = AUTH_URLS.includes(error.config?.url)
    if (error.response?.status === 401 && !isAuthRequest) {
      // The token is missing, expired or invalid: forget it and start over at the login page.
      // window.location, not react-router, because this code runs outside React components.
      clearToken()
      window.location.assign('/login')
    }
    return Promise.reject(error)
  },
)

export default client
