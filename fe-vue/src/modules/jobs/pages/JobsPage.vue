<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useI18n } from '../../../core/i18n/useI18n'
import StatusBanner from '../../../shared/components/common/StatusBanner.vue'
import { useAuth } from '../../auth/composables/useAuth'
import { ensureAuthenticated } from '../../auth/utils/authAction'
import type { CategorySummary, PageResponse, RecruitmentSummary } from '../../../shared/types/api'
import { getCategories, getRecruitments } from '../api/jobsApi'
import { toggleSaveJob } from '../../profile/api/memberApi'

const route = useRoute()
const router = useRouter()
const auth = useAuth()
const { t } = useI18n()

// ── Static filter options ──────────────────────────────────────────────────
const LOCATIONS = ['Da Nang', 'Hue', 'Ha Noi', 'Ho Chi Minh City', 'Quy Nhon']
const JOB_TYPES = ['Fulltime', 'Parttime', 'Hybrid', 'Remote', 'Contract', 'Internship']

// ── State ──────────────────────────────────────────────────────────────────
const loading = ref(false)
const errorMessage = ref('')
const noticeMessage = ref('')
const categories = ref<CategorySummary[]>([])
const jobs = ref<PageResponse<RecruitmentSummary> | null>(null)

// "Find Job" / "Find Candidate" tab — Vue-controlled (no Bootstrap JS needed)
const searchTab = ref<'job' | 'candidate'>('job')

const keyword = ref(String(route.query.keyword ?? ''))
const activeCategoryId = ref<number | null>(route.query.categoryId ? Number(route.query.categoryId) : null)
const activeAddress = ref<string | null>((route.query.address as string) || null)
const activeJobType = ref<string | null>((route.query.jobType as string) || null)

const page = computed(() => Number(route.query.page ?? 0))

// ── Data loading ───────────────────────────────────────────────────────────
async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [loadedCategories, loadedJobs] = await Promise.all([
      getCategories(),
      getRecruitments({
        page: page.value,
        size: 6,
        keyword: keyword.value || undefined,
        categoryId: activeCategoryId.value,
        address: activeAddress.value || undefined,
        jobType: activeJobType.value || undefined,
      }),
    ])
    categories.value = loadedCategories
    jobs.value = loadedJobs
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('jobsPage.cannotLoad')
  } finally {
    loading.value = false
  }
}

function buildQuery(nextPage = 0) {
  return {
    ...(keyword.value        ? { keyword:    keyword.value }                    : {}),
    ...(activeCategoryId.value ? { categoryId: String(activeCategoryId.value) } : {}),
    ...(activeAddress.value  ? { address:    activeAddress.value }              : {}),
    ...(activeJobType.value  ? { jobType:    activeJobType.value }              : {}),
    page: String(nextPage),
  }
}

function applyFilters() {
  void router.push({ path: '/jobs', query: buildQuery(0) })
}

function changePage(nextPage: number) {
  void router.push({ path: '/jobs', query: buildQuery(nextPage) })
}

/** Toggle a single-select sidebar filter (checkbox acting as radio button) */
function toggleLocation(loc: string) {
  activeAddress.value = activeAddress.value === loc ? null : loc
  applyFilters()
}

function toggleJobType(jt: string) {
  activeJobType.value = activeJobType.value === jt ? null : jt
  applyFilters()
}

function toggleCategory(catId: number) {
  activeCategoryId.value = activeCategoryId.value === catId ? null : catId
  applyFilters()
}

async function saveJob(jobId: number) {
  if (!ensureAuthenticated(auth, router, () => {
    errorMessage.value = t('common.loginRequired')
  })) {
    return
  }
  try {
    const result = await toggleSaveJob(jobId)
    noticeMessage.value = result.message
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('jobsPage.cannotSave')
  }
}

watch(
  () => route.fullPath,
  () => {
    keyword.value          = String(route.query.keyword  ?? '')
    activeCategoryId.value = route.query.categoryId ? Number(route.query.categoryId) : null
    activeAddress.value    = (route.query.address  as string)  || null
    activeJobType.value    = (route.query.jobType  as string)  || null
    void loadData()
  },
)

onMounted(() => {
  void loadData()
})
</script>

<template>
  <div class="page-stack">
    <!-- Hero Banner -->
    <div class="hero-wrap hero-wrap-2" style="background: linear-gradient(rgba(0,0,0,0.55), rgba(0,0,0,0.55)), linear-gradient(135deg, #1a2a4a 0%, #0f3460 100%); background-attachment: fixed;">
      <div class="overlay"></div>
      <div class="container">
        <div class="row no-gutters slider-text align-items-end justify-content-start">
          <div class="col-md-12 text-center mb-5">
            <p class="breadcrumbs mb-0">
              <span class="mr-3"><a href="/" style="cursor: pointer;">{{ t('common.home') }} <i class="ion-ios-arrow-forward"></i></a></span>
              <span>{{ t('jobsPage.title') }}</span>
            </p>
            <h1 class="mb-3 bread">{{ t('jobsPage.browse') }}</h1>
          </div>
        </div>
      </div>
    </div>

    <StatusBanner :loading="loading" :error-message="errorMessage" :notice-message="noticeMessage" />

    <!-- Search Section -->
    <section class="ftco-section">
      <div class="container">
        <div class="row justify-content-center mb-5">
          <div class="col-md-7 heading-section text-center">
            <span class="subheading">{{ t('jobsPage.categories') }}</span>
            <h2 class="mb-0">{{ t('jobsPage.topCategories') }}</h2>
          </div>
        </div>
      </div>
    </section>

    <!-- Advanced Search -->
    <section class="ftco-section ftco-no-pb bg-light">
      <div class="container">
        <div class="row justify-content-center mb-4">
          <div class="col-md-7 text-center heading-section">
            <span class="subheading">{{ t('jobsPage.browse') }}</span>
            <h2 class="mb-4">{{ t('jobsPage.advancedSearch') }}</h2>
          </div>
        </div>
        <div class="row">
          <div class="ftco-search">
            <div class="row">
              <!-- Vue-controlled tabs — no Bootstrap JS / jQuery needed -->
              <div class="col-md-12 nav-link-wrap">
                <div class="nav nav-pills text-center">
                  <button :class="['nav-link', { active: searchTab === 'job' }]" @click="searchTab = 'job'">{{ t('jobsPage.findJob') }}</button>
                  <button :class="['nav-link', { active: searchTab === 'candidate' }]" @click="searchTab = 'candidate'">{{ t('jobsPage.findCandidate') }}</button>
                </div>
              </div>
              <div class="col-md-12 tab-wrap">
                <div class="tab-content p-4">
                  <!-- Find a Job tab -->
                  <div v-show="searchTab === 'job'" role="tabpanel">
                    <form class="search-job">
                      <div class="row no-gutters">
                        <div class="col-md mr-md-2">
                          <div class="form-group">
                            <div class="form-field">
                              <div class="icon"><span class="icon-briefcase"></span></div>
                              <input v-model="keyword" type="text" class="form-control" :placeholder="t('jobsPage.searchJobPlaceholder')" @keyup.enter="applyFilters">
                            </div>
                          </div>
                        </div>
                        <div class="col-md mr-md-2">
                          <div class="form-group">
                            <div class="form-field">
                              <div class="select-wrap">
                                <div class="icon"><span class="ion-ios-arrow-down"></span></div>
                                <select v-model="activeCategoryId" class="form-control">
                                  <option :value="null">{{ t('common.allCategories') }}</option>
                                  <option v-for="category in categories" :key="category.id" :value="category.id">
                                    {{ category.name }}
                                  </option>
                                </select>
                              </div>
                            </div>
                          </div>
                        </div>
                        <div class="col-md">
                          <div class="form-group">
                            <div class="form-field">
                              <button type="button" @click="applyFilters" class="form-control btn btn-primary">{{ t('common.search') }}</button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </form>
                  </div>

                  <!-- Find a Candidate tab -->
                  <div v-show="searchTab === 'candidate'" role="tabpanel">
                    <form class="search-job" @submit.prevent="applyFilters">
                      <div class="row">
                        <div class="col-md">
                          <div class="form-group">
                            <div class="form-field">
                              <div class="icon"><span class="icon-user"></span></div>
                              <input v-model="keyword" type="text" class="form-control" :placeholder="t('jobsPage.candidatePlaceholder')">
                            </div>
                          </div>
                        </div>
                        <div class="col-md">
                          <div class="form-group">
                            <div class="form-field">
                              <div class="select-wrap">
                                <div class="icon"><span class="ion-ios-arrow-down"></span></div>
                                <select v-model="activeCategoryId" class="form-control">
                                  <option :value="null">{{ t('common.allCategories') }}</option>
                                  <option v-for="category in categories" :key="category.id" :value="category.id">
                                    {{ category.name }}
                                  </option>
                                </select>
                              </div>
                            </div>
                          </div>
                        </div>
                        <div class="col-md">
                          <div class="form-group">
                            <div class="form-field">
                              <button type="submit" class="form-control btn btn-primary">{{ t('common.search') }}</button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </form>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Job List with Sidebar -->
    <section v-if="jobs" class="ftco-section bg-light">
      <div class="container">
        <div class="row">
          <div class="col-lg-9 pr-lg-4">
            <div class="row">
              <div v-for="job in jobs.content" :key="job.id" class="col-md-12">
                <div class="job-post-item p-4 d-block d-lg-flex align-items-center">
                  <div class="one-third mb-4 mb-md-0">
                    <div class="job-post-item-header align-items-center">
                      <span class="subadge">{{ job.type }}</span>
                      <h2 class="mr-3 text-black">
                        <a @click="router.push(`/jobs/${job.id}`)" style="cursor: pointer;">{{ job.title }}</a>
                      </h2>
                    </div>
                    <div class="job-post-item-body d-block d-md-flex">
                      <div class="mr-3">
                        <span class="icon-layers"></span>
                        <a @click="router.push(`/companies/${job.companyId}`)" style="cursor: pointer;">{{ job.companyName }}</a>
                      </div>
                      <div><span class="icon-my_location"></span><span>{{ job.address }}</span></div>
                    </div>
                  </div>

                  <div class="one-forth ml-auto d-flex align-items-center mt-4 md-md-0">
                    <div>
                      <a @click="saveJob(job.id)" style="cursor: pointer;" class="icon text-center d-flex justify-content-center align-items-center icon mr-2">
                        <span class="icon-heart"></span>
                      </a>
                    </div>
                    <button @click="router.push(`/jobs/${job.id}`)" class="btn btn-primary py-2">{{ t('common.applyJob') }}</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Pagination -->
            <div class="row mt-5">
              <div class="col text-center">
                <div class="block-27">
                  <ul>
                    <li :class="{ disabled: page === 0 }">
                      <a @click="page > 0 && changePage(page - 1)" style="cursor: pointer;">&lt;</a>
                    </li>
                    <li v-for="i in Math.min(5, jobs.totalPages)" :key="i" :class="{ active: page === i - 1 }">
                      <a @click="changePage(i - 1)" style="cursor: pointer;">{{ i }}</a>
                    </li>
                    <li :class="{ disabled: page + 1 >= jobs.totalPages }">
                      <a @click="page + 1 < jobs.totalPages && changePage(page + 1)" style="cursor: pointer;">&gt;</a>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          <!-- Sidebar -->
          <div class="col-lg-3 sidebar">
            <!-- Category Filter -->
            <div class="sidebar-box bg-white p-4">
              <h3 class="heading-sidebar">{{ t('jobsPage.browseCategory') }}</h3>
              <form class="browse-form">
                <label v-for="category in categories" :key="category.id">
                  <input type="checkbox"
                    :checked="activeCategoryId === category.id"
                    @change="toggleCategory(category.id)">
                  {{ category.name }}
                  <span class="text-muted small">({{ category.numberChoose }})</span>
                </label>
              </form>
            </div>

            <!-- Location Filter -->
            <div class="sidebar-box bg-white p-4">
              <h3 class="heading-sidebar">{{ t('jobsPage.selectLocation') }}</h3>
              <form class="browse-form">
                <label v-for="loc in LOCATIONS" :key="loc">
                  <input type="checkbox"
                    :checked="activeAddress === loc"
                    @change="toggleLocation(loc)">
                  {{ loc }}
                </label>
              </form>
            </div>

            <!-- Job Type Filter -->
            <div class="sidebar-box bg-white p-4">
              <h3 class="heading-sidebar">{{ t('jobsPage.jobType') }}</h3>
              <form class="browse-form">
                <label v-for="jt in JOB_TYPES" :key="jt">
                  <input type="checkbox"
                    :checked="activeJobType === jt"
                    @change="toggleJobType(jt)">
                  {{ jt }}
                </label>
              </form>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Newsletter Section -->
    <section class="ftco-section-parallax">
      <div class="parallax-img d-flex align-items-center">
        <div class="container">
          <div class="row d-flex justify-content-center">
            <div class="col-md-7 text-center heading-section heading-section-white">
              <h2>{{ t('jobsPage.newsletter') }}</h2>
              <p>{{ t('jobsPage.newsletterDesc') }}</p>
              <div class="row d-flex justify-content-center mt-4 mb-4">
                <div class="col-md-12">
                  <form class="subscribe-form">
                    <div class="form-group d-flex">
                      <input type="text" class="form-control" :placeholder="t('jobsPage.emailPlaceholder')">
                      <input type="submit" :value="t('jobsPage.subscribe')" class="submit px-3">
                    </div>
                  </form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.hero-wrap-2 {
  background-size: cover;
  background-position: center;
  position: relative;
  min-height: 300px;
  display: flex;
  align-items: flex-end;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.breadcrumbs {
  color: white;
  font-size: 14px;
}

.breadcrumbs a {
  color: white;
  text-decoration: none;
}

.bread {
  color: white;
  font-size: 42px;
}

.ftco-section {
  padding: 60px 0;
}

.ftco-section.ftco-no-pb {
  padding-bottom: 0;
}

.ftco-section.bg-light {
  background: #f8f9fa;
}

.heading-section {
  margin-bottom: 40px;
}

.subheading {
  color: #999;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.nav-link {
  margin-right: 10px !important;
  padding: 12px 24px;
  background: white;
  border: 1px solid #ddd;
  cursor: pointer;
}

.nav-link.active {
  background: #007bff;
  color: white;
}

.form-field {
  position: relative;
  display: flex;
  align-items: center;
}

.form-field .icon {
  position: absolute;
  left: 15px;
  color: #999;
}

.form-field input,
.form-field select {
  padding-left: 45px;
}

.job-post-item {
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 20px;
}

.job-post-item-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.subadge {
  display: inline-block;
  background: #007bff;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 10px;
}

.job-post-item-body {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.one-third {
  flex: 1;
}

.one-forth {
  flex: 0.5;
}

.ml-auto {
  margin-left: auto;
}

.sidebar {
  margin-top: 20px;
}

.sidebar-box {
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 20px;
}

.heading-sidebar {
  font-size: 18px;
  margin-bottom: 15px;
}

.browse-form label {
  display: block;
  margin-bottom: 10px;
  cursor: pointer;
}

.block-27 ul {
  list-style: none;
  padding: 0;
  display: flex;
  gap: 5px;
  justify-content: center;
}

.block-27 li {
  border: 1px solid #ddd;
  padding: 8px 12px;
}

.block-27 li.active {
  background: #007bff;
  color: white;
}

.block-27 li a {
  cursor: pointer;
}

.ftco-section-parallax {
  background: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)),
              linear-gradient(135deg, #1a2a4a 0%, #0f3460 100%);
  background-size: cover;
  background-attachment: fixed;
  padding: 80px 0;
}

.parallax-img {
  min-height: 400px;
}

.heading-section-white {
  color: white;
}

.heading-section-white h2 {
  color: white;
}

.subscribe-form {
  display: flex;
  gap: 10px;
}

.subscribe-form input[type="text"] {
  flex: 1;
  padding: 12px 15px;
  border: none;
  border-radius: 4px;
}

.subscribe-form .submit {
  padding: 12px 30px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>

