import { apiClient } from '../../../core/http/apiClient'
import type { ApiMessageResponse, UserSession } from '../../../shared/types/api'
import type { LoginPayload, RegisterPayload } from '../../../core/security/auth-context'

export function loginRequest(payload: LoginPayload) {
  return apiClient.post<UserSession>('/api/auth/login', payload).then((response) => response.data)
}

export function registerRequest(payload: RegisterPayload) {
  return apiClient.post<ApiMessageResponse>('/api/auth/register', payload).then((response) => response.data)
}

export function logoutRequest() {
  return apiClient.post<ApiMessageResponse>('/api/auth/logout').then((response) => response.data)
}

export function meRequest() {
  return apiClient.get<UserSession | null>('/api/auth/me').then((response) => {
    const data = response.data
    // Guard: if response is not a proper object (e.g. HTML from a misconfigured proxy),
    // treat it as "no session" so the app never falsely shows a logged-in state.
    if (!data || typeof data !== 'object') return null
    return data
  })
}

export function verifyEmailRequest(token: string) {
  return apiClient
    .post<ApiMessageResponse>('/api/auth/verification/confirm', { token })
    .then((response) => response.data)
}

export function resendVerificationRequest(email: string) {
  return apiClient
    .post<ApiMessageResponse>('/api/auth/verification/resend', { email })
    .then((response) => response.data)
}
