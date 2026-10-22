import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// The dev server proxies each API prefix to the service that owns it,
// so the browser only ever talks to localhost:5173 (no CORS config needed).
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api/users': 'http://localhost:8081',
      '/api/carts': 'http://localhost:8082',
      '/api/orders': 'http://localhost:8083',
      '/api/payments': 'http://localhost:8084',
    },
  },
})
