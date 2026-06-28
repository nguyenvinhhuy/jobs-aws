<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import { useI18n } from '../../../core/i18n/useI18n'
import StatusBanner from '../../../shared/components/common/StatusBanner.vue'
import { authService } from '../services/authService'

const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const errorMessage = ref('')
const noticeMessage = ref('')
const email = ref('')

async function submit() {
  if (!email.value.trim()) return

  loading.value = true
  errorMessage.value = ''
  noticeMessage.value = ''
  try {
    const response = await authService.forgotPassword(email.value.trim())
    noticeMessage.value = response.message
    email.value = ''
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('forgotPassword.sendFailed')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container py-4" style="max-width: 480px;">
    <h3 class="mb-3">{{ t('forgotPassword.title') }}</h3>

    <StatusBanner :loading="loading" :error-message="errorMessage" :notice-message="noticeMessage" />

    <div class="card mb-3">
      <div class="card-body">
        <p class="text-muted mb-3">{{ t('forgotPassword.subtitle') }}</p>
        <div class="mb-3">
          <input
            v-model="email"
            type="email"
            class="form-control"
            :placeholder="t('forgotPassword.emailPlaceholder')"
            @keydown.enter="submit"
          />
        </div>
        <div class="d-flex gap-2">
          <button class="btn btn-primary" :disabled="loading" @click="submit">
            {{ t('forgotPassword.submitButton') }}
          </button>
          <button class="btn btn-outline-secondary" @click="router.push('/profile')">
            {{ t('forgotPassword.backToLogin') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
