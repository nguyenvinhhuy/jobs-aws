<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useI18n } from '../../../core/i18n/useI18n'
import { useAuth } from '../../auth/composables/useAuth'
import { ensureAuthenticated } from '../../auth/utils/authAction'
import StatusBanner from '../../../shared/components/common/StatusBanner.vue'
import type { CompanyDetail } from '../../../shared/types/api'
import { getCompanyDetail } from '../../jobs/api/jobsApi'
import { toggleFollowCompany } from '../../profile/api/memberApi'

const route = useRoute()
const router = useRouter()
const auth = useAuth()
const { t } = useI18n()

const loading = ref(false)
const errorMessage = ref('')
const noticeMessage = ref('')
const company = ref<CompanyDetail | null>(null)

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    company.value = await getCompanyDetail(Number(route.params.id))
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('company.cannotLoad')
  } finally {
    loading.value = false
  }
}

async function followCompany() {
  if (!company.value) return
  if (!ensureAuthenticated(auth, router, () => {
    errorMessage.value = t('common.loginRequired')
  })) {
    return
  }

  try {
    const result = await toggleFollowCompany(company.value.id)
    noticeMessage.value = result.message
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('company.cannotFollow')
  }
}

watch(
  () => route.params.id,
  () => {
    void loadData()
  },
)

onMounted(() => {
  void loadData()
})
</script>

<template>
  <div class="container py-4">
    <StatusBanner :loading="loading" :error-message="errorMessage" :notice-message="noticeMessage" />

    <div v-if="company" class="row g-4">
      <!-- Main -->
      <div class="col-lg-8">
        <button class="btn btn-outline-secondary btn-sm mb-3" @click="router.push('/jobs')">
          ← {{ t('common.openJobBoard') }}
        </button>
        <div class="card mb-3">
          <div class="card-body">
            <h3 class="card-title">{{ company.companyName }}</h3>
            <p class="text-muted mb-2">{{ company.address }}</p>
            <p style="white-space: pre-wrap; line-height: 1.7; color: #495057;" class="mb-3">{{ company.description }}</p>
            <div class="d-flex flex-wrap gap-2 mb-3">
              <span class="badge bg-secondary">{{ company.email }}</span>
              <span class="badge bg-secondary">{{ company.phoneNumber }}</span>
              <span class="badge bg-primary">{{ company.recruitmentCount }} {{ t('company.postings') }}</span>
            </div>
            <button class="btn btn-primary btn-sm" @click="followCompany">{{ t('common.follow') }}</button>
          </div>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="col-lg-4">
        <div class="card mb-3">
          <div class="card-body">
            <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('company.activePosts') }}</h6>
            <div class="d-grid gap-1">
              <button v-for="job in company.recruitments" :key="job.id"
                class="btn btn-outline-secondary text-start btn-sm"
                @click="router.push(`/jobs/${job.id}`)">
                {{ job.title }} · {{ job.address }} · {{ job.salary }}
              </button>
              <p v-if="!company.recruitments.length" class="text-muted small mb-0">{{ t('company.noOpenings') }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
