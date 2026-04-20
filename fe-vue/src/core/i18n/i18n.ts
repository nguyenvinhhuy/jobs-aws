import { ref } from 'vue'

import { messages, type Locale } from './messages'

const storedLocale = typeof window !== 'undefined' ? window.localStorage.getItem('jobs-aws-locale') : null
export const locale = ref<Locale>((storedLocale === 'vi' || storedLocale === 'en' ? storedLocale : 'vi') as Locale)

function getByPath(source: unknown, path: string): string {
  const value = path.split('.').reduce<unknown>((current, segment) => {
    if (current && typeof current === 'object' && segment in current) {
      return (current as Record<string, unknown>)[segment]
    }
    return undefined
  }, source)

  return typeof value === 'string' ? value : path
}

export function t(path: string) {
  return getByPath(messages[locale.value], path)
}

export function setLocale(nextLocale: Locale) {
  locale.value = nextLocale
  if (typeof window !== 'undefined') {
    window.localStorage.setItem('jobs-aws-locale', nextLocale)
    document.documentElement.lang = nextLocale
  }
}
