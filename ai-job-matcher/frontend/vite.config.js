import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'


export default defineConfig({
  plugins: [react()],
  server: {
    // Calls to /api/... are forwarded to the Spring Boot backend in development.
    proxy: {
      '/api': 'http:///api',
    },
  },
})