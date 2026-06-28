<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useI18n } from '../../../core/i18n/useI18n'
import StatusBanner from '../../../shared/components/common/StatusBanner.vue'
import type { CategorySummary } from '../../../shared/types/api'
import { getCategories } from '../../jobs/api/jobsApi'
import { useAuth } from '../../auth/composables/useAuth'
import { useProfile } from '../composables/useProfile'
import { useEmployer, JOB_RANKS, JOB_TYPES } from '../composables/useEmployer'

const router = useRouter()
const route = useRoute()
const auth = useAuth()
const { t } = useI18n()

const loading = ref(false)
const errorMessage = ref('')
const noticeMessage = ref('')

const categories = ref<CategorySummary[]>([])

const authMode = ref<'login' | 'register'>('login')
const authForm = ref({
  fullName: '',
  address: '',
  email: '',
  phoneNumber: '',
  password: '',
  roleName: 'USER' as 'USER' | 'EMPLOYER',
})
const authErrors = ref({ email: '', password: '', fullName: '' })

const profileComposable = useProfile()
const employerComposable = useEmployer(categories)

const {
  profile,
  savedJobs,
  followedCompanies,
  myApplications,
  profileForm,
  companyForm,
} = profileComposable

const {
  employerJobs,
  employerApplicants,
  editingRecruitmentId,
  recruitmentForm,
} = employerComposable

const isEmployer = computed(() => auth.currentUser.value?.roleName === 'EMPLOYER')

// ── Helpers ────────────────────────────────────────────────────────────────
function handleError(error: unknown, fallback: string) {
  errorMessage.value = error instanceof Error ? error.message : fallback
}

function validateAuthForm(): boolean {
  authErrors.value = { email: '', password: '', fullName: '' }
  let valid = true

  if (authMode.value === 'register' && !authForm.value.fullName.trim()) {
    authErrors.value.fullName = t('profile.validation.fullNameRequired')
    valid = false
  }
  if (!authForm.value.email.trim()) {
    authErrors.value.email = t('profile.validation.emailRequired')
    valid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(authForm.value.email)) {
    authErrors.value.email = t('profile.validation.emailInvalid')
    valid = false
  }
  if (!authForm.value.password) {
    authErrors.value.password = t('profile.validation.passwordRequired')
    valid = false
  } else if (authForm.value.password.length < 5) {
    authErrors.value.password = t('profile.validation.passwordMinLength')
    valid = false
  }

  return valid
}

function resetAuthForm() {
  authForm.value = { fullName: '', address: '', email: '', phoneNumber: '', password: '', roleName: 'USER' }
  authErrors.value = { email: '', password: '', fullName: '' }
}

// ── Data loading ───────────────────────────────────────────────────────────
async function loadAuthenticatedData() {
  await profileComposable.load()
  if (isEmployer.value) {
    await employerComposable.load()
  }
}

async function initializePage() {
  loading.value = true
  errorMessage.value = ''
  try {
    const user = await auth.refreshSession()
    categories.value = await getCategories()
    if (user) {
      await loadAuthenticatedData()
    }
  } catch (error) {
    handleError(error, t('profile.loadProfileError'))
  } finally {
    loading.value = false
  }
}

// ── Auth actions ───────────────────────────────────────────────────────────
async function submitAuth() {
  if (!validateAuthForm()) return

  loading.value = true
  errorMessage.value = ''
  try {
    if (authMode.value === 'login') {
      await auth.login({ email: authForm.value.email, password: authForm.value.password })
      resetAuthForm()
      noticeMessage.value = t('profile.authSuccess')
      await loadAuthenticatedData()
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null
      if (redirect && redirect !== '/profile') {
        await router.push(redirect)
      }
    } else {
      const response = await auth.register(authForm.value)
      noticeMessage.value = response.message
      if (response.verificationUrl) {
        const verificationUrl = new URL(response.verificationUrl)
        await router.push(verificationUrl.pathname + verificationUrl.search)
        return
      }
      resetAuthForm()
      authMode.value = 'login'
    }
  } catch (error) {
    handleError(error, t('profile.authFailed'))
  } finally {
    loading.value = false
  }
}

// ── Profile actions ────────────────────────────────────────────────────────
async function saveProfile() {
  try {
    await profileComposable.saveProfile()
    noticeMessage.value = t('profile.profileUpdated')
  } catch (error) {
    handleError(error, t('profile.updateProfileError'))
  }
}

async function saveCompanyProfile() {
  try {
    await profileComposable.saveCompanyProfile()
    noticeMessage.value = t('profile.companyUpdated')
  } catch (error) {
    handleError(error, t('profile.updateCompanyError'))
  }
}

async function uploadFile(event: Event, type: 'avatar' | 'cv' | 'logo') {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  const validationError = profileComposable.validateFile(file, type)
  if (validationError) {
    errorMessage.value = validationError.type === 'invalidType'
      ? t('profile.validation.fileInvalidType')
      : t('profile.validation.fileTooLarge')
    input.value = ''
    return
  }

  try {
    if (type === 'avatar') {
      await profileComposable.handleAvatarUpload(file)
      noticeMessage.value = t('profile.avatarUploaded')
    } else if (type === 'cv') {
      await profileComposable.handleCvUpload(file)
      noticeMessage.value = t('profile.cvUploaded')
    } else {
      await profileComposable.handleLogoUpload(file)
      noticeMessage.value = t('profile.logoUploaded')
    }
  } catch (error) {
    handleError(error, t('profile.uploadError'))
  } finally {
    input.value = ''
  }
}

async function removeApplication(applyId: number) {
  if (!window.confirm(t('profile.confirmDeleteApplication'))) return
  try {
    await profileComposable.removeApplication(applyId)
    noticeMessage.value = t('profile.applicationDeleted')
  } catch (error) {
    handleError(error, t('profile.deleteApplicationError'))
  }
}

// ── Employer actions ───────────────────────────────────────────────────────
async function createOrUpdateRecruitment() {
  try {
    await employerComposable.saveRecruitment()
    noticeMessage.value = editingRecruitmentId.value
      ? t('profile.recruitmentUpdated')
      : t('profile.recruitmentCreated')
  } catch (error) {
    handleError(error, t('profile.saveRecruitmentError'))
  }
}

async function removeRecruitment(recruitmentId: number) {
  if (!window.confirm(t('profile.confirmDeleteRecruitment'))) return
  try {
    await employerComposable.removeRecruitment(recruitmentId)
    noticeMessage.value = t('profile.recruitmentDeleted')
  } catch (error) {
    handleError(error, t('profile.deleteRecruitmentError'))
  }
}

async function approve(applyId: number) {
  try {
    await employerComposable.approve(applyId)
    noticeMessage.value = t('profile.applicationApproved')
  } catch (error) {
    handleError(error, t('profile.approveError'))
  }
}

onMounted(() => {
  void initializePage()
})
</script>

<template>
  <div class="container py-4">
    <StatusBanner :loading="loading" :error-message="errorMessage" :notice-message="noticeMessage" />

    <div class="row g-4">
      <!-- ── Main column ── -->
      <div class="col-lg-8">
        <h3 class="mb-3">{{ auth.currentUser.value ? t('profile.dashboard') : t('profile.authTitle') }}</h3>

        <!-- ── Guest: login / register form ── -->
        <template v-if="!auth.currentUser.value">
          <ul class="nav nav-tabs mb-3">
            <li class="nav-item">
              <button :class="['nav-link', { active: authMode === 'login' }]" @click="authMode = 'login'">{{ t('profile.loginTab') }}</button>
            </li>
            <li class="nav-item">
              <button :class="['nav-link', { active: authMode === 'register' }]" @click="authMode = 'register'">{{ t('profile.registerTab') }}</button>
            </li>
          </ul>

          <div class="card mb-3">
            <div class="card-body">
              <div class="row g-2">
                <div v-if="authMode === 'register'" class="col-md-6">
                  <input v-model="authForm.fullName" :class="['form-control', { 'is-invalid': authErrors.fullName }]" :placeholder="t('profile.fullName')" />
                  <div v-if="authErrors.fullName" class="invalid-feedback">{{ authErrors.fullName }}</div>
                </div>
                <div v-if="authMode === 'register'" class="col-md-6">
                  <input v-model="authForm.address" class="form-control" :placeholder="t('profile.address')" />
                </div>
                <div class="col-md-6">
                  <input v-model="authForm.email" :class="['form-control', { 'is-invalid': authErrors.email }]" :placeholder="t('profile.email')" />
                  <div v-if="authErrors.email" class="invalid-feedback">{{ authErrors.email }}</div>
                </div>
                <div v-if="authMode === 'register'" class="col-md-6">
                  <input v-model="authForm.phoneNumber" class="form-control" :placeholder="t('profile.phoneNumber')" />
                </div>
                <div class="col-md-6">
                  <input v-model="authForm.password" type="password" :class="['form-control', { 'is-invalid': authErrors.password }]" :placeholder="t('profile.password')" />
                  <div v-if="authErrors.password" class="invalid-feedback">{{ authErrors.password }}</div>
                </div>
                <div v-if="authMode === 'register'" class="col-md-6">
                  <select v-model="authForm.roleName" class="form-select">
                    <option value="USER">{{ t('profile.roleUser') }}</option>
                    <option value="EMPLOYER">{{ t('profile.roleEmployer') }}</option>
                  </select>
                </div>
              </div>
              <div class="d-flex gap-2 flex-wrap mt-3">
                <button class="btn btn-primary" @click="submitAuth">
                  {{ authMode === 'login' ? t('profile.signIn') : t('profile.createAccount') }}
                </button>
                <RouterLink class="btn btn-outline-secondary" to="/verify-email">{{ t('profile.verifyEmail') }}</RouterLink>
                <RouterLink v-if="authMode === 'login'" class="btn btn-link px-0" to="/forgot-password">{{ t('profile.forgotPassword') }}</RouterLink>
              </div>
            </div>
          </div>
        </template>

        <!-- ── Authenticated dashboard ── -->
        <template v-else-if="profile">
          <!-- Profile info -->
          <div class="card mb-3">
            <div class="card-body">
              <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('profile.profileInfo') }}</h6>
              <div v-if="profile.image" class="mb-3">
                <img :src="profile.image" alt="Avatar" class="rounded-circle border" style="width:72px;height:72px;object-fit:cover;" />
              </div>
              <div class="row g-2">
                <div class="col-md-6">
                  <input v-model="profileForm.fullName" class="form-control" :placeholder="t('profile.fullName')" />
                </div>
                <div class="col-md-6">
                  <input v-model="profileForm.address" class="form-control" :placeholder="t('profile.address')" />
                </div>
                <div class="col-md-6">
                  <input v-model="profileForm.email" class="form-control" :placeholder="t('profile.email')" />
                </div>
                <div class="col-md-6">
                  <input v-model="profileForm.phoneNumber" class="form-control" :placeholder="t('profile.phoneNumber')" />
                </div>
                <div class="col-12">
                  <textarea v-model="profileForm.description" class="form-control" rows="4" :placeholder="t('profile.description')"></textarea>
                </div>
              </div>
              <div class="d-flex gap-2 flex-wrap mt-3">
                <button class="btn btn-primary" @click="saveProfile">{{ t('profile.saveProfile') }}</button>
                <label class="btn btn-outline-secondary mb-0">
                  {{ t('common.uploadAvatar') }}
                  <input class="d-none" type="file" accept="image/jpeg,image/png,image/gif,image/webp" @change="uploadFile($event, 'avatar')" />
                </label>
                <label class="btn btn-outline-secondary mb-0">
                  {{ t('common.uploadCv') }}
                  <input class="d-none" type="file" @change="uploadFile($event, 'cv')" />
                </label>
              </div>
              <p class="text-muted small mt-2">{{ t('profile.currentCv') }}: {{ profile.cvFileName || t('profile.noCv') }}</p>
            </div>
          </div>

          <!-- Saved jobs -->
          <div class="card mb-3">
            <div class="card-body">
              <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('profile.savedJobs') }}</h6>
              <div class="d-grid gap-1">
                <button v-for="job in savedJobs" :key="job.id"
                  class="btn btn-outline-secondary text-start btn-sm"
                  @click="router.push(`/jobs/${job.id}`)">
                  {{ job.title }} · {{ job.companyName }}
                </button>
                <p v-if="!savedJobs.length" class="text-muted small mb-0">{{ t('profile.noSavedJobs') }}</p>
              </div>
            </div>
          </div>

          <!-- Applied jobs -->
          <div class="card mb-3">
            <div class="card-body">
              <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('profile.appliedJobs') }}</h6>
              <div v-for="application in myApplications" :key="application.id" class="border rounded p-3 mb-2">
                <strong>{{ application.recruitmentTitle }}</strong>
                <p class="text-muted small mb-2">{{ application.cvFileName }} · {{ application.createdAt }}</p>
                <div class="d-flex gap-2">
                  <button class="btn btn-outline-primary btn-sm" @click="router.push(`/jobs/${application.recruitmentId}`)">{{ t('profile.openJob') }}</button>
                  <button class="btn btn-outline-danger btn-sm" @click="removeApplication(application.id)">{{ t('profile.deleteApplication') }}</button>
                </div>
              </div>
              <p v-if="!myApplications.length" class="text-muted small mb-0">{{ t('profile.noApplications') }}</p>
            </div>
          </div>
        </template>
      </div>

      <!-- ── Sidebar column ── -->
      <div class="col-lg-4">
        <template v-if="auth.currentUser.value && profile">
          <!-- Followed companies -->
          <div class="card mb-3">
            <div class="card-body">
              <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('profile.followedCompanies') }}</h6>
              <div v-for="company in followedCompanies" :key="company.id" class="border rounded p-3 mb-2">
                <strong>{{ company.companyName }}</strong>
                <p class="text-muted small mb-2">{{ company.address }}</p>
                <button class="btn btn-outline-primary btn-sm" @click="router.push(`/companies/${company.id}`)">{{ t('common.details') }}</button>
              </div>
              <p v-if="!followedCompanies.length" class="text-muted small mb-0">{{ t('profile.noFollowedCompanies') }}</p>
            </div>
          </div>

          <!-- Employer: company info -->
          <div v-if="isEmployer" class="card mb-3">
            <div class="card-body">
              <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('profile.companyInfo') }}</h6>
              <div class="row g-2">
                <div class="col-12">
                  <input v-model="companyForm.companyName" class="form-control" :placeholder="t('profile.companyName')" />
                </div>
                <div class="col-12">
                  <input v-model="companyForm.address" class="form-control" :placeholder="t('profile.address')" />
                </div>
                <div class="col-md-6">
                  <input v-model="companyForm.email" class="form-control" :placeholder="t('profile.email')" />
                </div>
                <div class="col-md-6">
                  <input v-model="companyForm.phoneNumber" class="form-control" :placeholder="t('profile.phoneNumber')" />
                </div>
                <div class="col-12">
                  <textarea v-model="companyForm.description" class="form-control" rows="3" :placeholder="t('profile.description')"></textarea>
                </div>
              </div>
              <div class="d-flex gap-2 flex-wrap mt-3">
                <button class="btn btn-primary btn-sm" @click="saveCompanyProfile">{{ t('profile.saveCompany') }}</button>
                <label class="btn btn-outline-secondary btn-sm mb-0">
                  {{ t('common.uploadLogo') }}
                  <input class="d-none" type="file" accept="image/jpeg,image/png,image/gif,image/webp" @change="uploadFile($event, 'logo')" />
                </label>
              </div>
            </div>
          </div>

          <!-- Employer: create/edit recruitment -->
          <div v-if="isEmployer" class="card mb-3">
            <div class="card-body">
              <h6 class="card-title fw-bold border-bottom pb-2 mb-3">
                {{ editingRecruitmentId ? t('profile.editRecruitment') : t('profile.createRecruitment') }}
              </h6>
              <div class="row g-2">
                <div class="col-12">
                  <input v-model="recruitmentForm.title" class="form-control" :placeholder="t('profile.title')" />
                </div>
                <div class="col-12">
                  <input v-model="recruitmentForm.address" class="form-control" :placeholder="t('profile.address')" />
                </div>
                <div class="col-md-6">
                  <input v-model="recruitmentForm.salary" class="form-control" :placeholder="t('profile.salary')" />
                </div>
                <div class="col-md-6">
                  <input v-model="recruitmentForm.experience" class="form-control" :placeholder="t('profile.experience')" />
                </div>
                <div class="col-md-6">
                  <select v-model="recruitmentForm.rank" class="form-select">
                    <option v-for="rank in JOB_RANKS" :key="rank" :value="rank">{{ rank }}</option>
                  </select>
                </div>
                <div class="col-md-6">
                  <select v-model="recruitmentForm.type" class="form-select">
                    <option v-for="jobType in JOB_TYPES" :key="jobType" :value="jobType">{{ jobType }}</option>
                  </select>
                </div>
                <div class="col-md-6">
                  <input v-model="recruitmentForm.deadline" class="form-control" :placeholder="t('profile.deadline')" />
                </div>
                <div class="col-md-6">
                  <input v-model.number="recruitmentForm.quantity" class="form-control" type="number" :placeholder="t('profile.quantity')" />
                </div>
                <div class="col-12">
                  <select v-model.number="recruitmentForm.categoryId" class="form-select">
                    <option v-for="category in categories" :key="category.id" :value="category.id">
                      {{ category.name }}
                    </option>
                  </select>
                </div>
                <div class="col-12">
                  <textarea v-model="recruitmentForm.description" class="form-control" rows="4" :placeholder="t('profile.description')"></textarea>
                </div>
              </div>
              <div class="d-flex gap-2 flex-wrap mt-3">
                <button class="btn btn-primary btn-sm" @click="createOrUpdateRecruitment">
                  {{ editingRecruitmentId ? t('profile.updatePost') : t('profile.createPost') }}
                </button>
                <button v-if="editingRecruitmentId" class="btn btn-outline-secondary btn-sm" @click="employerComposable.resetForm()">{{ t('common.cancelEdit') }}</button>
              </div>
            </div>
          </div>

          <!-- Employer: my recruitments list -->
          <div v-if="isEmployer" class="card mb-3">
            <div class="card-body">
              <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('profile.myRecruitments') }}</h6>
              <div v-for="job in employerJobs" :key="job.id" class="border rounded p-3 mb-2">
                <strong>{{ job.title }}</strong>
                <p class="text-muted small mb-2">{{ job.address }} · {{ job.salary }}</p>
                <div class="d-flex gap-2">
                  <button class="btn btn-outline-primary btn-sm" @click="employerComposable.startEdit(job)">{{ t('profile.edit') }}</button>
                  <button class="btn btn-outline-danger btn-sm" @click="removeRecruitment(job.id)">{{ t('profile.delete') }}</button>
                </div>
              </div>
              <p v-if="!employerJobs.length" class="text-muted small mb-0">{{ t('profile.noRecruitments') }}</p>
            </div>
          </div>

          <!-- Employer: applicants -->
          <div v-if="isEmployer" class="card mb-3">
            <div class="card-body">
              <h6 class="card-title fw-bold border-bottom pb-2 mb-3">{{ t('profile.applicants') }}</h6>
              <div v-for="application in employerApplicants" :key="application.id" class="border rounded p-3 mb-2">
                <strong>{{ application.candidateName }} · {{ application.recruitmentTitle }}</strong>
                <p class="text-muted small mb-2">{{ application.cvFileName }}</p>
                <button class="btn btn-outline-success btn-sm" @click="approve(application.id)">{{ t('profile.approve') }}</button>
              </div>
              <p v-if="!employerApplicants.length" class="text-muted small mb-0">{{ t('profile.noApplicants') }}</p>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
