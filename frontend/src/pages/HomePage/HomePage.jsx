import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Spin } from 'antd'
import { AuthRedirectError } from '../../shared/apiFetch.js'
import { fetchCurrentUser, logout } from './API/sessionApi.js'
import { SessionPanel } from './Components/SessionPanel.jsx'
import './CSS/HomePage.css'

export function HomePage() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [userId, setUserId] = useState(null)

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
        setUserId(user.id)
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

  const onLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  if (loading) {
    return (
      <div className="home-page">
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div className="home-page">
      <SessionPanel userId={userId} onLogout={onLogout} />
    </div>
  )
}
