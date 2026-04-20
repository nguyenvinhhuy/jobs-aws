let redirectToLoginHandler: (() => void) | null = null

export function setRedirectToLoginHandler(handler: () => void) {
  redirectToLoginHandler = handler
}

export function redirectToLogin() {
  redirectToLoginHandler?.()
}
