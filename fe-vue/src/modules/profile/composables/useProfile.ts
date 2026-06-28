import { ref } from 'vue'

import type { ApplyPostSummary, CompanySummary, ProfileResponse, RecruitmentSummary } from '../../../shared/types/api'
import {
  deleteApplication,
  getFollowedCompanies,
  getMyApplications,
  getProfile,
  getSavedJobs,
  updateCompany,
  updateProfile,
  uploadAvatar,
  uploadCv,
  uploadLogo,
} from '../api/memberApi'

const MAX_IMAGE_SIZE = 5 * 1024 * 1024
const MAX_CV_SIZE = 10 * 1024 * 1024
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

export interface FileValidationError {
  type: 'invalidType' | 'tooLarge'
}

export function useProfile() {
  const profile = ref<ProfileResponse | null>(null)
  const savedJobs = ref<RecruitmentSummary[]>([])
  const followedCompanies = ref<CompanySummary[]>([])
  const myApplications = ref<ApplyPostSummary[]>([])

  const profileForm = ref({
    fullName: '',
    address: '',
    email: '',
    description: '',
    phoneNumber: '',
  })

  const companyForm = ref({
    companyName: '',
    address: '',
    email: '',
    description: '',
    phoneNumber: '',
  })

  async function load() {
    const [loadedProfile, loadedSavedJobs, loadedCompanies, loadedApplications] = await Promise.all([
      getProfile(),
      getSavedJobs(),
      getFollowedCompanies(),
      getMyApplications(),
    ])

    profile.value = loadedProfile
    savedJobs.value = loadedSavedJobs
    followedCompanies.value = loadedCompanies
    myApplications.value = loadedApplications

    profileForm.value = {
      fullName: loadedProfile.fullName,
      address: loadedProfile.address,
      email: loadedProfile.email,
      description: loadedProfile.description ?? '',
      phoneNumber: loadedProfile.phoneNumber,
    }

    if (loadedProfile.company) {
      companyForm.value = {
        companyName: loadedProfile.company.companyName,
        address: loadedProfile.company.address,
        email: loadedProfile.company.email,
        description: loadedProfile.company.description,
        phoneNumber: loadedProfile.company.phoneNumber,
      }
    }

    return loadedProfile
  }

  async function saveProfile() {
    profile.value = await updateProfile(profileForm.value)
  }

  async function saveCompanyProfile() {
    const company = await updateCompany(companyForm.value)
    if (profile.value) profile.value = { ...profile.value, company }
  }

  function validateFile(file: File, type: 'avatar' | 'cv' | 'logo'): FileValidationError | null {
    if (type === 'avatar' || type === 'logo') {
      if (!ALLOWED_IMAGE_TYPES.includes(file.type)) return { type: 'invalidType' }
      if (file.size > MAX_IMAGE_SIZE) return { type: 'tooLarge' }
    } else if (type === 'cv') {
      if (file.size > MAX_CV_SIZE) return { type: 'tooLarge' }
    }
    return null
  }

  async function handleAvatarUpload(file: File) {
    profile.value = await uploadAvatar(file)
  }

  async function handleCvUpload(file: File) {
    profile.value = await uploadCv(file)
  }

  async function handleLogoUpload(file: File) {
    const company = await uploadLogo(file)
    if (profile.value) profile.value = { ...profile.value, company }
  }

  async function removeApplication(applyId: number) {
    await deleteApplication(applyId)
    myApplications.value = await getMyApplications()
  }

  return {
    profile,
    savedJobs,
    followedCompanies,
    myApplications,
    profileForm,
    companyForm,
    load,
    saveProfile,
    saveCompanyProfile,
    validateFile,
    handleAvatarUpload,
    handleCvUpload,
    handleLogoUpload,
    removeApplication,
  }
}
