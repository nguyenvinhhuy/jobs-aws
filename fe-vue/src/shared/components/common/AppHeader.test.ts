import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import AppHeader from './AppHeader.vue'

describe('AppHeader', () => {
  it('shows logout button when user exists', () => {
    const wrapper = mount(AppHeader, {
      props: {
        currentUser: {
          id: 1,
          fullName: 'John',
          email: 'john@example.com',
          roleName: 'USER',
          image: null,
          companyId: null,
        },
      },
      global: {
        stubs: {
          RouterLink: {
            template: '<a><slot /></a>',
          },
        },
      },
    })

    expect(wrapper.text()).toContain('Đăng xuất')
    expect(wrapper.find('select').exists()).toBe(true)
  })
})
