import { createApp } from 'vue'
import '../styles/global.css'
import App from './App.vue'
import { installAuthProvider } from './providers/auth-provider'
import { router } from './router'

const app = createApp(App)
installAuthProvider(app)
app.use(router)
app.mount('#app')
