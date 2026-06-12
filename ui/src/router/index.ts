import { createRouter, createWebHistory } from 'vue-router'
import AdminPage from '../pages/AdminPage.vue'
import ConnectPage from '../pages/ConnectPage.vue'
import UploadPage from '../pages/UploadPage.vue'

const routes = [
  { path: '/', redirect: '/app/admin' },
  { path: '/app/admin', component: AdminPage },
  { path: '/app/connect', component: ConnectPage },
  { path: '/app/upload', component: UploadPage },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
