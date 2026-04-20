import { inject } from 'vue'

import { authContextKey } from '../../../core/security/auth-context'

export function useAuth() {
  const context = inject(authContextKey)
  if (!context) {
    throw new Error('Auth provider is not installed')
  }
  return context
}
