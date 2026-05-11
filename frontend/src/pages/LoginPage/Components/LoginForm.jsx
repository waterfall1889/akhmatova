import { useEffect } from 'react'
import { Button, Checkbox, Form, Input, InputNumber, Radio, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { emailFormatRule } from '../../../shared/emailPattern.js'
import { login } from '../API/loginApi.js'
import {
  clearRememberedLogin,
  loadRememberedLogin,
  saveRememberedLogin,
} from '../Utils/rememberLogin.js'

export function LoginForm() {
  const [form] = Form.useForm()
  const navigate = useNavigate()

  useEffect(() => {
    const saved = loadRememberedLogin()
    if (!saved) {
      return
    }
    const next = {
      loginMode: saved.loginMode,
      rememberPassword: true,
      password: saved.password,
    }
    if (saved.loginMode === 'id' && saved.id != null) {
      next.id = typeof saved.id === 'number' ? saved.id : Number(saved.id)
    }
    if (saved.loginMode === 'email' && saved.email) {
      next.email = saved.email
    }
    form.setFieldsValue(next)
  }, [form])

  const onFinish = async (values) => {
    const password = values.password ?? ''
    const mode = values.loginMode ?? 'id'
    const remember = Boolean(values.rememberPassword)
    try {
      const result =
        mode === 'email'
          ? await login({ email: values.email, password })
          : await login({ id: values.id, password })
      if (result.success) {
        message.success(result.message)
        if (remember) {
          saveRememberedLogin({
            loginMode: mode,
            id: values.id,
            email: values.email,
            password: values.password,
          })
        } else {
          clearRememberedLogin()
        }
        form.resetFields()
        navigate('/auth', { replace: true })
      } else {
        message.error(result.message)
      }
    } catch {
      message.error('Request failed')
    }
  }

  return (
    <Form
      form={form}
      layout="vertical"
      onFinish={onFinish}
      autoComplete="off"
      initialValues={{ loginMode: 'id', rememberPassword: false }}
    >
      <Form.Item label="Sign-in method" name="loginMode">
        <Radio.Group
          className="login-form__mode-tabs"
          optionType="button"
          buttonStyle="solid"
          onChange={() => {
            form.setFieldsValue({ id: undefined, email: undefined })
          }}
        >
          <Radio.Button value="id">Account ID</Radio.Button>
          <Radio.Button value="email">Email</Radio.Button>
        </Radio.Group>
      </Form.Item>

      <Form.Item noStyle shouldUpdate={(prev, cur) => prev.loginMode !== cur.loginMode}>
        {() =>
          form.getFieldValue('loginMode') === 'email' ? (
            <Form.Item
              label="Email"
              name="email"
              rules={[
                { required: true, message: 'Please enter your email' },
                { type: 'email', message: 'Please enter a valid email' },
                emailFormatRule,
              ]}
            >
              <Input type="email" placeholder="Email" autoComplete="username" style={{ width: '100%' }} />
            </Form.Item>
          ) : (
            <Form.Item
              label="Account ID"
              name="id"
              rules={[{ required: true, message: 'Please enter your account ID' }]}
            >
              <InputNumber
                stringMode
                controls={false}
                placeholder="Account ID"
                style={{ width: '100%' }}
              />
            </Form.Item>
          )
        }
      </Form.Item>

      <Form.Item
        label="Password"
        name="password"
        rules={[{ required: true, message: 'Please enter your password' }]}
      >
        <Input.Password placeholder="Password" autoComplete="current-password" style={{ width: '100%' }} />
      </Form.Item>
      <Form.Item name="rememberPassword" valuePropName="checked">
        <Checkbox>Remember password</Checkbox>
      </Form.Item>
      <Form.Item>
        <Button type="primary" htmlType="submit" block>
          Log in
        </Button>
      </Form.Item>
    </Form>
  )
}
