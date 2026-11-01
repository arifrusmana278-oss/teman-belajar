import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const ngrokHosts = (env.NGROK_HOST || '').split(',').map(host => host.trim()).filter(Boolean)
  return {
    plugins: [vue()],
    server: {
      host: '0.0.0.0',
      port: 5173,
      allowedHosts: ['.ngrok-free.dev', '.ngrok.app', '.ngrok-free.app', '.ngrok.io', ...ngrokHosts],
      proxy: {
        '/api': {
          target: env.VITE_API_PROXY_TARGET || 'http://localhost:8080',
          changeOrigin: true,
          configure: proxy => proxy.on('proxyReq', proxyRequest => proxyRequest.removeHeader('origin'))
        }
      }
    }
  }
})
