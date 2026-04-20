export interface CategorySummary {
  id: number
  name: string
  numberChoose: number
}

export interface CompanySummary {
  id: number
  companyName: string
  address: string
  description: string
  email: string
  logo: string | null
  phoneNumber: string
  recruitmentCount: number
}

export interface RecruitmentSummary {
  id: number
  title: string
  address: string
  description: string
  salary: string
  type: string
  rank: string
  experience: string
  quantity: number
  deadline: string
  view: number
  createdAt: string
  companyId: number
  companyName: string
  categoryId: number
  categoryName: string
}

export interface RecruitmentDetail {
  id: number
  title: string
  address: string
  description: string
  salary: string
  type: string
  rank: string
  experience: string
  quantity: number
  deadline: string
  view: number
  createdAt: string
  companyId: number
  companyName: string
  companyDescription: string
  companyEmail: string
  companyPhoneNumber: string
  categoryId: number
  categoryName: string
  relatedRecruitments: RecruitmentSummary[]
}

export interface CompanyDetail {
  id: number
  companyName: string
  address: string
  description: string
  email: string
  logo: string | null
  phoneNumber: string
  recruitmentCount: number
  recruitments: RecruitmentSummary[]
}

export interface HomeOverview {
  totalCompany: number
  totalRecruitment: number
  totalCandidate: number
  topCategories: CategorySummary[]
  topCompanies: CompanySummary[]
  topRecruitments: RecruitmentSummary[]
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface UserSession {
  id: number
  fullName: string
  email: string
  roleName: 'EMPLOYER' | 'USER'
  image: string | null
  companyId: number | null
}

export interface ProfileResponse {
  id: number
  fullName: string
  address: string
  email: string
  description: string | null
  image: string | null
  phoneNumber: string
  status: number
  roleName: 'EMPLOYER' | 'USER'
  cvFileName: string | null
  company: CompanySummary | null
}

export interface ApplyPostSummary {
  id: number
  candidateName: string
  candidateEmail: string
  cvFileName: string
  text: string
  status: number
  createdAt: string
  recruitmentId: number
  recruitmentTitle: string
}

export interface ApiMessageResponse {
  message: string
  verificationUrl?: string | null
}

export interface ToggleStateResponse {
  active: boolean
  message: string
}
