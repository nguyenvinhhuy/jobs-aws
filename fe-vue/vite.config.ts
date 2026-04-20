import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

// In dev/Docker Vite server, proxy /api and /uploads to the backend.
// Local dev: backend at localhost:8080
// Docker:    set BACKEND_URL=http://job-service:8080 in docker-compose env
const backendTarget = process.env['BACKEND_URL'] ?? 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    watch: {
      usePolling: true,
    },
    proxy: {
      '/api': {
        target: backendTarget,
        changeOrigin: true,
      },
      '/uploads': {
        target: backendTarget,
        changeOrigin: true,
      },
    },
  },
})
