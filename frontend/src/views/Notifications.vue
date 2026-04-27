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
          <router-link to="/" style="color: white;">首页</router-link>
          <router-link to="/my-registrations" style="color: white;">我的报名</router-link>
          <template v-if="userStore.isAdmin">
            <router-link to="/admin" style="color: white;">管理后台</router-link>
          </template>
          <span>{{ userStore.user?.username }}</span>
        </div>
      </div>
    </el-header>

    <el-main style="padding: 20px;">
      <div class="container">
        <div class="page-header">
          <h2>我的通知</h2>
          <el-button type="primary" link @click="markAllRead" v-if="unreadCount > 0">
            全部标记为已读
          </el-button>
        </div>

        <div v-loading="loading">
          <el-empty v-if="notifications.length === 0" description="暂无通知" />

          <div v-else class="flex flex-wrap" style="gap: 15px;">
            <el-card
              v-for="notification in notifications"
              :key="notification.id"
              class="event-card"
              style="width: 100%;"
              :class="{ 'is-unread': !notification.isRead }"
            >
              <div class="flex justify-between align-start">
                <div style="flex: 1;">
                  <div class="flex align-center gap-10">
                    <el-icon v-if="!notification.isRead" color="#409eff" size="12">
                      <CircleCheck />
                    </el-icon>
                    <h4 :style="{ fontWeight: notification.isRead ? 'normal' : 'bold' }">
                      {{ notification.title }}
                    </h4>
                  </div>
                  <p style="white-space: pre-wrap; margin-top: 10px; color: #606266;">
                    {{ notification.content }}
                  </p>
                  <div class="flex align-center gap-20" style="margin-top: 15px; color: #909399; font-size: 14px;">
                    <span>
                      <el-icon><Clock /></el-icon>
                      {{ formatDateTime(notification.createdAt) }}
                    </span>
                    <template v-if="notification.eventId">
                      <el-link type="primary" @click="goEventDetail(notification.eventId)">
                        查看活动
                      </el-link>
                    </template>
                  </div>
                </div>
                <div>
                  <el-button 
                    type="primary" 
                    link 
                    size="small"
                    v-if="!notification.isRead"
                    @click="markAsRead(notification)"
                  >
                    标记已读
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>

          <div class="text-center mt-20">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchNotifications"
              @current-change="fetchNotifications"
            />
          </div>
        </div>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const notifications = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const unreadCount = ref(0)

const formatDateTime = (dt) => {
  if (!dt) return ''
  return dayjs(dt).format('YYYY-MM-DD HH:mm')
}

const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await request.get('/notifications', {
      params: { page: currentPage.value, size: pageSize.value }
    })
    notifications.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取通知失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await request.get('/notifications/unread/count')
    unreadCount.value = res.data.count
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

const markAsRead = async (notification) => {
  try {
    await request.put(`/notifications/${notification.id}/read`)
    notification.isRead = true
    unreadCount.value--
    userStore.fetchUnreadCount()
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

const markAllRead = async () => {
  try {
    await request.put('/notifications/read-all')
    notifications.value.forEach(n => n.isRead = true)
    unreadCount.value = 0
    userStore.fetchUnreadCount()
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('全部标记已读失败:', error)
  }
}

const goEventDetail = (id) => {
  router.push(`/events/${id}`)
}

onMounted(() => {
  fetchNotifications()
  fetchUnreadCount()
})
</script>

<style scoped>
.is-unread {
  background: linear-gradient(90deg, #ecf5ff 0%, #ffffff 100%);
  border-left: 4px solid #409eff;
}
</style>
