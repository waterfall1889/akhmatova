import { Card, Typography } from 'antd'
import { Link } from 'react-router-dom'
import bgImage from '../../assets/back.png'
import { LoginBrandAside } from './Components/LoginBrandAside.jsx'
import { LoginForm } from './Components/LoginForm.jsx'
import './CSS/LoginPage.css'

const { Title } = Typography

export function LoginPage() {
  return (
    <div className="login-page" style={{ backgroundImage: `url(${bgImage})` }}>
      <div className="login-page__overlay" aria-hidden />
      <div className="login-page__layout">
        <LoginBrandAside />
        <div className="login-page__panel">
          <Card className="login-page__card" bordered={false}>
            <Title level={3} className="login-page__title">
              Log In
            </Title>
            <LoginForm />
            <div className="login-page__footer">
              <Link to="/register">No account? Sign up</Link>
            </div>
          </Card>
        </div>
      </div>
    </div>
  )
}
