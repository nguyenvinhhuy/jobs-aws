import { ref } from 'vue'
import type { App } from 'vue'

import { authContextKey, type AuthContextValue, type LoginPayload, type RegisterPayload } from '../../core/security/auth-context'
import { authService } from '../../modules/auth/services/authService'
import type { UserSession } from '../../shared/types/api'

function createAuthContext(): AuthContextValue {
  const currentUser = ref<UserSession | null>(null)
  const isBootstrapping = ref(false)

  async function refreshSession() {
    isBootstrapping.value = true
    try {
      currentUser.value = await authService.me()
      return currentUser.value
    } catch {
      currentUser.value = null
      return null
    } finally {
      isBootstrapping.value = false
    }
  }

  async function login(payload: LoginPayload) {
    currentUser.value = await authService.login(payload)
    return currentUser.value
  }

  async function register(payload: RegisterPayload) {
    return authService.register(payload)
  }

  async function logout() {
    try {
      await authService.logout()
    } finally {
      currentUser.value = null
    }
  }

  return {
    currentUser,
    isBootstrapping,
    refreshSession,
    login,
    register,
    logout,
  }
}

export function installAuthProvider(app: App) {
  app.provide(authContextKey, createAuthContext())
}
