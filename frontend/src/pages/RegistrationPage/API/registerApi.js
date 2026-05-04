import { API_BASE } from '../../../shared/config.js'

export async function register(payload) {
  const res = await fetch(`${API_BASE}/api/auth/register`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({
      userName: payload.userName?.trim() ?? '',
      userEmail: payload.userEmail?.trim() ?? '',
      password: payload.password ?? '',
    }),
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
    id: typeof data.id === 'number' ? data.id : data.id != null ? Number(data.id) : undefined,
  }
}
