import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/events/:id',
    name: 'EventDetail',
    component: () => import('@/views/EventDetail.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/my-registrations',
    name: 'MyRegistrations',
    component: () => import('@/views/MyRegistrations.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/Notifications.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'events',
        name: 'AdminEvents',
        component: () => import('@/views/admin/Events.vue')
      },
      {
        path: 'events/create',
        name: 'CreateEvent',
        component: () => import('@/views/admin/EventForm.vue')
      },
      {
        path: 'events/edit/:id',
        name: 'EditEvent',
        component: () => import('@/views/admin/EventForm.vue')
      },
      {
        path: 'events/:id/registrations',
        name: 'EventRegistrations',
        component: () => import('@/views/admin/Registrations.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  if (!userStore.isLoggedIn && localStorage.getItem('token')) {
    await userStore.fetchUserInfo()
  }
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
