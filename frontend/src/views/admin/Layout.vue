<template>
  <el-container style="min-height: 100vh;">
    <el-aside width="220px" style="background: #304156;">
      <div style="height: 60px; display: flex; align-items: center; justify-content: center; color: white; font-size: 18px; font-weight: bold; border-bottom: 1px solid #1f2d3d;">
        <el-icon size="24"><OfficeBuilding /></el-icon>
        <span style="margin-left: 10px;">管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/admin">
          <el-icon><DataBoard /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/admin/events">
          <el-icon><Document /></el-icon>
          <span>活动管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="background: white; border-bottom: 1px solid #e6e6e6; display: flex; justify-content: space-between; align-items: center; padding: 0 20px;">
        <div>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>
              <el-link type="primary" @click="$router.push('/')">首页</el-link>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              <template v-if="item.path">
                <el-link type="primary" @click="$router.push(item.path)">{{ item.name }}</el-link>
              </template>
              <template v-else>
                {{ item.name }}
              </template>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="flex align-center gap-20">
          <el-dropdown @command="handleCommand">
            <span class="flex align-center" style="cursor: pointer;">
              <el-icon><UserFilled /></el-icon>
              <span style="margin-left: 5px;">{{ userStore.user?.username }}</span>
              <el-icon style="margin-left: 5px;"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">返回前台</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main style="background: #f5f7fa;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  if (route.path === '/admin' || route.path === '/admin/') {
    return '/admin'
  }
  if (route.path.startsWith('/admin/events')) {
    return '/admin/events'
  }
  return route.path
})

const breadcrumbs = computed(() => {
  const items = []
  if (route.path === '/admin' || route.path === '/admin/') {
    items.push({ name: '数据概览' })
  } else if (route.path === '/admin/events') {
    items.push({ name: '活动管理' })
  } else if (route.path === '/admin/events/create') {
    items.push({ name: '活动管理', path: '/admin/events' })
    items.push({ name: '创建活动' })
  } else if (route.path.includes('/admin/events/edit/')) {
    items.push({ name: '活动管理', path: '/admin/events' })
    items.push({ name: '编辑活动' })
  } else if (route.path.includes('/admin/events/') && route.path.includes('/registrations')) {
    items.push({ name: '活动管理', path: '/admin/events' })
    items.push({ name: '报名列表' })
  }
  return items
})

const handleCommand = (command) => {
  if (command === 'home') {
    router.push('/')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>
