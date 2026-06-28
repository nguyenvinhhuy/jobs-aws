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

export async function getHomeOverview() {
  const response = await apiClient.get<HomeOverview>('/api/public/home')
  return response.data
}

export async function getCategories() {
  const response = await apiClient.get<CategorySummary[]>('/api/public/categories')
  return response.data
}

export async function getRecruitments(params: {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number | null
  companyId?: number | null
  address?: string | null
  jobType?: string | null
}) {
  const response = await apiClient.get<PageResponse<RecruitmentSummary>>('/api/public/recruitments', { params })
  return response.data
}

export async function getRecruitmentDetail(id: number) {
  const response = await apiClient.get<RecruitmentDetail>(`/api/public/recruitments/${id}`)
  return response.data
}

export async function getCompanyDetail(id: number) {
  const response = await apiClient.get<CompanyDetail>(`/api/public/companies/${id}`)
  return response.data
}

export async function getCompanies(params?: { keyword?: string; limit?: number }) {
  const response = await apiClient.get<CompanySummary[]>('/api/public/companies', { params })
  return response.data
}
