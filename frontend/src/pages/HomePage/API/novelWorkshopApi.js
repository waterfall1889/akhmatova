import { API_BASE } from '../../../shared/config.js'
import { apiFetch } from '../../../shared/apiFetch.js'

export async function fetchMyStories() {
  const res = await apiFetch(`${API_BASE}/api/stories`)
  if (!res.ok) {
    throw new Error(`加载小说列表失败: ${res.status}`)
  }
  return res.json()
}

/** @param {string} storyName */
export async function createStory(storyName) {
  const res = await apiFetch(`${API_BASE}/api/stories`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ storyName }),
  })
  const data = await res.json().catch(() => ({}))
  return { ok: res.ok, status: res.status, data }
}

/**
 * @param {string} storyId
 * @param {string} [paragraphId]
 */
export async function fetchStoryEditor(storyId, paragraphId) {
  const q = paragraphId ? `?paragraphId=${encodeURIComponent(paragraphId)}` : ''
  const res = await apiFetch(`${API_BASE}/api/stories/${encodeURIComponent(storyId)}/editor${q}`)
  if (!res.ok) {
    throw new Error(res.status === 404 ? '小说不存在或无权访问' : `加载失败: ${res.status}`)
  }
  return res.json()
}

/**
 * @param {string} storyId
 * @param {string} paragraphId
 * @param {{ paragraphTitle?: string, details?: string }} body
 */
export async function saveParagraph(storyId, paragraphId, body) {
  const res = await apiFetch(
    `${API_BASE}/api/stories/${encodeURIComponent(storyId)}/paragraphs/${encodeURIComponent(paragraphId)}`,
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    },
  )
  const data = await res.json().catch(() => ({}))
  return { ok: res.ok, data }
}

/**
 * 在指定章节之后新建一章（顺位插入）。
 * @param {string} storyId
 * @param {string} afterParagraphId
 */
export async function addParagraphAfter(storyId, afterParagraphId) {
  const res = await apiFetch(`${API_BASE}/api/stories/${encodeURIComponent(storyId)}/paragraphs`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ afterParagraphId }),
  })
  const data = await res.json().catch(() => ({}))
  return { ok: res.ok, data }
}

/**
 * @param {string} storyId
 * @param {string} paragraphId
 */
export async function deleteParagraph(storyId, paragraphId) {
  const res = await apiFetch(
    `${API_BASE}/api/stories/${encodeURIComponent(storyId)}/paragraphs/${encodeURIComponent(paragraphId)}`,
    { method: 'DELETE', headers: { Accept: 'application/json' } },
  )
  const data = await res.json().catch(() => ({}))
  return { ok: res.ok, data }
}
