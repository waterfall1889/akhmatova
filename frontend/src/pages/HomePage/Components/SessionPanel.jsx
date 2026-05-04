import { Button, Card, Typography } from 'antd'

const { Title, Text } = Typography

export function SessionPanel({ userId, onLogout }) {
  return (
    <Card className="home-page__card" bordered={false}>
      <Title level={3}>Signed in</Title>
      <Text type="secondary">
        ID: <strong>{userId}</strong>
      </Text>
      <div className="home-page__actions">
        <Button type="primary" onClick={onLogout}>
          Log out
        </Button>
      </div>
    </Card>
  )
}
