import { computed } from 'vue'

import { locale, setLocale, t } from './i18n'
import type { Locale } from './messages'

export function useI18n() {
  return {
    locale: computed(() => locale.value),
    t,
    setLocale,
    isLocale: (candidate: Locale) => locale.value === candidate,
  }
}
