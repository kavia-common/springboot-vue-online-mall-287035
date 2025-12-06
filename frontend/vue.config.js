module.exports = {
  devServer: {
    // Listen on all interfaces for container access; can override via env
    host: process.env.HOST || '0.0.0.0',
    port: Number(process.env.VUE_APP_DEV_SERVER_PORT || process.env.PORT || 3000),
    proxy: {
      '/api': {
        target: process.env.VUE_APP_API_URL || 'http://localhost:3001',
        changeOrigin: true,
        pathRewrite: {
          '/api': ''
        }
      }
    }
  },
};