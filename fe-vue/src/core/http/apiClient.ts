import axios from 'axios'

import { env } from '../config/env'
import { logger } from '../monitoring/logger'
import { ApiError } from './ApiError'
import { redirectToLogin } from './authRedirect'

export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  withCredentials: true,
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status as number | undefined
    const requestUrl = String(error.config?.url ?? '')
    // Spring Boot ProblemDetail uses "detail" not "message"
    const message =
      error.response?.data?.detail ??
      error.response?.data?.message ??
      error.message ??
      'Unexpected API error'

    if (status === 401 && requestUrl.startsWith('/api/member')) {
      redirectToLogin()
    }

    if (!status || status >= 500) {
      logger.error('API request failed', { message, status, error })
    }

    return Promise.reject(new ApiError(message, status))
  },
)
