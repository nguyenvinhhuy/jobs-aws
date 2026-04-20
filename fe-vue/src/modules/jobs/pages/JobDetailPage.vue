<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useI18n } from '../../../core/i18n/useI18n'
import StatusBanner from '../../../shared/components/common/StatusBanner.vue'
import { ensureAuthenticated } from '../../auth/utils/authAction'
import type { RecruitmentDetail } from '../../../shared/types/api'
import { getRecruitmentDetail } from '../api/jobsApi'
import { applyWithExistingCv, applyWithUploadedCv, toggleFollowCompany, toggleSaveJob } from '../../profile/api/memberApi'
import { useAuth } from '../../auth/composables/useAuth'

const route = useRoute()
const router = useRouter()
const auth = useAuth()
const { t } = useI18n()

const loading = ref(false)
const errorMessage = ref('')
const noticeMessage = ref('')
const selectedJob = ref<RecruitmentDetail | null>(null)
const applyText = ref('I am interested in this job opportunity.')

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    selectedJob.value = await getRecruitmentDetail(Number(route.params.id))
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('jobDetail.cannotLoad')
  } finally {
    loading.value = false
  }
}

async function saveJob() {
  if (!selectedJob.value) return
  if (!ensureAuthenticated(auth, router, () => {
    errorMessage.value = t('common.loginRequired')
  })) {
    return
  }

  try {
    const result = await toggleSaveJob(selectedJob.value.id)
    noticeMessage.value = result.message
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('jobDetail.cannotSave')
  }
}

async function followCompany() {
  if (!selectedJob.value) return
  if (!ensureAuthenticated(auth, router, () => {
    errorMessage.value = t('common.loginRequired')
  })) {
    return
  }

  try {
    const result = await toggleFollowCompany(selectedJob.value.companyId)
    noticeMessage.value = result.message
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('jobDetail.cannotFollow')
  }
}

async function applyForJob() {
  if (!selectedJob.value) return
  if (!ensureAuthenticated(auth, router, () => {
    errorMessage.value = t('common.loginRequired')
  })) {
    return
  }

  try {
    const result = await applyWithExistingCv({
      recruitmentId: selectedJob.value.id,
      text: applyText.value,
    })
    noticeMessage.value = result.message
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('jobDetail.cannotApply')
  }
}

async function applyWithFile(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || !selectedJob.value) return
  if (!ensureAuthenticated(auth, router, () => {
    errorMessage.value = t('common.loginRequired')
  })) {
    input.value = ''
    return
  }

  try {
    const result = await applyWithUploadedCv({
      recruitmentId: selectedJob.value.id,
      text: applyText.value,
      file,
    })
    noticeMessage.value = result.message
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('jobDetail.cannotApplyUpload')
  } finally {
    input.value = ''
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

    <div v-if="selectedJob" class="row g-4">
      <!-- Main -->
      <div class="col-lg-8">
        <button class="btn btn-outline-secondary btn-sm mb-3"
          @click="router.push({ path: '/jobs', query: { categoryId: String(selectedJob.categoryId) } })">
          ← {{ t('common.backToJobs') }}
        </button>

        <div class="card mb-3">
          <div class="card-body">
            <h3 class="card-title">{{ selectedJob.title }}</h3>
            <p class="text-muted">{{ selectedJob.companyName }} · {{ selectedJob.address }} · {{ selectedJob.categoryName }}</p>
            <div class="d-flex flex-wrap gap-2 mb-3">
              <span class="badge bg-primary">{{ selectedJob.salary }}</span>
              <span class="badge bg-secondary">{{ selectedJob.type }}</span>
              <span class="badge bg-secondary">{{ selectedJob.rank }}</span>
              <span class="badge bg-secondary">{{ selectedJob.experience }}</span>
            </div>
            <p style="white-space: pre-wrap; line-height: 1.7; color: #495057;">{{ selectedJob.description }}</p>

            <div v-if="auth.currentUser.value" class="border-top pt-3 mt-3">
              <textarea v-model="applyText" class="form-control mb-3" rows="4"></textarea>
              <div class="d-flex gap-2 flex-wrap">
                <button class="btn btn-outline-secondary btn-sm" @click="saveJob">{{ t('common.save') }}</button>
                <button class="btn btn-outline-secondary btn-sm" @click="followCompany">{{ t('common.follow') }}</button>
                <button class="btn btn-primary btn-sm" @click="applyForJob">{{ t('jobDetail.applyCurrentCv') }}</button>
                <label class="btn btn-outline-primary btn-sm mb-0">
                  {{ t('jobDetail.applyNewCv') }}
                  <input class="d-none" type="file" @change="applyWithFile" />
                </label>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="col-lg-4">
        <div class="card mb-3">
          <div class="card-body">
            <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('jobDetail.companyInfo') }}</h6>
            <p class="fw-semibold mb-1">{{ selectedJob.companyName }}</p>
            <p class="text-muted small">{{ selectedJob.companyDescription }}</p>
            <p class="text-muted small mb-1">{{ selectedJob.companyEmail }}</p>
            <p class="text-muted small mb-3">{{ selectedJob.companyPhoneNumber }}</p>
            <button class="btn btn-outline-primary btn-sm w-100"
              @click="router.push(`/companies/${selectedJob.companyId}`)">
              {{ t('common.openCompanyPage') }}
            </button>
          </div>
        </div>

        <div class="card mb-3">
          <div class="card-body">
            <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('jobDetail.postingInfo') }}</h6>
            <p class="text-muted small mb-1">{{ t('jobDetail.deadline') }}: {{ selectedJob.deadline }}</p>
            <p class="text-muted small mb-1">{{ t('jobDetail.views') }}: {{ selectedJob.view }}</p>
            <p class="text-muted small mb-1">{{ t('jobDetail.createdAt') }}: {{ selectedJob.createdAt }}</p>
            <p class="text-muted small mb-0">{{ t('jobDetail.quantity') }}: {{ selectedJob.quantity }}</p>
          </div>
        </div>

        <div v-if="selectedJob.relatedRecruitments.length" class="card mb-3">
          <div class="card-body">
            <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('jobDetail.relatedJobs') }}</h6>
            <div class="d-grid gap-1">
              <button v-for="job in selectedJob.relatedRecruitments" :key="job.id"
                class="btn btn-outline-secondary text-start btn-sm"
                @click="router.push(`/jobs/${job.id}`)">
                {{ job.title }} · {{ job.companyName }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
