const STORAGE_KEY = 'beckend.rememberLogin.v1'

export function loadRememberedLogin() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return null
    }
    const o = JSON.parse(raw)
    if (!o || typeof o !== 'object' || typeof o.password !== 'string') {
      return null
    }
    if (o.loginMode !== 'id' && o.loginMode !== 'email') {
      return null
    }
    if (o.loginMode === 'id' && o.id != null && typeof o.id !== 'number' && typeof o.id !== 'string') {
      return null
    }
    if (o.loginMode === 'email' && (typeof o.email !== 'string' || !o.email)) {
      return null
    }
    return o
  } catch {
    return null
  }
}

export function saveRememberedLogin(payload) {
  const { loginMode, password } = payload
  const data = { loginMode, password: String(password) }
  if (loginMode === 'id') {
    const id = payload.id
    data.id = id == null || id === '' ? null : typeof id === 'number' ? id : Number(id)
  } else {
    data.email = String(payload.email ?? '').trim()
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
}

export function clearRememberedLogin() {
  localStorage.removeItem(STORAGE_KEY)
}
