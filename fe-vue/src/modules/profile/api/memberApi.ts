import { apiClient } from '../../../core/http/apiClient'
import type {
  ApiMessageResponse,
  ApplyPostSummary,
  CompanySummary,
  ProfileResponse,
  RecruitmentSummary,
  ToggleStateResponse,
} from '../../../shared/types/api'

export async function getProfile() {
  const response = await apiClient.get<ProfileResponse>('/api/member/profile')
  return response.data
}

export async function updateProfile(payload: {
  fullName: string
  address: string
  email: string
  description: string
  phoneNumber: string
}) {
  const response = await apiClient.put<ProfileResponse>('/api/member/profile', payload)
  return response.data
}

export async function updateCompany(payload: {
  companyName: string
  address: string
  email: string
  description: string
  phoneNumber: string
}) {
  const response = await apiClient.put<CompanySummary>('/api/member/company', payload)
  return response.data
}

export async function toggleSaveJob(recruitmentId: number) {
  const response = await apiClient.post<ToggleStateResponse>(`/api/member/saved-jobs/${recruitmentId}`)
  return response.data
}

export async function toggleFollowCompany(companyId: number) {
  const response = await apiClient.post<ToggleStateResponse>(`/api/member/followed-companies/${companyId}`)
  return response.data
}

export async function applyWithExistingCv(payload: { recruitmentId: number; text: string }) {
  const response = await apiClient.post<ApiMessageResponse>('/api/member/applications', payload)
  return response.data
}

export async function applyWithUploadedCv(payload: { recruitmentId: number; text: string; file: File }) {
  const formData = new FormData()
  formData.append('recruitmentId', String(payload.recruitmentId))
  formData.append('text', payload.text)
  formData.append('file', payload.file)

  const response = await apiClient.post<ApiMessageResponse>('/api/member/applications/upload', formData)
  return response.data
}

export async function getSavedJobs() {
  const response = await apiClient.get<RecruitmentSummary[]>('/api/member/saved-jobs')
  return response.data
}

export async function getFollowedCompanies() {
  const response = await apiClient.get<CompanySummary[]>('/api/member/followed-companies')
  return response.data
}

export async function getMyApplications() {
  const response = await apiClient.get<ApplyPostSummary[]>('/api/member/applications')
  return response.data
}

export async function deleteApplication(applyId: number) {
  const response = await apiClient.delete<ApiMessageResponse>(`/api/member/applications/${applyId}`)
  return response.data
}

export async function getEmployerRecruitments() {
  const response = await apiClient.get<RecruitmentSummary[]>('/api/member/employer/recruitments')
  return response.data
}

export async function createRecruitment(payload: {
  title: string
  address: string
  description: string
  experience: string
  quantity: number
  rank: string
  salary: string
  type: string
  deadline: string
  categoryId: number
}) {
  const response = await apiClient.post<RecruitmentSummary>('/api/member/employer/recruitments', payload)
  return response.data
}

export async function updateRecruitment(
  recruitmentId: number,
  payload: {
    title: string
    address: string
    description: string
    experience: string
    quantity: number
    rank: string
    salary: string
    type: string
    deadline: string
    categoryId: number
  },
) {
  const response = await apiClient.put<RecruitmentSummary>(
    `/api/member/employer/recruitments/${recruitmentId}`,
    payload,
  )
  return response.data
}

export async function deleteRecruitment(recruitmentId: number) {
  const response = await apiClient.delete<ApiMessageResponse>(
    `/api/member/employer/recruitments/${recruitmentId}`,
  )
  return response.data
}

export async function getEmployerApplicants() {
  const response = await apiClient.get<ApplyPostSummary[]>('/api/member/employer/applicants')
  return response.data
}

export async function approveApplicant(applyId: number) {
  const response = await apiClient.post<ApplyPostSummary>(
    `/api/member/employer/applicants/${applyId}/approve`,
  )
  return response.data
}

async function uploadFile<T>(url: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await apiClient.post<T>(url, formData)
  return response.data
}

export function uploadAvatar(file: File) {
  return uploadFile<ProfileResponse>('/api/member/profile/avatar', file)
}

export function uploadCv(file: File) {
  return uploadFile<ProfileResponse>('/api/member/profile/cv', file)
}

export function uploadLogo(file: File) {
  return uploadFile<CompanySummary>('/api/member/company/logo', file)
}
