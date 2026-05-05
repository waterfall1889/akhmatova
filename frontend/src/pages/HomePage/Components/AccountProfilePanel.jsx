import { useEffect, useMemo, useState } from 'react'
import { Avatar, Button, Card, Form, Input, Space, Typography, Upload, message } from 'antd'
import { UploadOutlined } from '@ant-design/icons'
import { emailFormatRule } from '../../../shared/emailPattern.js'
import { getAuthAvatarUrl } from '../API/sessionApi.js'
import { updateProfile, uploadAvatarFile } from '../API/profileApi.js'

const { Text } = Typography

function firstDisplayChar(userName) {
  const s = (userName ?? '').trim()
  if (!s) {
    return '?'
  }
  const chars = Array.from(s)
  return chars[0] ?? '?'
}

export function AccountProfilePanel({
  profile,
  onRefreshProfile,
  onLogout,
  avatarBroken,
  onAvatarBroken,
  avatarVersion,
  onAvatarBust,
}) {
  const [form] = Form.useForm()
  const [editing, setEditing] = useState(false)
  const [savingProfile, setSavingProfile] = useState(false)
  const [uploadingAvatar, setUploadingAvatar] = useState(false)

  useEffect(() => {
    form.setFieldsValue({
      userName: profile.userName ?? '',
      userEmail: profile.userEmail ?? '',
    })
  }, [form, profile.id, profile.userName, profile.userEmail])

  const avatarLetter = useMemo(() => firstDisplayChar(profile.userName), [profile.userName])
  const avatarSrc = avatarBroken ? undefined : `${getAuthAvatarUrl()}?v=${avatarVersion}`

  const idLabel = typeof profile.id === 'number' ? profile.id : String(profile.id ?? '—')
  const viewUserName = profile.userName ?? '—'
  const viewUserEmail = profile.userEmail ?? '—'

  const enterEdit = () => {
    form.setFieldsValue({
      userName: profile.userName ?? '',
      userEmail: profile.userEmail ?? '',
    })
    setEditing(true)
  }

  const cancelEdit = () => {
    form.setFieldsValue({
      userName: profile.userName ?? '',
      userEmail: profile.userEmail ?? '',
    })
    setEditing(false)
  }

  const onFinishProfile = async (values) => {
    setSavingProfile(true)
    try {
      const result = await updateProfile({
        userName: values.userName?.trim(),
        userEmail: values.userEmail?.trim(),
      })
      if (result.ok && result.success) {
        message.success(result.message || '已保存')
        await onRefreshProfile()
        setEditing(false)
      } else {
        message.error(result.message || '保存失败')
      }
    } catch {
      message.error('请求失败')
    } finally {
      setSavingProfile(false)
    }
  }

  const beforeUpload = async (file) => {
    const isImage = /^image\//.test(file.type) || /\.(png|jpe?g|gif|webp)$/i.test(file.name)
    if (!isImage) {
      message.error('请上传图片文件（png / jpg / gif / webp）')
      return Upload.LIST_IGNORE
    }
    setUploadingAvatar(true)
    try {
      const result = await uploadAvatarFile(file)
      if (result.ok && result.success) {
        message.success(result.message || '头像已更新')
        onAvatarBroken(false)
        onAvatarBust()
        await onRefreshProfile()
      } else {
        message.error(result.message || '上传失败')
      }
    } catch {
      message.error('上传失败')
    } finally {
      setUploadingAvatar(false)
    }
    return false
  }

  return (
    <Card className="home-account-card" title={editing ? '修改资料' : '账号信息'} bordered={false}>
      <div className="home-account-avatar-wrap">
        <Avatar
          className="home-account-avatar"
          size={120}
          src={avatarSrc}
          onError={() => onAvatarBroken(true)}
        >
          {avatarLetter}
        </Avatar>
      </div>

      {editing ? (
        <>
          <div className="home-account-upload-wrap">
            <Upload accept="image/png,image/jpeg,image/jpg,image/gif,image/webp" showUploadList={false} beforeUpload={beforeUpload}>
              <Button icon={<UploadOutlined />} loading={uploadingAvatar}>
                更换头像
              </Button>
            </Upload>
            <Text type="secondary" className="home-account-upload-hint">
              最大 2MB；无自定义头像时显示用户名首字
            </Text>
          </div>
          <div className="home-account-readonly-row">
            <Text className="home-account-label">ID</Text>
            <Text className="home-account-value">{idLabel}</Text>
          </div>
          <Form form={form} layout="vertical" className="home-account-form" onFinish={onFinishProfile} requiredMark={false}>
            <Form.Item
              label="用户名"
              name="userName"
              rules={[
                { required: true, message: '请输入用户名' },
                { max: 20, message: '用户名不超过 20 个字符' },
              ]}
            >
              <Input placeholder="用户名" autoComplete="nickname" />
            </Form.Item>
            <Form.Item
              label="邮箱"
              name="userEmail"
              rules={[
                { required: true, message: '请输入邮箱' },
                { ...emailFormatRule, message: '邮箱格式不正确' },
              ]}
            >
              <Input type="email" placeholder="邮箱" autoComplete="email" />
            </Form.Item>
            <Form.Item>
              <Space wrap>
                <Button type="primary" htmlType="submit" loading={savingProfile}>
                  保存资料
                </Button>
                <Button onClick={cancelEdit}>取消</Button>
              </Space>
            </Form.Item>
          </Form>
        </>
      ) : (
        <>
          <div className="home-account-view-block">
            <div className="home-account-readonly-row">
              <Text className="home-account-label">ID</Text>
              <Text className="home-account-value">{idLabel}</Text>
            </div>
            <div className="home-account-readonly-row">
              <Text className="home-account-label">用户名</Text>
              <Text className="home-account-value">{viewUserName}</Text>
            </div>
            <div className="home-account-readonly-row">
              <Text className="home-account-label">邮箱</Text>
              <Text className="home-account-value">{viewUserEmail}</Text>
            </div>
          </div>
          <div className="home-account-actions-view">
            <Button type="primary" onClick={enterEdit}>
              修改信息
            </Button>
          </div>
        </>
      )}

      <div className="home-account-footer">
        <Text type="secondary">退出后将需重新登录</Text>
        <Button type="primary" danger onClick={onLogout}>
          退出登录
        </Button>
      </div>
    </Card>
  )
}
