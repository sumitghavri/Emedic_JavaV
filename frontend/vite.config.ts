import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  return {
    plugins: [react()],
    server: {
      host: '127.0.0.1',
      port: 5173,
      proxy: {
        '/api': {
          target: env.VITE_API_PROXY_TARGET || 'http://localhost:8087',
          changeOrigin: true,
          // Browser requests are same-origin with Vite. Don't forward that local
          // Origin header to Spring, where it would be checked against the
          // separately configured direct-API CORS allowlist (often port 5173).
          configure: proxy => {
            proxy.on('proxyReq', proxyRequest => proxyRequest.removeHeader('origin'))
          },
        },
      },
    },
  }
})
