import { Card, Typography } from 'antd'
import { Link } from 'react-router-dom'
import bgImage from '../../assets/back.png'
import { RegistrationForm } from './Components/RegistrationForm.jsx'
import '../LoginPage/CSS/LoginPage.css'
import './CSS/RegistrationPage.css'

const { Title } = Typography

export function RegistrationPage() {
  return (
    <div className="login-page login-page--register-solo" style={{ backgroundImage: `url(${bgImage})` }}>
      <div className="login-page__overlay" aria-hidden />
      <div className="login-page__panel login-page__panel--register">
        <Card className="login-page__card" bordered={false}>
          <Title level={3} className="login-page__title">
            Sign up
          </Title>
          <RegistrationForm />
          <div className="login-page__footer">
            <Link to="/login">Already have an account? Log in</Link>
          </div>
        </Card>
      </div>
    </div>
  )
}
