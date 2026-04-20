import { apiClient } from '../../../core/http/apiClient'
import type {
  CategorySummary,
  CompanyDetail,
  CompanySummary,
  HomeOverview,
  PageResponse,
  RecruitmentDetail,
  RecruitmentSummary,
} from '../../../shared/types/api'

export function getHomeOverview() {
  return apiClient.get<HomeOverview>('/api/public/home').then((response) => response.data)
}

export function getCategories() {
  return apiClient.get<CategorySummary[]>('/api/public/categories').then((response) => response.data)
}

export function getRecruitments(params: {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number | null
  companyId?: number | null
  address?: string | null
  jobType?: string | null
}) {
  return apiClient
    .get<PageResponse<RecruitmentSummary>>('/api/public/recruitments', { params })
    .then((response) => response.data)
}

export function getRecruitmentDetail(id: number) {
  return apiClient.get<RecruitmentDetail>(`/api/public/recruitments/${id}`).then((response) => response.data)
}

export function getCompanyDetail(id: number) {
  return apiClient.get<CompanyDetail>(`/api/public/companies/${id}`).then((response) => response.data)
}

export function getTopCompanies(limit = 4) {
  return apiClient
    .get<CompanySummary[]>('/api/public/companies/top', { params: { limit } })
    .then((response) => response.data)
}

export function getCompanies(params?: { keyword?: string; limit?: number }) {
  return apiClient
    .get<CompanySummary[]>('/api/public/companies', { params })
    .then((response) => response.data)
}

