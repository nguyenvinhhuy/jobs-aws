<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import type { UserSession } from '../../types/api'
import { useI18n } from '../../../core/i18n/useI18n'

defineProps<{
  currentUser: UserSession | null | undefined
  isBootstrapping?: boolean
}>()

const emit = defineEmits<{
  logout: []
}>()

const { t } = useI18n()
const showMenu = ref(false)
const showDropdown = ref(false)

function toggleDropdown(e: Event) {
  e.preventDefault()
  e.stopPropagation()
  showDropdown.value = !showDropdown.value
}

function closeDropdown() {
  showDropdown.value = false
}

function handleClickOutside(e: Event) {
  const target = e.target as HTMLElement
  if (!target.closest('.dropdown')) {
    showDropdown.value = false
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<template>
  <nav class="navbar navbar-expand-lg navbar-light bg-light">
    <div class="container-fluid px-4">
      <RouterLink class="navbar-brand" to="/">Work CV</RouterLink>
      <button
        class="navbar-toggler"
        type="button"
        @click="showMenu = !showMenu"
        :aria-expanded="showMenu"
      >
        <span class="navbar-toggler-icon"></span>
      </button>
      <div :class="['collapse navbar-collapse', { show: showMenu }]" id="navbarNav">
        <ul class="navbar-nav ms-auto align-items-center">
          <li class="nav-item">
            <RouterLink to="/" class="nav-link" @click="showMenu = false">{{ t('common.home') }}</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink to="/jobs" class="nav-link" @click="showMenu = false">{{ t('common.jobs') }}</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink to="/companies" class="nav-link" @click="showMenu = false">{{ t('common.companies') }}</RouterLink>
          </li>

          <!-- Profile Dropdown — Vue-managed (no Bootstrap JS required) -->
          <li v-if="!isBootstrapping && currentUser" class="nav-item dropdown">
            <a
              class="nav-link dropdown-toggle"
              href="#"
              role="button"
              :aria-expanded="showDropdown"
              @click="toggleDropdown"
            >
              {{ t('common.profile') }}
            </a>
            <ul class="dropdown-menu dropdown-menu-end" :class="{ show: showDropdown }">
              <li><RouterLink to="/profile" class="dropdown-item" @click="closeDropdown">{{ t('common.savedJobs') }}</RouterLink></li>
              <li><RouterLink to="/profile" class="dropdown-item" @click="closeDropdown">{{ t('common.appliedJobs') }}</RouterLink></li>
              <li><RouterLink to="/profile" class="dropdown-item" @click="closeDropdown">{{ t('common.followedCompanies') }}</RouterLink></li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <button @click="emit('logout'); closeDropdown()" class="dropdown-item text-danger">
                  {{ t('common.logout') }}
                </button>
              </li>
            </ul>
          </li>

          <li v-if="!isBootstrapping && currentUser" class="nav-item ms-2">
            <RouterLink to="/profile" class="nav-link fw-semibold text-success" @click="showMenu = false">
              {{ currentUser.fullName }}
            </RouterLink>
          </li>

          <li v-if="!isBootstrapping && currentUser?.roleName === 'EMPLOYER'" class="nav-item ms-2">
            <RouterLink to="/profile" class="btn btn-primary btn-sm" @click="showMenu = false">
              {{ t('common.postJob') }}
            </RouterLink>
          </li>

          <li v-if="!isBootstrapping && !currentUser" class="nav-item ms-2">
            <RouterLink to="/profile" class="btn btn-outline-primary btn-sm" @click="showMenu = false">
              {{ t('common.login') }}
            </RouterLink>
          </li>

          <li v-if="isBootstrapping" class="nav-item ms-2">
            <span class="nav-link text-muted" style="font-size:0.85rem;">...</span>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  border-bottom: 1px solid #dee2e6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.navbar-brand {
  font-size: 1.4rem;
  font-weight: 700;
  color: #212529 !important;
}

.nav-link {
  color: #555 !important;
  transition: color 0.2s ease;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #007bff !important;
}

.dropdown-menu {
  min-width: 200px;
  border: 1px solid #dee2e6;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.dropdown-item {
  padding: 0.5rem 1.25rem;
}

.dropdown-item:hover {
  background-color: #f0f4ff;
  color: #007bff;
}
</style>

