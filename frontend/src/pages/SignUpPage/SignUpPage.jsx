import { Card } from 'antd'
import { SignUpPlaceholder } from './Components/SignUpPlaceholder.jsx'
import './CSS/SignUpPage.css'

export function SignUpPage() {
  return (
    <div className="sign-in-page">
      <Card className="sign-in-page__card" bordered={false}>
        <SignUpPlaceholder />
      </Card>
    </div>
  )
}
