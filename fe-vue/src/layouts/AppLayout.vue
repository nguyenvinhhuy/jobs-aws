<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { RouterView, useRouter } from 'vue-router'

import { setRedirectToLoginHandler } from '../core/http/authRedirect'
import { useI18n } from '../core/i18n/useI18n'
import AppHeader from '../shared/components/common/AppHeader.vue'
import { useAuth } from '../modules/auth/composables/useAuth'

const auth = useAuth()
const router = useRouter()
const { locale } = useI18n()

async function logout() {
  try {
    await auth.logout()
  } finally {
    await router.push('/')
  }
}

setRedirectToLoginHandler(() => {
  const currentPath = window.location.pathname + window.location.search
  // Avoid redirect loop: don't redirect to /profile if already there
  if (window.location.pathname === '/profile') return
  void router.push({ path: '/profile', query: { redirect: currentPath } })
})

onMounted(() => {
  document.documentElement.lang = locale.value
  void auth.refreshSession()
})

watch(locale, (value) => {
  document.documentElement.lang = value
})
</script>

<template>
  <div>
    <AppHeader
      :current-user="auth.isBootstrapping.value ? undefined : auth.currentUser.value"
      :is-bootstrapping="auth.isBootstrapping.value"
      @logout="logout"
    />
    <main>
      <RouterView />
    </main>
  </div>
</template>
