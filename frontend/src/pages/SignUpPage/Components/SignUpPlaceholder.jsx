import { Button, Space, Typography } from 'antd'
import { Link } from 'react-router-dom'

const { Title } = Typography

export function SignUpPlaceholder() {
  return (
    <>
      <Title level={3}>More</Title>
      <Space wrap style={{ marginTop: 8 }}>
        <Link to="/register">
          <Button type="primary">Sign up</Button>
        </Link>
        <Link to="/login">
          <Button type="link">Log in</Button>
        </Link>
        <Link to="/auth">
          <Button type="default">Home</Button>
        </Link>
      </Space>
    </>
  )
}
