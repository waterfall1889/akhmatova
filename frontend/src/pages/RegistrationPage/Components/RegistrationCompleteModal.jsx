import { Button, Modal, Typography, message, Space } from 'antd'
import { useNavigate } from 'react-router-dom'
import './RegistrationCompleteModal.css'

const { Text, Paragraph } = Typography

/**
 * @param {{ open: boolean, userId: number | null, userName?: string | null, onClose: () => void }} props
 */
export function RegistrationCompleteModal({ open, userId, userName, onClose }) {
  const navigate = useNavigate()
  const idText = userId != null ? String(userId) : ''

  const copyId = async () => {
    try {
      await navigator.clipboard.writeText(idText)
      message.success('Copied')
    } catch {
      message.error('Copy failed')
    }
  }

  const onLogin = () => {
    onClose()
    navigate('/login')
  }

  return (
    <Modal
      title="Registration complete"
      open={open}
      onCancel={onClose}
      centered
      destroyOnClose
      footer={
        <Space wrap>
          <Button onClick={onClose}>Close</Button>
          <Button type="primary" onClick={onLogin}>
            Log in
          </Button>
        </Space>
      }
    >
      {userName ? (
        <Paragraph type="secondary" style={{ marginBottom: 12 }}>
          {userName}
        </Paragraph>
      ) : null}
      <div className="registration-complete-modal__id-row">
        <Text strong className="registration-complete-modal__id">
          {idText}
        </Text>
        <Button type="primary" onClick={copyId}>
          Copy ID
        </Button>
      </div>
      <Paragraph type="secondary" style={{ marginTop: 12, marginBottom: 0 }}>
        Save this ID — use it with Account ID or your email to log in.
      </Paragraph>
    </Modal>
  )
}
