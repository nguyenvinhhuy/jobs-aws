import type { Router } from 'vue-router'

import type { AuthContextValue } from '../../../core/security/auth-context'

export function ensureAuthenticated(auth: AuthContextValue, router: Router, onBlocked: () => void) {
  if (auth.currentUser.value) {
    return true
  }

  onBlocked()
  void router.push('/profile')
  return false
}
