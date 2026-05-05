import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useBlocker, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Button, Card, Input, Menu, Modal, Space, Spin, Tag, Typography, message } from 'antd'
import { ArrowLeftOutlined, DeleteOutlined, PlusOutlined, SaveOutlined } from '@ant-design/icons'
import { addParagraphAfter, deleteParagraph, fetchStoryEditor, saveParagraph } from '../API/novelWorkshopApi.js'
import { AuthRedirectError } from '../../../shared/apiFetch.js'
import './NovelWorkshop.css'

const { Title, Text } = Typography

function formatEditTime(iso) {
  if (iso == null || iso === '') {
    return '—'
  }
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) {
    return String(iso)
  }
  return d.toLocaleString('zh-CN', { hour12: false })
}

export function NovelEditorPage() {
  const { storyId } = useParams()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const paragraphIdFromUrl = searchParams.get('paragraphId') || undefined
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [chapterMutating, setChapterMutating] = useState(false)
  const [editor, setEditor] = useState(null)
  const [chapterTitle, setChapterTitle] = useState('')
  const [details, setDetails] = useState('')
  const [opsGuardOpen, setOpsGuardOpen] = useState(false)
  const [savedSnapshot, setSavedSnapshot] = useState({ title: '', details: '' })

  const dirtyRef = useRef(false)
  const opsRunRef = useRef(null)

  const isDirty = useMemo(() => {
    if (editor == null) {
      return false
    }
    const t = chapterTitle ?? ''
    const b = details ?? ''
    return t !== savedSnapshot.title || b !== savedSnapshot.details
  }, [editor, chapterTitle, details, savedSnapshot])

  useEffect(() => {
    dirtyRef.current = isDirty
  }, [isDirty])

  const shouldBlockNavigation = useCallback(({ currentLocation, nextLocation }) => {
    if (!dirtyRef.current) {
      return false
    }
    const cur = `${currentLocation.pathname}${currentLocation.search}`
    const next = `${nextLocation.pathname}${nextLocation.search}`
    return cur !== next
  }, [])

  const blocker = useBlocker(shouldBlockNavigation)

  useEffect(() => {
    const onBeforeUnload = (e) => {
      if (dirtyRef.current) {
        e.preventDefault()
        e.returnValue = ''
      }
    }
    window.addEventListener('beforeunload', onBeforeUnload)
    return () => window.removeEventListener('beforeunload', onBeforeUnload)
  }, [])

  useEffect(() => {
    if (!storyId) {
      return undefined
    }
    let cancelled = false
    void (async () => {
      await Promise.resolve()
      if (cancelled) {
        return
      }
      setLoading(true)
      try {
        const data = await fetchStoryEditor(storyId, paragraphIdFromUrl)
        if (cancelled) {
          return
        }
        setEditor(data)
        const nt = data.activeParagraphTitle ?? ''
        const nd = data.details ?? ''
        setChapterTitle(nt)
        setDetails(nd)
        setSavedSnapshot({ title: nt, details: nd })
      } catch (e) {
        if (e instanceof AuthRedirectError) {
          return
        }
        if (!cancelled) {
          message.error(e?.message ?? '加载失败')
          navigate('/novel-workshop', { replace: true })
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
  }, [storyId, paragraphIdFromUrl, navigate])

  const persistBaseline = useCallback((title, body) => {
    setSavedSnapshot({ title: title ?? '', details: body ?? '' })
  }, [])

  const performSave = useCallback(async () => {
    if (!storyId || !editor?.activeParagraphId) {
      return false
    }
    const { ok, data } = await saveParagraph(storyId, editor.activeParagraphId, {
      paragraphTitle: chapterTitle,
      details,
    })
    if (!ok) {
      message.error(data?.message ?? '保存失败')
      return false
    }
    message.success(data?.message ?? '已保存')
    try {
      const fresh = await fetchStoryEditor(storyId, paragraphIdFromUrl)
      setEditor(fresh)
      const nt = fresh.activeParagraphTitle ?? ''
      const nd = fresh.details ?? ''
      setChapterTitle(nt)
      setDetails(nd)
      persistBaseline(nt, nd)
    } catch (e) {
      if (!(e instanceof AuthRedirectError)) {
        message.error(e?.message ?? '刷新失败')
      }
      persistBaseline(chapterTitle, details)
    }
    return true
  }, [storyId, editor, chapterTitle, details, paragraphIdFromUrl, persistBaseline])

  const closeRouteGuardModal = useCallback(() => {
    if (blocker.state === 'blocked') {
      blocker.reset()
    }
  }, [blocker])

  const discardRouteAndProceed = useCallback(() => {
    if (blocker.state === 'blocked') {
      blocker.proceed()
    }
  }, [blocker])

  const saveRouteAndProceed = useCallback(async () => {
    setSaving(true)
    try {
      const ok = await performSave()
      if (!ok) {
        return
      }
      if (blocker.state === 'blocked') {
        blocker.proceed()
      }
    } finally {
      setSaving(false)
    }
  }, [performSave, blocker])

  const reloadEditor = async (nextParagraphId) => {
    const pid = nextParagraphId ?? paragraphIdFromUrl
    const data = await fetchStoryEditor(storyId, pid)
    setEditor(data)
    const nt = data.activeParagraphTitle ?? ''
    const nd = data.details ?? ''
    setChapterTitle(nt)
    setDetails(nd)
    persistBaseline(nt, nd)
    if (nextParagraphId != null) {
      setSearchParams({ paragraphId: String(nextParagraphId) })
    }
  }

  const runWhenSavedOrDiscarded = (action) => {
    if (!isDirty) {
      void action()
      return
    }
    opsRunRef.current = action
    setOpsGuardOpen(true)
  }

  const closeOpsGuard = () => {
    setOpsGuardOpen(false)
    opsRunRef.current = null
  }

  const onAddChapterAfter = () => {
    if (!storyId || !editor?.activeParagraphId) {
      return
    }
    runWhenSavedOrDiscarded(async () => {
      setChapterMutating(true)
      try {
        const { ok, data } = await addParagraphAfter(storyId, editor.activeParagraphId)
        if (!ok) {
          message.error(data?.message ?? '新建章节失败')
          return
        }
        message.success(data?.message ?? '已新建章节')
        const newId = data?.paragraphId
        if (newId) {
          await reloadEditor(newId)
        }
      } catch (e) {
        if (!(e instanceof AuthRedirectError)) {
          message.error('新建章节失败')
        }
      } finally {
        setChapterMutating(false)
      }
    })
  }

  const onDeleteChapter = () => {
    if (!storyId || !editor?.activeParagraphId) {
      return
    }
    const pid = editor.activeParagraphId
    runWhenSavedOrDiscarded(() => {
      Modal.confirm({
        title: '确认删除本章？',
        content: '删除后不可恢复，本章正文将一并删除。若全书仅剩一章则无法删除。',
        okText: '删除',
        okType: 'danger',
        cancelText: '取消',
        onOk: async () => {
          setChapterMutating(true)
          try {
            const { ok, data } = await deleteParagraph(storyId, pid)
            if (!ok) {
              message.error(data?.message ?? '删除失败')
              return
            }
            message.success(data?.message ?? '已删除')
            const fresh = await fetchStoryEditor(storyId)
            setEditor(fresh)
            const nt = fresh.activeParagraphTitle ?? ''
            const nd = fresh.details ?? ''
            setChapterTitle(nt)
            setDetails(nd)
            persistBaseline(nt, nd)
            setSearchParams({ paragraphId: String(fresh.activeParagraphId) })
          } catch (e) {
            if (!(e instanceof AuthRedirectError)) {
              message.error('删除失败')
            }
          } finally {
            setChapterMutating(false)
          }
        },
      })
    })
  }

  const onSave = async () => {
    if (!storyId || !editor?.activeParagraphId) {
      return
    }
    setSaving(true)
    try {
      await performSave()
    } finally {
      setSaving(false)
    }
  }

  const goBackToList = () => {
    navigate('/novel-workshop')
  }

  const onSelectChapter = ({ key }) => {
    if (!key || key === paragraphIdFromUrl) {
      return
    }
    setSearchParams({ paragraphId: key })
  }

  if (loading || !editor) {
    return (
      <div className="novel-editor-page" style={{ justifyContent: 'center', alignItems: 'center', display: 'flex', minHeight: 320 }}>
        <Spin size="large" />
      </div>
    )
  }

  const menuItems = (editor.paragraphs ?? []).map((p) => {
    const ord = typeof p.index === 'number' ? p.index : ''
    const title = p.paragraphTitle || p.paragraphId
    return {
      key: p.paragraphId,
      label: ord !== '' ? `${ord}. ${title}` : title,
    }
  })

  const selectedKey = paragraphIdFromUrl ?? editor.activeParagraphId

  return (
    <div className="novel-editor-page">
      <div className="novel-editor-page__layout">
        <aside className="novel-editor-page__sider">
          <div className="novel-editor-page__sider-inner">
            <Button type="text" icon={<ArrowLeftOutlined />} onClick={goBackToList} style={{ alignSelf: 'flex-start', paddingInline: 8 }}>
              返回小说列表
            </Button>
            <Text type="secondary" style={{ fontSize: 12, marginTop: 4 }}>
              章节目录（切换章节时若有未保存内容将提示）
            </Text>
            <div className="novel-editor-page__sider-scroll">
              <Menu mode="inline" selectedKeys={[selectedKey]} items={menuItems} onClick={onSelectChapter} />
            </div>
          </div>
        </aside>
        <main className="novel-editor-page__body">
          <div className="novel-editor-page__toolbar">
            <div className="novel-editor-page__toolbar-main">
              <Space align="center" wrap size={10}>
                <Title level={4} style={{ margin: 0, fontWeight: 600 }}>
                  {editor.storyName}
                </Title>
                {isDirty ? (
                  <Tag color="warning">未保存</Tag>
                ) : (
                  <Tag color="success" style={{ margin: 0 }}>
                    已保存
                  </Tag>
                )}
              </Space>
              <div className="novel-editor-page__meta">
                小说 ID：{editor.storyId} · 本书最近保存：{formatEditTime(editor.lastEditTime)}
              </div>
            </div>
            <div className="novel-editor-page__toolbar-actions">
              <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={onSave}>
                保存本章
              </Button>
              <Button icon={<PlusOutlined />} loading={chapterMutating} onClick={onAddChapterAfter}>
                在本章后新建
              </Button>
              <Button danger icon={<DeleteOutlined />} loading={chapterMutating} onClick={onDeleteChapter}>
                删除本章
              </Button>
            </div>
          </div>

          <div className="novel-editor-page__scroll">
            <Card bordered={false} size="small" styles={{ body: { padding: 0 } }}>
              <div className="novel-editor-page__title-field">
                <Text type="secondary" style={{ display: 'block', marginBottom: 6 }}>
                  章节标题
                </Text>
                <Input value={chapterTitle} onChange={(e) => setChapterTitle(e.target.value)} maxLength={200} placeholder="本章标题" allowClear />
              </div>
              <div className="novel-editor-page__editor-wrap">
                <Text type="secondary" style={{ display: 'block', marginBottom: 6 }}>
                  正文（可拖动右下角调整编辑区高度）
                </Text>
                <Input.TextArea
                  className="novel-editor__textarea"
                  value={details}
                  onChange={(e) => setDetails(e.target.value)}
                  placeholder="在此撰写正文……"
                  autoSize={{ minRows: 20, maxRows: 64 }}
                />
              </div>
            </Card>
          </div>
        </main>
      </div>

      <Modal
        title="离开当前章节？"
        open={blocker.state === 'blocked'}
        closable={false}
        maskClosable={false}
        onCancel={closeRouteGuardModal}
        footer={
          <Space wrap style={{ width: '100%', justifyContent: 'flex-end' }}>
            <Button onClick={closeRouteGuardModal}>留在本页</Button>
            <Button danger onClick={discardRouteAndProceed}>
              不保存，离开
            </Button>
            <Button type="primary" loading={saving} onClick={saveRouteAndProceed}>
              保存并离开
            </Button>
          </Space>
        }
      >
        <p style={{ marginBottom: 0 }}>当前章节有未保存的修改。可保存后再跳转、直接放弃修改，或留在本页继续编辑。</p>
      </Modal>

      <Modal
        title="当前章节有未保存修改"
        open={opsGuardOpen}
        closable={false}
        maskClosable={false}
        onCancel={closeOpsGuard}
        footer={
          <Space wrap style={{ width: '100%', justifyContent: 'flex-end' }}>
            <Button onClick={closeOpsGuard}>取消</Button>
            <Button
              danger
              onClick={() => {
                const fn = opsRunRef.current
                closeOpsGuard()
                if (fn) {
                  void fn()
                }
              }}
            >
              不保存，继续
            </Button>
            <Button
              type="primary"
              loading={saving}
              onClick={async () => {
                const fn = opsRunRef.current
                setSaving(true)
                try {
                  const ok = await performSave()
                  if (!ok) {
                    return
                  }
                  closeOpsGuard()
                  if (fn) {
                    void fn()
                  }
                } finally {
                  setSaving(false)
                }
              }}
            >
              保存并继续
            </Button>
          </Space>
        }
      >
        <p style={{ marginBottom: 0 }}>是否先保存本章，再执行刚才的操作？</p>
      </Modal>
    </div>
  )
}
