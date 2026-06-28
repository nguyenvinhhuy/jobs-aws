import type { LoginPayload, RegisterPayload } from '../../../core/security/auth-context'
import { forgotPasswordRequest, loginRequest, logoutRequest, meRequest, registerRequest, resendVerificationRequest, resetPasswordRequest, verifyEmailRequest } from '../api/authApi'

export const authService = {
  login: (payload: LoginPayload) => loginRequest(payload),
  register: (payload: RegisterPayload) => registerRequest(payload),
  logout: () => logoutRequest(),
  me: () => meRequest(),
  verifyEmail: (token: string) => verifyEmailRequest(token),
  resendVerification: (email: string) => resendVerificationRequest(email),
  forgotPassword: (email: string) => forgotPasswordRequest(email),
  resetPassword: (token: string, newPassword: string) => resetPasswordRequest(token, newPassword),
}
