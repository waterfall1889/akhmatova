export class AuthRedirectError extends Error {
  constructor() {
    super('redirecting to login')
    this.name = 'AuthRedirectError'
  }
}

/** Sends cookies; on 401 with loginUrl, full-page redirect to login and throws {@link AuthRedirectError}. */
export async function apiFetch(input, init = {}) {
  const headers = new Headers(init.headers ?? {})
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json')
  }
  const res = await fetch(input, {
    ...init,
    credentials: 'include',
    headers,
  })
  if (res.status === 401) {
    const ct = res.headers.get('content-type') ?? ''
    if (ct.includes('application/json')) {
      const data = await res.clone().json().catch(() => null)
      if (data?.loginUrl) {
        window.location.assign(data.loginUrl)
        throw new AuthRedirectError()
      }
    }
  }
  return res
}
