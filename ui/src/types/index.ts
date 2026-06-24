export interface AdminConfig {
  nextcloudUrl: string
  clientId: string
  clientSecret: string
  redirectUri: string
}

export interface OAuthStatus {
  connected: boolean
  username: string | null
}

export interface UploadResult {
  success: boolean
  path: string | null
  error: string | null
}

export interface SignRequestResult {
  success: boolean
  uuid: string | null
  nodeId: string | null
  name: string | null
  error: string | null
}

export interface LoginResponse {
  redirectUrl: string
}
