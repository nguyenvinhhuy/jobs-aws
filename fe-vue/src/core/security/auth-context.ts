import type { InjectionKey, Ref } from 'vue'

import type { ApiMessageResponse, UserSession } from '../../shared/types/api'

export interface RegisterPayload {
  fullName: string
  address: string
  email: string
  phoneNumber: string
  password: string
  roleName: 'EMPLOYER' | 'USER'
}

export interface LoginPayload {
  email: string
  password: string
}

export interface AuthContextValue {
  currentUser: Ref<UserSession | null>
  isBootstrapping: Ref<boolean>
  refreshSession: () => Promise<UserSession | null>
  login: (payload: LoginPayload) => Promise<UserSession>
  register: (payload: RegisterPayload) => Promise<ApiMessageResponse>
  logout: () => Promise<void>
}

export const authContextKey: InjectionKey<AuthContextValue> = Symbol('auth-context')
