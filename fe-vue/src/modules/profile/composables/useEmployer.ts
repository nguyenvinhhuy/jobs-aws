import { ref } from 'vue'
import type { Ref } from 'vue'

import type { ApplyPostSummary, CategorySummary, RecruitmentSummary } from '../../../shared/types/api'
import {
  approveApplicant,
  createRecruitment,
  deleteRecruitment,
  getEmployerApplicants,
  getEmployerRecruitments,
  updateRecruitment,
} from '../api/memberApi'

export const JOB_RANKS = ['Fresher', 'Junior', 'Middle', 'Senior', 'Lead', 'Manager'] as const
export const JOB_TYPES = ['Fulltime', 'Parttime', 'Remote', 'Contract', 'Internship'] as const

function defaultRecruitmentForm(categories: CategorySummary[]) {
  return {
    title: '',
    address: '',
    description: '',
    experience: '',
    quantity: 1,
    rank: JOB_RANKS[0] as string,
    salary: '',
    type: JOB_TYPES[0] as string,
    deadline: '',
    categoryId: categories[0]?.id ?? 1,
  }
}

export function useEmployer(categories: Ref<CategorySummary[]>) {
  const employerJobs = ref<RecruitmentSummary[]>([])
  const employerApplicants = ref<ApplyPostSummary[]>([])
  const editingRecruitmentId = ref<number | null>(null)
  const recruitmentForm = ref(defaultRecruitmentForm(categories.value))

  async function load() {
    ;[employerJobs.value, employerApplicants.value] = await Promise.all([
      getEmployerRecruitments(),
      getEmployerApplicants(),
    ])
  }

  async function refreshJobs() {
    employerJobs.value = await getEmployerRecruitments()
  }

  function startEdit(job: RecruitmentSummary) {
    editingRecruitmentId.value = job.id
    recruitmentForm.value = {
      title: job.title,
      address: job.address,
      description: job.description,
      experience: job.experience,
      quantity: job.quantity,
      rank: job.rank,
      salary: job.salary,
      type: job.type,
      deadline: job.deadline,
      categoryId: job.categoryId,
    }
  }

  function resetForm() {
    editingRecruitmentId.value = null
    recruitmentForm.value = defaultRecruitmentForm(categories.value)
  }

  async function saveRecruitment() {
    if (editingRecruitmentId.value) {
      await updateRecruitment(editingRecruitmentId.value, recruitmentForm.value)
    } else {
      await createRecruitment(recruitmentForm.value)
    }
    await refreshJobs()
    resetForm()
  }

  async function removeRecruitment(recruitmentId: number) {
    await deleteRecruitment(recruitmentId)
    await refreshJobs()
    if (editingRecruitmentId.value === recruitmentId) {
      resetForm()
    }
  }

  async function approve(applyId: number) {
    await approveApplicant(applyId)
    employerApplicants.value = await getEmployerApplicants()
  }

  return {
    employerJobs,
    employerApplicants,
    editingRecruitmentId,
    recruitmentForm,
    load,
    startEdit,
    resetForm,
    saveRecruitment,
    removeRecruitment,
    approve,
  }
}
