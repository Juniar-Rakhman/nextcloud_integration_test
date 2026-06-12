import type { OAuthStatus, LoginResponse } from '../types'

export async function getStatus(): Promise<OAuthStatus> {
  const res = await fetch('/api/oauth/status')
  if (!res.ok) throw new Error('Failed to fetch status')
  return res.json()
}

export async function getLoginUrl(): Promise<LoginResponse> {
  const res = await fetch('/api/oauth/login')
  if (!res.ok) throw new Error('Failed to get login URL')
  return res.json()
}

export async function disconnect(): Promise<void> {
  const res = await fetch('/api/oauth/disconnect', { method: 'DELETE' })
  if (!res.ok) throw new Error('Failed to disconnect')
}
