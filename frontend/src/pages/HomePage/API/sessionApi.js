import { API_BASE } from '../../../shared/config.js'
import { apiFetch } from '../../../shared/apiFetch.js'

export async function fetchCurrentUser() {
  const res = await apiFetch(`${API_BASE}/api/auth/me`)
  if (!res.ok) {
    return null
  }
  return res.json()
}

export async function logout() {
  const res = await fetch(`${API_BASE}/api/auth/logout`, {
    method: 'POST',
    credentials: 'include',
    headers: { Accept: 'application/json' },
  })
  const data = await res.json().catch(() => ({}))
  return {
    success: Boolean(data.success),
    message: data.message ?? '',
  }
}
