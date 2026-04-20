import { describe, expect, it } from 'vitest'

import { appRoutes } from './index'

describe('appRoutes', () => {
  it('registers email verification route', () => {
    expect(appRoutes.some((route) => route.path === '/verify-email')).toBe(true)
  })
})
