import { useState } from 'react'
import { Button, Form, Input, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { emailFormatRule } from '../../../shared/emailPattern.js'
import { register } from '../API/registerApi.js'
import { RegistrationCompleteModal } from './RegistrationCompleteModal.jsx'

const USER_NAME_MAX = 20
const PASSWORD_MIN = 6

export function RegistrationForm() {
  const [form] = Form.useForm()
  const navigate = useNavigate()
  const [completeOpen, setCompleteOpen] = useState(false)
  const [assigned, setAssigned] = useState({ id: null, userName: null })

  const onFinish = async (values) => {
    try {
      const result = await register({
        userName: values.userName,
        userEmail: values.userEmail,
        password: values.password,
      })
      if (result.success) {
        if (result.id == null) {
          message.success(result.message)
          form.resetFields()
          navigate('/login', { replace: true })
          return
        }
        form.resetFields()
        setAssigned({ id: result.id, userName: values.userName ?? null })
        setCompleteOpen(true)
      } else {
        message.error(result.message)
      }
    } catch {
      message.error('Request failed')
    }
  }

  return (
    <>
      <Form form={form} layout="vertical" onFinish={onFinish} autoComplete="off">
        <Form.Item
          label="Username"
          name="userName"
          rules={[
            { required: true, message: 'Please enter a username' },
            { max: USER_NAME_MAX, message: `At most ${USER_NAME_MAX} characters` },
          ]}
        >
          <Input maxLength={USER_NAME_MAX} showCount placeholder="Username" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item
          label="Email"
          name="userEmail"
          rules={[
            { required: true, message: 'Please enter your email' },
            { type: 'email', message: 'Please enter a valid email' },
            emailFormatRule,
          ]}
        >
          <Input type="email" placeholder="Email" autoComplete="email" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item
          label="Password"
          name="password"
          rules={[
            { required: true, message: 'Please enter a password' },
            { min: PASSWORD_MIN, message: `At least ${PASSWORD_MIN} characters` },
          ]}
        >
          <Input.Password placeholder="Password" autoComplete="new-password" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item
          label="Confirm password"
          name="confirmPassword"
          dependencies={['password']}
          rules={[
            { required: true, message: 'Please confirm your password' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('password') === value) {
                  return Promise.resolve()
                }
                return Promise.reject(new Error('Passwords do not match'))
              },
            }),
          ]}
        >
          <Input.Password placeholder="Confirm password" autoComplete="new-password" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" block>
            Sign up
          </Button>
        </Form.Item>
      </Form>
      <RegistrationCompleteModal
        open={completeOpen}
        userId={assigned.id}
        userName={assigned.userName}
        onClose={() => setCompleteOpen(false)}
      />
    </>
  )
}
