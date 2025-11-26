import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";

// https://vitejs.dev/config/
export default defineConfig({
  server: {
    host: "::",
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, res) => {
            console.log('⚠️  Error de conexión con el backend:', err.message);
            console.log('💡 Asegúrate de que el backend esté corriendo en http://localhost:8080');
            console.log('💡 Ejecuta: npm run dev:backend o scripts/dev/start-backend.bat');
            if (res && !res.headersSent) {
              res.writeHead(503, {
                'Content-Type': 'application/json',
              });
              res.end(JSON.stringify({
                error: 'Backend no disponible',
                message: 'El servidor backend no está corriendo. Por favor inicia el backend primero.',
                hint: 'Ejecuta: npm run dev:backend o scripts/dev/start-backend.bat'
              }));
            }
          });
        },
      }
    }
  },
  plugins: [
    react(),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      "@core": path.resolve(__dirname, "./src/core"),
      "@features": path.resolve(__dirname, "./src/features"),
      "@shared": path.resolve(__dirname, "./src/shared"),
    },
  },
});
