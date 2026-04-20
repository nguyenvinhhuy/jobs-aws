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
const email = ref('')

async function verifyToken() {
  const token = String(route.query.token ?? '')
  if (!token) {
    errorMessage.value = t('verifyEmail.tokenMissing')
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    const response = await authService.verifyEmail(token)
    noticeMessage.value = response.message
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('verifyEmail.verifyFailed')
  } finally {
    loading.value = false
  }
}

async function resendVerification() {
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await authService.resendVerification(email.value)
    noticeMessage.value = response.message
    if (response.verificationUrl) {
      const verificationUrl = new URL(response.verificationUrl)
      await router.replace(verificationUrl.pathname + verificationUrl.search)
      await verifyToken()
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('verifyEmail.resendFailed')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void verifyToken()
})
</script>

<template>
  <div class="container py-4" style="max-width: 600px;">
    <h3 class="mb-3">{{ t('verifyEmail.title') }}</h3>

    <StatusBanner :loading="loading" :error-message="errorMessage" :notice-message="noticeMessage" />

    <div class="card mb-3">
      <div class="card-body">
        <p class="text-muted mb-3">{{ t('verifyEmail.subtitle') }}</p>
        <button class="btn btn-primary" @click="router.push('/profile')">{{ t('verifyEmail.goToLogin') }}</button>
      </div>
    </div>

    <div class="card mb-3">
      <div class="card-body">
        <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('verifyEmail.resendTitle') }}</h6>
        <div class="row g-2">
          <div class="col-12">
            <input v-model="email" class="form-control" :placeholder="t('profile.email')" />
          </div>
        </div>
        <div class="mt-3">
          <button class="btn btn-outline-primary" @click="resendVerification">{{ t('verifyEmail.resendButton') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>
