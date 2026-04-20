import type { RouteRecordRaw } from 'vue-router'

import VerifyEmailPage from '../modules/auth/pages/VerifyEmailPage.vue'
import CompaniesPage from '../modules/company/pages/CompaniesPage.vue'
import CompanyPage from '../modules/company/pages/CompanyPage.vue'
import HomePage from '../modules/jobs/pages/HomePage.vue'
import JobDetailPage from '../modules/jobs/pages/JobDetailPage.vue'
import JobsPage from '../modules/jobs/pages/JobsPage.vue'
import ProfilePage from '../modules/profile/pages/ProfilePage.vue'

export const appRoutes: RouteRecordRaw[] = [
  { path: '/', component: HomePage },
  { path: '/jobs', component: JobsPage },
  { path: '/jobs/:id', component: JobDetailPage },
  { path: '/companies', component: CompaniesPage },
  { path: '/companies/:id', component: CompanyPage },
  { path: '/profile', component: ProfilePage },
  { path: '/verify-email', component: VerifyEmailPage },
]
