const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

let csrfToken = ''

async function getCsrfToken() {
  const response = await fetch(`${API_BASE}/api/auth/csrf`, { credentials: 'include' })
  if (!response.ok) throw new Error('Could not start a secure session. Please try again.')
  const data = await response.json()
  csrfToken = data.token
  return csrfToken
}

export async function apiRequest(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase()
  const headers = { ...(options.headers || {}) }
  if (options.body && !headers['Content-Type']) headers['Content-Type'] = 'application/json'
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    if (!csrfToken) await getCsrfToken()
    headers['X-XSRF-TOKEN'] = csrfToken
  }
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    method,
    headers,
    credentials: 'include'
  })
  if (response.status === 204) return null
  const payload = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(payload.error || payload.message || (response.status === 401 ? 'Please sign in again.' : 'Something went wrong. Please try again.'))
  return payload
}

export async function ensureCsrf() {
  return getCsrfToken()
}
