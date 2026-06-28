<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useI18n } from '../../../core/i18n/useI18n'
import StatusBanner from '../../../shared/components/common/StatusBanner.vue'
import { authService } from '../services/authService'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const errorMessage = ref('')
const noticeMessage = ref('')
const token = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

onMounted(() => {
  token.value = String(route.query.token ?? '')
  if (!token.value) {
    errorMessage.value = t('resetPassword.tokenMissing')
  }
})

async function submit() {
  errorMessage.value = ''

  if (newPassword.value.length < 6) {
    errorMessage.value = t('resetPassword.passwordMinLength')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    errorMessage.value = t('resetPassword.passwordMismatch')
    return
  }

  loading.value = true
  try {
    const response = await authService.resetPassword(token.value, newPassword.value)
    noticeMessage.value = response.message
    newPassword.value = ''
    confirmPassword.value = ''
    setTimeout(() => router.push('/profile'), 2000)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('resetPassword.resetFailed')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container py-4" style="max-width: 480px;">
    <h3 class="mb-3">{{ t('resetPassword.title') }}</h3>

    <StatusBanner :loading="loading" :error-message="errorMessage" :notice-message="noticeMessage" />

    <div class="card mb-3">
      <div class="card-body">
        <p class="text-muted mb-3">{{ t('resetPassword.subtitle') }}</p>
        <div class="row g-2 mb-3">
          <div class="col-12">
            <input
              v-model="newPassword"
              type="password"
              class="form-control"
              :placeholder="t('resetPassword.newPassword')"
            />
          </div>
          <div class="col-12">
            <input
              v-model="confirmPassword"
              type="password"
              class="form-control"
              :placeholder="t('resetPassword.confirmPassword')"
              @keydown.enter="submit"
            />
          </div>
        </div>
        <div class="d-flex gap-2">
          <button class="btn btn-primary" :disabled="loading || !token" @click="submit">
            {{ t('resetPassword.submitButton') }}
          </button>
          <button class="btn btn-outline-secondary" @click="router.push('/profile')">
            {{ t('resetPassword.goToLogin') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
