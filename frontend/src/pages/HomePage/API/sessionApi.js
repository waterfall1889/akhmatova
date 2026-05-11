import { API_BASE } from '../../../shared/config.js'
import { apiFetch } from '../../../shared/apiFetch.js'

/** 当前登录用户自定义头像（GET，带 Cookie）；无 Mongo 数据时后端 404。 */
export function getAuthAvatarUrl() {
  const base = API_BASE ?? ''
  if (!base) {
    return '/api/auth/avatar'
  }
  return `${base}/api/auth/avatar`
}

/** 将 /api/auth/me 的 JSON 规范为 camelCase（兼容 snake_case 等）。 */
export function normalizeSessionUser(data) {
  if (!data || typeof data !== 'object') {
    return null
  }
  const rawId = data.id
  if (rawId == null) {
    return null
  }
  const id = typeof rawId === 'number' ? rawId : Number(rawId)
  if (Number.isNaN(id)) {
    return null
  }
  const userName = data.userName ?? data.user_name
  const userEmail = data.userEmail ?? data.user_email
  return {
    id,
    userName: userName != null && String(userName).trim() !== '' ? String(userName) : null,
    userEmail: userEmail != null && String(userEmail).trim() !== '' ? String(userEmail) : null,
  }
}

export async function fetchCurrentUser() {
  const res = await apiFetch(`${API_BASE}/api/auth/me`)
  if (!res.ok) {
    return null
  }
  const body = await res.json()
  return normalizeSessionUser(body)
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
