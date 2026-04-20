import type { LoginPayload, RegisterPayload } from '../../../core/security/auth-context'
import { loginRequest, logoutRequest, meRequest, registerRequest, resendVerificationRequest, verifyEmailRequest } from '../api/authApi'

export const authService = {
  login: (payload: LoginPayload) => loginRequest(payload),
  register: (payload: RegisterPayload) => registerRequest(payload),
  logout: () => logoutRequest(),
  me: () => meRequest(),
  verifyEmail: (token: string) => verifyEmailRequest(token),
  resendVerification: (email: string) => resendVerificationRequest(email),
}
