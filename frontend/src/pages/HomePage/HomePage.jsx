import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Spin, message } from 'antd'
import { AuthAppShellLayout } from '../../layouts/index.js'
import { AuthRedirectError } from '../../shared/apiFetch.js'
import { fetchCurrentUser, logout } from './API/sessionApi.js'
import { AccountProfilePanel } from './Components/AccountProfilePanel.jsx'
import './HomePage.css'

/**
 * 已登录区路由根节点：布局见 {@link AuthAppShellLayout}；
 * 个人主页 `/auth` 内容由 {@link AccountProfilePanel} 展示。
 */
export function HomePage() {
  return <AuthAppShellLayout />
}

/** `/auth`：资料与头像编辑、退出登录 */
export function MyHomeSection() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [profile, setProfile] = useState(null)
  const [avatarBroken, setAvatarBroken] = useState(false)
  const [avatarVersion, setAvatarVersion] = useState(0)

  const reloadProfile = useCallback(async () => {
    const user = await fetchCurrentUser()
    if (user?.id) {
      setAvatarBroken(false)
      setProfile(user)
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const user = await fetchCurrentUser()
        if (cancelled) {
          return
        }
        if (!user?.id) {
          navigate('/login', { replace: true })
          return
        }
        if (!cancelled) {
          setAvatarBroken(false)
          setProfile(user)
        }
      } catch (e) {
        if (e instanceof AuthRedirectError) {
          return
        }
        if (!cancelled) {
          navigate('/login', { replace: true })
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [navigate])

  const onLogout = useCallback(async () => {
    const result = await logout()
    if (result.success) {
      message.success(result.message || '已退出')
    }
    navigate('/login', { replace: true })
  }, [navigate])

  if (loading) {
    return (
      <div className="home-account-loading">
        <Spin size="large" />
      </div>
    )
  }

  if (!profile) {
    return null
  }

  return (
    <div className="home-account-page">
      <AccountProfilePanel
        profile={profile}
        onRefreshProfile={reloadProfile}
        onLogout={onLogout}
        avatarBroken={avatarBroken}
        onAvatarBroken={setAvatarBroken}
        avatarVersion={avatarVersion}
        onAvatarBust={() => setAvatarVersion((v) => v + 1)}
      />
    </div>
  )
}
