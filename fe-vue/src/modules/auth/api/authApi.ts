import { apiClient } from '../../../core/http/apiClient'
import type { ApiMessageResponse, UserSession } from '../../../shared/types/api'
import type { LoginPayload, RegisterPayload } from '../../../core/security/auth-context'

export async function loginRequest(payload: LoginPayload) {
  const response = await apiClient.post<UserSession>('/api/auth/login', payload)
  return response.data
}

export async function registerRequest(payload: RegisterPayload) {
  const response = await apiClient.post<ApiMessageResponse>('/api/auth/register', payload)
  return response.data
}

export async function logoutRequest() {
  const response = await apiClient.post<ApiMessageResponse>('/api/auth/logout')
  return response.data
}

export async function meRequest() {
  const response = await apiClient.get<UserSession | null>('/api/auth/me')
  const data = response.data
  // Guard: if response is not a proper object (e.g. HTML from a misconfigured proxy),
  // treat it as "no session" so the app never falsely shows a logged-in state.
  if (!data || typeof data !== 'object') return null
  return data
}

export async function verifyEmailRequest(token: string) {
  const response = await apiClient.post<ApiMessageResponse>('/api/auth/verification/confirm', { token })
  return response.data
}

export async function resendVerificationRequest(email: string) {
  const response = await apiClient.post<ApiMessageResponse>('/api/auth/verification/resend', { email })
  return response.data
}

export async function forgotPasswordRequest(email: string) {
  const response = await apiClient.post<ApiMessageResponse>('/api/auth/password/forgot', { email })
  return response.data
}

export async function resetPasswordRequest(token: string, newPassword: string) {
  const response = await apiClient.post<ApiMessageResponse>('/api/auth/password/reset', { token, newPassword })
  return response.data
}
