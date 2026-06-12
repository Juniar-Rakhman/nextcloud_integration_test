import type { AdminConfig } from '../types'

const BASE = '/api/admin/config'

export async function getConfig(): Promise<AdminConfig> {
  const res = await fetch(BASE)
  if (!res.ok) throw new Error('Failed to fetch config')
  return res.json()
}

export async function saveConfig(config: AdminConfig): Promise<AdminConfig> {
  const res = await fetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config),
  })
  if (!res.ok) throw new Error('Failed to save config')
  return res.json()
}
