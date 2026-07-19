import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Login from '@/views/Login.vue'
import Dashboard from '@/views/Dashboard.vue'
import Devices from '@/views/Devices.vue'
import TimeControl from '@/views/TimeControl.vue'
import BlockList from '@/views/BlockList.vue'
import Statistics from '@/views/Statistics.vue'
import Settings from '@/views/Settings.vue'
import Approvals from '@/views/Approvals.vue'
import Alerts from '@/views/Alerts.vue'
import Relationships from '@/views/Relationships.vue'
import ContentFilters from '@/views/ContentFilters.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Login',
    component: Login
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/devices',
    name: 'Devices',
    component: Devices,
    meta: { requiresAuth: true }
  },
  {
    path: '/time-control',
    name: 'TimeControl',
    component: TimeControl,
    meta: { requiresAuth: true }
  },
  {
    path: '/block-list',
    name: 'BlockList',
    component: BlockList,
    meta: { requiresAuth: true }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: Statistics,
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { requiresAuth: true }
  },
  {
    path: '/approvals',
    name: 'Approvals',
    component: Approvals,
    meta: { requiresAuth: true }
  },
  {
    path: '/relationships',
    name: 'Relationships',
    component: Relationships,
    meta: { requiresAuth: true }
  },
  {
    path: '/alerts',
    name: 'Alerts',
    component: Alerts,
    meta: { requiresAuth: true }
  },
  {
    path: '/content-filters',
    name: 'ContentFilters',
    component: ContentFilters,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
