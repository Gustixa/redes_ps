import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: [
      { find: "@pages", replacement: resolve(__dirname, './src/pages')},
      { find: "@routing", replacement: resolve(__dirname, './src/routes')},
      { find: '@contexts', replacement: resolve(__dirname, './src/context')},
      { find: '@components', replacement: resolve(__dirname, './src/components')},
    ]
  }
})
