<script setup lang="ts">
import { useI18n } from '../../../core/i18n/useI18n'

defineProps<{
  loading?: boolean
  errorMessage?: string
  noticeMessage?: string
}>()

const { t } = useI18n()
</script>

<template>
  <div v-if="loading || errorMessage || noticeMessage" class="mb-3">
    <div v-if="loading" class="alert alert-info py-2 mb-1">{{ t('common.loading') }}</div>
    <div v-if="errorMessage" class="alert alert-danger py-2 mb-1">{{ errorMessage }}</div>
    <div v-if="noticeMessage" class="alert alert-success py-2 mb-1">{{ noticeMessage }}</div>
  </div>
</template>
