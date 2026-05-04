import { API_BASE } from '../../../shared/config.js'

export async function login(credentials) {
  const password = credentials.password ?? ''
  const emailRaw = credentials.email != null ? String(credentials.email).trim() : ''
  const useEmail = emailRaw !== ''

  const body = { password, id: null, email: null }
  if (useEmail) {
    body.email = emailRaw
  } else {
    const id = credentials.id
    if (id == null || id === '') {
      return { success: false, message: 'Please enter account ID or email' }
    }
    body.id = typeof id === 'string' ? Number(id) : id
  }

  const res = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(body),
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) {
    return {
      success: false,
      message: data.message ?? `Request failed (${res.status})`,
    }
  }
  return {
    success: Boolean(data.success),
    message: data.message ?? '',
  }
}
