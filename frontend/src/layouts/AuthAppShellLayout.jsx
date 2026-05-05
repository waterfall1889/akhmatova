import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { BookOutlined, HomeOutlined } from '@ant-design/icons'
import { Spin } from 'antd'
import { fetchCurrentUser } from '../pages/HomePage/API/sessionApi.js'
import { AuthRedirectError } from '../shared/apiFetch.js'
import { AppShellLayout } from './AppShellLayout.jsx'
import './AuthAppShellLayout.css'

const MENU_HOME = 'home'
const MENU_NOVEL = 'novel-workshop'

const PATH_AUTH = '/auth'
const PATH_NOVEL_WORKSHOP = '/novel-workshop'

/**
 * 已登录主站壳：校验会话、侧栏「我的主页 / 小说车间」与路由跳转、右侧 {@link Outlet} 子路由。
 * 业务页（如 {@link HomePage}）只需挂载本组件并在路由 `children` 里写各页内容。
 */
export function AuthAppShellLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const [loading, setLoading] = useState(true)

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
        setLoading(false)
      } catch (e) {
        if (e instanceof AuthRedirectError) {
          return
        }
        if (!cancelled) {
          navigate('/login', { replace: true })
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [navigate])

  const selectedKeys = useMemo(() => {
    if (location.pathname === PATH_NOVEL_WORKSHOP || location.pathname.startsWith(`${PATH_NOVEL_WORKSHOP}/`)) {
      return [MENU_NOVEL]
    }
    return [MENU_HOME]
  }, [location.pathname])

  const menuItems = useMemo(
    () => [
      {
        key: MENU_HOME,
        icon: <HomeOutlined />,
        label: '我的主页',
      },
      {
        key: MENU_NOVEL,
        icon: <BookOutlined />,
        label: '小说车间',
      },
    ],
    [],
  )

  const onMenuClick = ({ key }) => {
    if (key === MENU_HOME) {
      navigate(PATH_AUTH, { replace: false })
      return
    }
    if (key === MENU_NOVEL) {
      navigate(PATH_NOVEL_WORKSHOP, { replace: false })
    }
  }

  if (loading) {
    return (
      <div className="auth-app-shell-loading">
        <Spin size="large" />
      </div>
    )
  }

  return (
    <AppShellLayout menuItems={menuItems} selectedKeys={selectedKeys} onMenuClick={onMenuClick} />
  )
}
