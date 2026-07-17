import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Forwards to the Spring Boot backend (run separately on 8080) so the
      // app can call same-origin '/api/...' paths with no CORS config needed,
      // in dev and after a same-origin production deploy alike.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
