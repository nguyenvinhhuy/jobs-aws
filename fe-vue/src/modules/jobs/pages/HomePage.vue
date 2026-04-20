<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useI18n } from '../../../core/i18n/useI18n'
import { useAuth } from '../../auth/composables/useAuth'
import { ensureAuthenticated } from '../../auth/utils/authAction'
import { getHomeOverview } from '../api/jobsApi'
import { toggleFollowCompany } from '../../profile/api/memberApi'
import { toggleSaveJob } from '../../profile/api/memberApi'
import type { HomeOverview } from '../../../shared/types/api'
import StatusBanner from '../../../shared/components/common/StatusBanner.vue'

const router = useRouter()
const auth = useAuth()
const { t } = useI18n()
const loading = ref(false)
const errorMessage = ref('')
const noticeMessage = ref('')
const home = ref<HomeOverview | null>(null)
const activeTab = ref(1)
const searchKey = ref('')

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    home.value = await getHomeOverview()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('home.subtitle')
  } finally {
    loading.value = false
  }
}

async function followCompany(companyId: number) {
  if (!ensureAuthenticated(auth, router, () => {
    errorMessage.value = t('common.loginRequired')
  })) {
    return
  }

  try {
    const result = await toggleFollowCompany(companyId)
    noticeMessage.value = result.message
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.loginRequired')
  }
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
    errorMessage.value = error instanceof Error ? error.message : t('common.loginRequired')
  }
}

function handleSearch() {
  if (searchKey.value.trim()) {
    if (activeTab.value === 1) {
      router.push({ path: '/jobs', query: { keyword: searchKey.value } })
    } else if (activeTab.value === 2) {
      router.push({ path: '/jobs', query: { company: searchKey.value } })
    } else if (activeTab.value === 3) {
      router.push({ path: '/jobs', query: { address: searchKey.value } })
    }
  }
}

onMounted(() => {
  void loadData()
})
</script>

<template>
  <div class="page-stack">
    <!-- Hero Section -->
    <div class="hero-wrap img" style="background: linear-gradient(rgba(0,0,0,0.55), rgba(0,0,0,0.55)), linear-gradient(135deg, #1a2a4a 0%, #0f3460 100%);">
      <div class="overlay"></div>
      <div class="container">
        <div class="row d-md-flex no-gutters slider-text align-items-center justify-content-center">
          <div class="col-md-10 d-flex align-items-center">
            <div class="text text-center pt-5 mt-md-5">
              <p class="mb-4">{{ t('home.eyebrow') }}</p>
              <h1 class="mb-5">{{ t('home.title') }}</h1>

              <!-- Stats -->
              <div v-if="home" class="ftco-counter ftco-no-pt ftco-no-pb">
                <div class="row">
                  <div class="col-md-4 d-flex justify-content-center counter-wrap">
                    <div class="block-18">
                      <div class="text d-flex">
                        <div class="icon mr-2">
                          <span class="flaticon-visitor"></span>
                        </div>
                        <div class="desc text-left">
                          <strong class="number">{{ home.totalCandidate }}</strong>
                          <span>{{ t('home.candidates') }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="col-md-4 d-flex justify-content-center counter-wrap">
                    <div class="block-18 text-center">
                      <div class="text d-flex">
                        <div class="icon mr-2">
                          <span class="flaticon-visitor"></span>
                        </div>
                        <div class="desc text-left">
                          <strong class="number">{{ home.totalCompany }}</strong>
                          <span>{{ t('home.companies') }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="col-md-4 d-flex justify-content-center counter-wrap">
                    <div class="block-18 text-center">
                      <div class="text d-flex">
                        <div class="icon mr-2">
                          <span class="flaticon-resume"></span>
                        </div>
                        <div class="desc text-left">
                          <strong class="number">{{ home.totalRecruitment }}</strong>
                          <span>{{ t('home.recruitments') }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Search Tabs -->
              <div class="ftco-search my-md-5">
                <div class="row">
                  <div class="col-md-12 nav-link-wrap">
                    <div class="nav nav-pills text-center" id="v-pills-tab" role="tablist">
                      <a
                        :class="['nav-link', { active: activeTab === 1 }]"
                        @click="activeTab = 1"
                        role="tab"
                      >{{ t('home.searchJob') }}</a>
                      <a
                        :class="['nav-link', { active: activeTab === 2 }]"
                        @click="activeTab = 2"
                        role="tab"
                      >{{ t('home.searchCompany') }}</a>
                      <a
                        :class="['nav-link', { active: activeTab === 3 }]"
                        @click="activeTab = 3"
                        role="tab"
                      >{{ t('home.searchLocation') }}</a>
                    </div>
                  </div>
                  <div class="col-md-12 tab-wrap">
                    <div class="tab-content p-4" id="v-pills-tabContent">
                      <div v-if="activeTab === 1" class="tab-pane fade show active">
                        <form @submit.prevent="handleSearch" class="search-job">
                          <div class="row no-gutters">
                            <div class="col-md-10 mr-md-2">
                              <div class="form-group">
                                <div class="form-field">
                                  <div class="icon"><span class="icon-map-marker"></span></div>
                                  <input v-model="searchKey" type="text" class="form-control" :placeholder="t('home.searchJobPlaceholder')">
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

                      <div v-else-if="activeTab === 2" class="tab-pane fade show active">
                        <form @submit.prevent="handleSearch" class="search-job">
                          <div class="row no-gutters">
                            <div class="col-md-10 mr-md-2">
                              <div class="form-group">
                                <div class="form-field">
                                  <div class="icon"><span class="icon-map-marker"></span></div>
                                  <input v-model="searchKey" type="text" class="form-control" :placeholder="t('home.searchCompanyPlaceholder')">
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

                      <div v-else-if="activeTab === 3" class="tab-pane fade show active">
                        <form @submit.prevent="handleSearch" class="search-job">
                          <div class="row no-gutters">
                            <div class="col-md-10 mr-md-2">
                              <div class="form-group">
                                <div class="form-field">
                                  <div class="icon"><span class="icon-map-marker"></span></div>
                                  <input v-model="searchKey" type="text" class="form-control" :placeholder="t('home.searchLocationPlaceholder')">
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
        </div>
      </div>
    </div>

    <StatusBanner :loading="loading" :error-message="errorMessage" :notice-message="noticeMessage" />

    <!-- Categories Section -->
    <template v-if="home">
      <section class="ftco-section">
        <div class="container">
          <div class="row justify-content-center mb-5">
            <div class="col-md-7 heading-section text-center">
              <span class="subheading">{{ t('home.categoryLabel') }}</span>
              <h2 class="mb-0">{{ t('home.topCategories') }}</h2>
            </div>
          </div>
          <div class="row">
            <div v-for="category in home.topCategories" :key="category.id" class="col-md-3">
              <ul class="category text-center">
                <li>
                  <a
                    style="text-decoration: none !important;"
                    @click="router.push({ path: '/jobs', query: { categoryId: String(category.id) } })"
                  >
                    <p>{{ category.name }}</p>
                    <span class="number">{{ category.numberChoose }}</span>
                    <span>{{ t('home.positions') }}</span>
                    <i class="ion-ios-arrow-forward"></i>
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      <!-- Services Section -->
      <section class="ftco-section services-section">
        <div class="container">
          <div class="row d-flex">
            <div class="col-md-3 d-flex align-self-stretch">
              <div class="media block-6 services d-block">
                <div class="icon"><span class="flaticon-resume"></span></div>
                <div class="media-body">
                  <h3 class="heading mb-3">{{ t('home.service1') }}</h3>
                  <p>{{ t('home.serviceDesc') }}</p>
                </div>
              </div>
            </div>
            <div class="col-md-3 d-flex align-self-stretch">
              <div class="media block-6 services d-block">
                <div class="icon"><span class="flaticon-team"></span></div>
                <div class="media-body">
                  <h3 class="heading mb-3">{{ t('home.service2') }}</h3>
                  <p>{{ t('home.serviceDesc') }}</p>
                </div>
              </div>
            </div>
            <div class="col-md-3 d-flex align-self-stretch">
              <div class="media block-6 services d-block">
                <div class="icon"><span class="flaticon-career"></span></div>
                <div class="media-body">
                  <h3 class="heading mb-3">{{ t('home.service3') }}</h3>
                  <p>{{ t('home.serviceDesc') }}</p>
                </div>
              </div>
            </div>
            <div class="col-md-3 d-flex align-self-stretch">
              <div class="media block-6 services d-block">
                <div class="icon"><span class="flaticon-employees"></span></div>
                <div class="media-body">
                  <h3 class="heading mb-3">{{ t('home.service4') }}</h3>
                  <p>{{ t('home.serviceDesc') }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Hot Jobs Section -->
      <section class="ftco-section bg-light">
        <div class="container">
          <div class="row">
            <div class="col-lg-9 pr-lg-5">
              <div class="row justify-content-center pb-3">
                <div class="col-md-12 heading-section">
                  <span class="subheading">{{ t('home.hotJobsLabel') }}</span>
                  <h2 class="mb-4">{{ t('home.hotJobs') }}</h2>
                </div>
              </div>
              <div class="row">
                <div v-for="job in home.topRecruitments" :key="job.id" class="col-md-12">
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
                      <div v-if="auth.currentUser.value">
                        <a @click="saveJob(job.id)" style="cursor: pointer;" class="icon text-center d-flex justify-content-center align-items-center icon mr-2">
                          <span class="icon-heart"></span>
                        </a>
                      </div>
                      <button v-if="auth.currentUser.value" @click="router.push(`/jobs/${job.id}`)" class="btn btn-primary py-2">{{ t('common.applyJob') }}</button>
                      <button v-else @click="router.push('/profile')" class="btn btn-primary py-2">{{ t('common.login') }}</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Sidebar - Top Companies -->
            <div class="col-lg-3 sidebar">
              <div class="row justify-content-center pb-3">
                <div class="col-md-12 heading-section">
                  <h2 class="mb-4" style="margin-top: 30px;">{{ t('home.topCompanies') }}</h2>
                </div>
              </div>
              <div v-for="company in home.topCompanies" :key="company.id" class="sidebar-box">
                <div class="">
                  <a @click="router.push(`/companies/${company.id}`)" class="company-wrap" style="cursor: pointer;">
                    <img style="padding-left: 16px;" height="100" width="100" :src="company.logo ? `/uploads/logos/${company.logo}` : '/favicon.svg'" class="img-fluid" alt="Logo">
                  </a>
                  <div class="text p-3">
                    <h3><a @click="router.push(`/companies/${company.id}`)" style="cursor: pointer;">{{ company.companyName }}</a></h3>
                    <p><span class="number" style="color: black;">{{ company.recruitmentCount }}</span> <span>{{ t('home.positions') }}</span></p>
                    <button v-if="auth.currentUser.value" @click="followCompany(company.id)" class="btn btn-sm btn-outline-primary w-100">{{ t('common.follow') }}</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.hero-wrap {
  background-size: cover;
  background-position: center;
  position: relative;
  min-height: 600px;
  display: flex;
  align-items: center;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.hero-wrap .container {
  position: relative;
  z-index: 1;
}

.slider-text {
  color: white;
}

.slider-text p {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
}

.slider-text h1 {
  font-size: 48px;
  color: white;
  font-weight: bold;
}

.ftco-counter {
  margin-top: 40px;
}

.counter-wrap {
  margin-bottom: 20px;
}

.block-18 {
  background: white;
  padding: 30px;
  border-radius: 8px;
}

.block-18 .text {
  display: flex;
  align-items: center;
}

.block-18 .icon {
  font-size: 40px;
  color: #007bff;
}

.block-18 .number {
  font-size: 28px;
  font-weight: bold;
  color: #007bff;
}

.ftco-search {
  margin-top: 50px;
}

.nav-link-wrap {
  margin-bottom: 20px;
}

.nav {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.nav-link {
  padding: 12px 24px;
  background: white;
  border: 1px solid #ddd;
  cursor: pointer;
  border-radius: 4px;
}

.nav-link.active {
  background: #007bff;
  color: white;
}

.tab-wrap {
  margin-top: 20px;
}

.tab-pane {
  display: block;
}

.search-job {
  width: 100%;
}

.form-field {
  display: flex;
  align-items: center;
  position: relative;
}

.form-field .icon {
  position: absolute;
  left: 15px;
}

.form-field input {
  padding-left: 45px;
}

.ftco-section {
  padding: 40px 0;
}

.ftco-section.bg-light {
  background-color: #f8f9fa;
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

.category {
  list-style: none;
  padding: 0;
}

.category li {
  margin-bottom: 15px;
}

.category a {
  display: block;
  padding: 20px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.category a:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.category .number {
  font-size: 24px;
  font-weight: bold;
  color: #007bff;
}

.services-section {
  background: white;
}

.block-6 {
  padding: 20px;
  text-align: center;
}

.block-6 .icon {
  font-size: 48px;
  color: #007bff;
  margin-bottom: 20px;
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
}

.one-forth {
  text-align: right;
}

.sidebar-box {
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 20px;
  overflow: hidden;
}

.sidebar-box .company-wrap {
  display: block;
  padding: 10px;
  text-align: center;
}

.sidebar-box img {
  max-width: 100%;
  height: auto;
}
</style>

