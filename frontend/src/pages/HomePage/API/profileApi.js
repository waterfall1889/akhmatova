import { API_BASE } from '../../../shared/config.js'
import { apiFetch } from '../../../shared/apiFetch.js'

export async function updateProfile({ userName, userEmail }) {
  const res = await apiFetch(`${API_BASE}/api/auth/profile`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ userName, userEmail }),
  })
  const data = await res.json().catch(() => ({}))
  return {
    ok: res.ok,
    success: Boolean(data.success),
    message: data.message ?? '',
  }
}

export async function uploadAvatarFile(file) {
  const form = new FormData()
  form.append('file', file)
  const res = await fetch(`${API_BASE}/api/auth/avatar`, {
    method: 'POST',
    credentials: 'include',
    headers: { Accept: 'application/json' },
    body: form,
  })
  const data = await res.json().catch(() => ({}))
  return {
    ok: res.ok,
    success: Boolean(data.success),
    message: data.message ?? '',
  }
}
