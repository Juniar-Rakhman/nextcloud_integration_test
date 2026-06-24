import type { SignRequestResult, UploadResult } from '../types'

export async function uploadFile(file: File): Promise<UploadResult> {
  const formData = new FormData()
  formData.append('file', file)
  const res = await fetch('/api/files/upload', {
    method: 'POST',
    body: formData,
  })
  if (!res.ok) throw new Error('Upload failed')
  return res.json()
}

export async function requestSignature(
  file: File,
  signerEmail: string,
  signerDisplayName?: string,
): Promise<SignRequestResult> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('signerEmail', signerEmail)
  if (signerDisplayName) {
    formData.append('signerDisplayName', signerDisplayName)
  }
  const res = await fetch('/api/files/request-signature', {
    method: 'POST',
    body: formData,
  })
  if (!res.ok) throw new Error('Signature request failed')
  return res.json()
}
