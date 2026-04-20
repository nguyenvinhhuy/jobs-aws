<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useI18n } from '../../../core/i18n/useI18n'
import StatusBanner from '../../../shared/components/common/StatusBanner.vue'
import type { CompanySummary } from '../../../shared/types/api'
import { getCompanies } from '../../jobs/api/jobsApi'

const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const errorMessage = ref('')
const keyword = ref('')
const companies = ref<CompanySummary[]>([])

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    companies.value = await getCompanies({ keyword: keyword.value || undefined, limit: 24 })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('company.cannotLoad')
  } finally {
    loading.value = false
  }
}

function search() {
  void loadData()
}

onMounted(() => {
  void loadData()
})
</script>

<template>
  <div>
    <StatusBanner :loading="loading" :error-message="errorMessage" :notice-message="''" />

    <div class="hero-wrap-2" style="background: linear-gradient(rgba(0,0,0,0.55), rgba(0,0,0,0.55)), linear-gradient(135deg, #1a2a4a 0%, #0f3460 100%);">
      <div class="overlay"></div>
      <div class="container pb-4">
        <h1 class="bread">{{ t('common.companies') }}</h1>
      </div>
    </div>

    <div class="container py-4">
      <div class="d-flex gap-2 mb-4">
        <input
          v-model="keyword"
          class="form-control"
          :placeholder="t('home.searchCompanyPlaceholder')"
          @keyup.enter="search"
        />
        <button class="btn btn-primary" @click="search">{{ t('common.search') }}</button>
      </div>

      <div class="row g-3">
        <div
          v-for="company in companies"
          :key="company.id"
          class="col-md-4 col-lg-3"
        >
          <div class="card h-100 company-card-hover" style="cursor: pointer;" @click="router.push(`/companies/${company.id}`)">
            <div class="card-body d-flex flex-column gap-2">
              <div class="d-flex align-items-center gap-3">
                <div class="rounded bg-light d-flex align-items-center justify-content-center" style="width:48px;height:48px;flex-shrink:0;">
                  <img v-if="company.logo" :src="company.logo" :alt="company.companyName" class="img-fluid rounded" style="max-width:48px;max-height:48px;object-fit:contain;" />
                  <span v-else class="fw-bold text-primary fs-5">{{ company.companyName[0] }}</span>
                </div>
                <div>
                  <h6 class="mb-0 fw-bold">{{ company.companyName }}</h6>
                </div>
              </div>
              <p class="text-muted small mb-0">{{ company.address }}</p>
              <p class="text-muted small mb-0">{{ company.recruitmentCount }} {{ t('company.postings') }}</p>
              <button class="btn btn-outline-primary btn-sm mt-auto" @click.stop="router.push(`/companies/${company.id}`)">
                {{ t('common.details') }}
              </button>
            </div>
          </div>
        </div>
        <div v-if="!companies.length && !loading" class="col-12">
          <p class="text-muted text-center py-4">{{ t('company.noCompanies') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.company-card-hover:hover {
  box-shadow: 0 6px 18px rgba(0,0,0,.1);
  transform: translateY(-2px);
  transition: box-shadow 0.2s, transform 0.15s;
}
</style>
