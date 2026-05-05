import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Empty, Input, Modal, Space, Table, Typography, message } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { createStory, fetchMyStories } from '../API/novelWorkshopApi.js'
import { AuthRedirectError } from '../../../shared/apiFetch.js'
import './NovelWorkshop.css'

const { Title, Paragraph } = Typography

function formatDate(iso) {
  if (iso == null || iso === '') {
    return '—'
  }
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) {
    return String(iso)
  }
  return d.toLocaleString('zh-CN', { hour12: false })
}

export function NovelWorkshopSection() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [rows, setRows] = useState([])
  const [createOpen, setCreateOpen] = useState(false)
  const [createName, setCreateName] = useState('')
  const [creating, setCreating] = useState(false)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      await Promise.resolve()
      if (cancelled) {
        return
      }
      setLoading(true)
      try {
        const list = await fetchMyStories()
        if (!cancelled) {
          setRows(Array.isArray(list) ? list : [])
        }
      } catch (e) {
        if (e instanceof AuthRedirectError) {
          return
        }
        if (!cancelled) {
          message.error(e?.message ?? '加载失败')
          setRows([])
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  const onCreate = async () => {
    const name = createName.trim()
    if (!name) {
      message.warning('请填写小说名称')
      return
    }
    setCreating(true)
    try {
      const { ok, data } = await createStory(name)
      if (!ok) {
        message.error(data?.message ?? '创建失败')
        return
      }
      message.success(data?.message ?? '已创建')
      setCreateOpen(false)
      setCreateName('')
      if (data?.storyId) {
        navigate(`/novel-workshop/${data.storyId}/edit`, { replace: false })
        return
      }
      const list = await fetchMyStories()
      setRows(Array.isArray(list) ? list : [])
    } catch (e) {
      if (e instanceof AuthRedirectError) {
        return
      }
      message.error('创建失败')
    } finally {
      setCreating(false)
    }
  }

  const columns = [
    { title: '小说 ID', dataIndex: 'storyId', key: 'storyId', ellipsis: true },
    { title: '小说名称', dataIndex: 'storyName', key: 'storyName', ellipsis: true },
    { title: '作者', dataIndex: 'authorName', key: 'authorName', width: 120 },
    { title: '创建时间', dataIndex: 'buildTime', key: 'buildTime', width: 170, render: formatDate },
    { title: '最后修改', dataIndex: 'lastEditTime', key: 'lastEditTime', width: 170, render: formatDate },
    { title: '阅读量', dataIndex: 'readTimes', key: 'readTimes', width: 88 },
  ]

  return (
    <div className="novel-workshop">
      <Title level={4} style={{ marginTop: 0 }}>
        小说车间
      </Title>
      <Paragraph type="secondary" style={{ marginBottom: '1rem' }}>
        展示当前账号下全部小说；点击一行进入编辑。本人浏览列表不增加阅读量。
      </Paragraph>
      <div className="novel-workshop__toolbar">
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
          新建小说
        </Button>
      </div>
      <Table
        rowKey="storyId"
        loading={loading}
        size="middle"
        pagination={false}
        locale={{ emptyText: <Empty description="暂无小说，可先新建一部" /> }}
        dataSource={rows}
        columns={columns}
        onRow={(record) => ({
          onClick: () => navigate(`/novel-workshop/${record.storyId}/edit`),
          style: { cursor: 'pointer' },
        })}
      />
      <Modal
        title="新建小说"
        open={createOpen}
        onCancel={() => {
          setCreateOpen(false)
          setCreateName('')
        }}
        footer={
          <Space>
            <Button onClick={() => setCreateOpen(false)}>取消</Button>
            <Button type="primary" loading={creating} onClick={onCreate}>
              创建
            </Button>
          </Space>
        }
      >
        <Paragraph type="secondary" style={{ marginTop: 0 }}>
          将按规则生成小说 ID，并自动创建第 1 章。
        </Paragraph>
        <Typography.Text strong>名称</Typography.Text>
        <Input
          style={{ marginTop: 8 }}
          placeholder="小说名称"
          value={createName}
          onChange={(e) => setCreateName(e.target.value)}
          maxLength={200}
        />
      </Modal>
    </div>
  )
}
