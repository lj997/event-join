<template>
  <div>
    <el-header style="background: #409eff; color: white;">
      <div class="container flex justify-between align-center" style="height: 60px;">
        <div class="flex align-center gap-20">
          <router-link to="/" style="font-size: 20px; font-weight: bold; color: white;">
            活动发布与报名系统
          </router-link>
        </div>
        <div class="flex align-center gap-20">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/notifications" style="color: white; position: relative;">
              <el-icon size="20"><Bell /></el-icon>
              <span v-if="userStore.unreadCount > 0" class="notification-badge">
                {{ userStore.unreadCount > 99 ? '99+' : userStore.unreadCount }}
              </span>
            </router-link>
            <router-link to="/my-registrations" style="color: white;">我的报名</router-link>
            <template v-if="userStore.isAdmin">
              <router-link to="/admin" style="color: white;">管理后台</router-link>
            </template>
            <el-dropdown @command="handleCommand">
              <span class="flex align-center" style="cursor: pointer;">
                <el-icon><UserFilled /></el-icon>
                <span style="margin-left: 5px;">{{ userStore.user?.username }}</span>
                <el-icon style="margin-left: 5px;"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" style="color: white;">登录</router-link>
            <router-link to="/register" style="color: white;">注册</router-link>
          </template>
        </div>
      </div>
    </el-header>

    <el-main style="padding: 20px;">
      <div class="container">
        <div v-if="upcomingEvents.length > 0" class="upcoming-section">
          <div class="upcoming-title">
            <el-icon><Warning /></el-icon>
            即将开始的活动
          </div>
          <div v-for="event in upcomingEvents" :key="event.id" class="upcoming-item flex justify-between align-center">
            <div>
              <strong style="font-size: 16px;">{{ event.title }}</strong>
              <div style="color: #909399; font-size: 14px; margin-top: 5px;">
                <el-icon><Location /></el-icon> {{ event.location || '待定' }}
                <span style="margin-left: 20px;">
                  <el-icon><Clock /></el-icon> {{ formatDateTime(event.eventDateTime) }}
                </span>
              </div>
            </div>
            <el-button type="warning" size="small" @click="goEventDetail(event.id)">
              查看详情
            </el-button>
          </div>
        </div>

        <div class="search-form">
          <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="searchForm.keyword" placeholder="搜索活动标题或描述" clearable @clear="handleSearch" />
            </el-form-item>
            <el-form-item label="地点">
              <el-input v-model="searchForm.location" placeholder="搜索地点" clearable @clear="handleSearch" />
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                @change="handleSearch"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-loading="loading">
          <div v-if="events.length === 0" class="text-center" style="padding: 60px 0;">
            <el-empty description="暂无活动" />
          </div>
          <div v-else class="flex flex-wrap" style="gap: 20px;">
            <el-card
              v-for="event in events"
              :key="event.id"
              class="event-card"
              style="width: calc(33.333% - 14px); cursor: pointer;"
              @click="goEventDetail(event.id)"
            >
              <template #header>
                <div class="flex justify-between align-center">
                  <span style="font-weight: bold; font-size: 16px;">{{ event.title }}</span>
                  <span :class="['status-tag', event.status === 'OPEN' ? 'status-open' : 'status-closed']">
                    {{ event.status === 'OPEN' ? '开放报名' : '已关闭' }}
                  </span>
                </div>
              </template>
              <div class="event-info">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDateTime(event.eventDateTime) }}</span>
              </div>
              <div class="event-info">
                <el-icon><Location /></el-icon>
                <span>{{ event.location || '待定' }}</span>
              </div>
              <div class="event-info">
                <el-icon><User /></el-icon>
                <span>报名：{{ event.currentParticipants }}/{{ event.maxParticipants }}</span>
              </div>
              <el-progress
                :percentage="Math.round((event.currentParticipants / event.maxParticipants) * 100)"
                :stroke-width="8"
                style="margin-top: 15px;"
              />
            </el-card>
          </div>
        </div>

        <div class="text-center mt-20">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchEvents"
            @current-change="fetchEvents"
          />
        </div>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const events = ref([])
const upcomingEvents = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  location: '',
  dateRange: null
})

const formatDateTime = (dt) => {
  if (!dt) return ''
  return dayjs(dt).format('YYYY-MM-DD HH:mm')
}

const fetchEvents = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.location) params.location = searchForm.location
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const res = await request.get('/events/public/search', { params })
    events.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取活动列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchUpcomingEvents = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await request.get('/notifications/upcoming')
    upcomingEvents.value = res.data
  } catch (error) {
    console.error('获取即将开始的活动失败:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchEvents()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.location = ''
  searchForm.dateRange = null
  currentPage.value = 1
  fetchEvents()
}

const goEventDetail = (id) => {
  router.push(`/events/${id}`)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/')
  }
}

onMounted(() => {
  fetchEvents()
  if (userStore.isLoggedIn) {
    fetchUpcomingEvents()
    userStore.fetchUnreadCount()
  }
})
</script>
