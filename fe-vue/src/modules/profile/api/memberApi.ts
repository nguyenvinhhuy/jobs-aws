import { apiClient } from '../../../core/http/apiClient'
import type {
  ApiMessageResponse,
  ApplyPostSummary,
  CompanySummary,
  ProfileResponse,
  RecruitmentSummary,
  ToggleStateResponse,
} from '../../../shared/types/api'

export function getProfile() {
  return apiClient.get<ProfileResponse>('/api/member/profile').then((response) => response.data)
}

export function updateProfile(payload: {
  fullName: string
  address: string
  email: string
  description: string
  phoneNumber: string
}) {
  return apiClient.put<ProfileResponse>('/api/member/profile', payload).then((response) => response.data)
}

export function updateCompany(payload: {
  companyName: string
  address: string
  email: string
  description: string
  phoneNumber: string
}) {
  return apiClient.put<CompanySummary>('/api/member/company', payload).then((response) => response.data)
}

export function toggleSaveJob(recruitmentId: number) {
  return apiClient
    .post<ToggleStateResponse>(`/api/member/saved-jobs/${recruitmentId}`)
    .then((response) => response.data)
}

export function toggleFollowCompany(companyId: number) {
  return apiClient
    .post<ToggleStateResponse>(`/api/member/followed-companies/${companyId}`)
    .then((response) => response.data)
}

export function applyWithExistingCv(payload: { recruitmentId: number; text: string }) {
  return apiClient.post<ApiMessageResponse>('/api/member/applications', payload).then((response) => response.data)
}

export function applyWithUploadedCv(payload: { recruitmentId: number; text: string; file: File }) {
  const formData = new FormData()
  formData.append('recruitmentId', String(payload.recruitmentId))
  formData.append('text', payload.text)
  formData.append('file', payload.file)

  return apiClient.post<ApiMessageResponse>('/api/member/applications/upload', formData).then((response) => response.data)
}

export function getSavedJobs() {
  return apiClient.get<RecruitmentSummary[]>('/api/member/saved-jobs').then((response) => response.data)
}

export function getFollowedCompanies() {
  return apiClient.get<CompanySummary[]>('/api/member/followed-companies').then((response) => response.data)
}

export function getMyApplications() {
  return apiClient.get<ApplyPostSummary[]>('/api/member/applications').then((response) => response.data)
}

export function deleteApplication(applyId: number) {
  return apiClient.delete<ApiMessageResponse>(`/api/member/applications/${applyId}`).then((response) => response.data)
}

export function getEmployerRecruitments() {
  return apiClient.get<RecruitmentSummary[]>('/api/member/employer/recruitments').then((response) => response.data)
}

export function createRecruitment(payload: {
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
  return apiClient.post<RecruitmentSummary>('/api/member/employer/recruitments', payload).then((response) => response.data)
}

export function updateRecruitment(
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
  return apiClient
    .put<RecruitmentSummary>(`/api/member/employer/recruitments/${recruitmentId}`, payload)
    .then((response) => response.data)
}

export function deleteRecruitment(recruitmentId: number) {
  return apiClient.delete<ApiMessageResponse>(`/api/member/employer/recruitments/${recruitmentId}`).then((response) => response.data)
}

export function getEmployerApplicants() {
  return apiClient.get<ApplyPostSummary[]>('/api/member/employer/applicants').then((response) => response.data)
}

export function approveApplicant(applyId: number) {
  return apiClient
    .post<ApplyPostSummary>(`/api/member/employer/applicants/${applyId}/approve`)
    .then((response) => response.data)
}

function uploadFile<T>(url: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return apiClient.post<T>(url, formData).then((response) => response.data)
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
