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
            <router-link to="/my-registrations" style="color: white;">我的报名</router-link>
            <template v-if="userStore.isAdmin">
              <router-link to="/admin" style="color: white;">管理后台</router-link>
            </template>
            <span>{{ userStore.user?.username }}</span>
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
        <el-button type="primary" link @click="$router.back()" style="margin-bottom: 20px;">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </el-button>

        <div v-loading="loading">
          <el-card>
            <div class="page-header">
              <h2>{{ event.title }}</h2>
              <el-tag :type="event.status === 'OPEN' ? 'success' : 'info'" size="large">
                {{ event.status === 'OPEN' ? '开放报名' : '已关闭' }}
              </el-tag>
            </div>

            <el-divider />

            <div class="flex" style="gap: 30px;">
              <div style="width: 60%;">
                <h4 style="margin-bottom: 15px;">活动描述</h4>
                <p style="white-space: pre-wrap; line-height: 1.8;">
                  {{ event.description || '暂无描述' }}
                </p>

                <el-divider />

                <div class="flex flex-wrap" style="gap: 20px;">
                  <div style="width: calc(50% - 10px);">
                    <el-descriptions title="活动信息" :column="1" border>
                      <el-descriptions-item label="活动时间">
                        <el-icon><Calendar /></el-icon>
                        {{ formatDateTime(event.eventDateTime) }}
                      </el-descriptions-item>
                      <el-descriptions-item label="报名截止">
                        <el-icon><Timer /></el-icon>
                        {{ formatDateTime(event.deadline) }}
                      </el-descriptions-item>
                      <el-descriptions-item label="活动地点">
                        <el-icon><Location /></el-icon>
                        {{ event.location || '待定' }}
                      </el-descriptions-item>
                    </el-descriptions>
                  </div>
                  <div style="width: calc(50% - 10px);">
                    <el-descriptions title="报名信息" :column="1" border>
                      <el-descriptions-item label="报名人数">
                        <el-icon><User /></el-icon>
                        <span style="font-size: 18px; font-weight: bold; color: #409eff;">
                          {{ event.currentParticipants }}
                        </span>
                        <span style="color: #909399;"> / {{ event.maxParticipants }}</span>
                      </el-descriptions-item>
                      <el-descriptions-item label="报名状态">
                        <template v-if="event.status !== 'OPEN'">
                          <el-tag type="info">活动已关闭</el-tag>
                        </template>
                        <template v-else-if="isDeadlinePassed">
                          <el-tag type="danger">报名已截止</el-tag>
                        </template>
                        <template v-else-if="event.currentParticipants >= event.maxParticipants">
                          <el-tag type="warning">人数已满</el-tag>
                        </template>
                        <template v-else>
                          <el-tag type="success">报名中</el-tag>
                        </template>
                      </el-descriptions-item>
                    </el-descriptions>
                  </div>
                </div>

                <el-progress
                  :percentage="Math.round((event.currentParticipants / event.maxParticipants) * 100)"
                  :stroke-width="10"
                  style="margin-top: 20px;"
                />
              </div>

              <div style="width: 40%;">
                <el-card>
                  <template #header>
                    <span style="font-weight: bold;">报名操作</span>
                  </template>

                  <template v-if="!userStore.isLoggedIn">
                    <el-empty description="请先登录后再报名" />
                    <el-button type="primary" style="width: 100%; margin-top: 15px;" @click="goLogin">
                      立即登录
                    </el-button>
                  </template>

                  <template v-else>
                    <div v-if="isRegistered">
                      <el-alert title="您已报名此活动" type="success" show-icon style="margin-bottom: 15px;" />
                      <el-button 
                        type="danger" 
                        style="width: 100%;" 
                        :loading="canceling"
                        @click="handleCancel"
                        :disabled="event.eventDateTime && dayjs().isAfter(dayjs(event.eventDateTime))"
                      >
                        取消报名
                      </el-button>
                      <p v-if="event.eventDateTime && dayjs().isAfter(dayjs(event.eventDateTime))" 
                         style="color: #909399; font-size: 12px; margin-top: 10px;">
                        活动已开始，无法取消报名
                      </p>
                    </div>

                    <div v-else>
                      <el-alert 
                        v-if="canRegister" 
                        title="您还没有报名此活动" 
                        type="info" 
                        show-icon 
                        style="margin-bottom: 15px;" 
                      />
                      <el-alert 
                        v-else 
                        title="无法报名" 
                        type="warning" 
                        show-icon 
                        style="margin-bottom: 15px;" 
                      >
                        <template #default>
                          <p v-if="event.status !== 'OPEN'">活动已关闭</p>
                          <p v-else-if="isDeadlinePassed">报名已截止</p>
                          <p v-else-if="event.currentParticipants >= event.maxParticipants">人数已满</p>
                        </template>
                      </el-alert>
                      <el-button 
                        type="primary" 
                        style="width: 100%;" 
                        :loading="registering"
                        :disabled="!canRegister"
                        @click="handleRegister"
                      >
                        立即报名
                      </el-button>
                    </div>
                  </template>
                </el-card>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const registering = ref(false)
const canceling = ref(false)
const event = ref({})
const isRegistered = ref(false)

const isDeadlinePassed = computed(() => {
  if (!event.value.deadline) return false
  return dayjs().isAfter(dayjs(event.value.deadline))
})

const canRegister = computed(() => {
  return event.value.status === 'OPEN' &&
         !isDeadlinePassed.value &&
         (event.value.currentParticipants || 0) < (event.value.maxParticipants || 0)
})

const formatDateTime = (dt) => {
  if (!dt) return ''
  return dayjs(dt).format('YYYY-MM-DD HH:mm')
}

const fetchEventDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/events/public/${route.params.id}`)
    event.value = res.data
  } catch (error) {
    console.error('获取活动详情失败:', error)
  } finally {
    loading.value = false
  }
}

const checkRegistration = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await request.get(`/registrations/check/${route.params.id}`)
    isRegistered.value = res.data
  } catch (error) {
    console.error('检查报名状态失败:', error)
  }
}

const handleRegister = async () => {
  try {
    await ElMessageBox.confirm('确定要报名此活动吗？', '确认报名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    registering.value = true
    await request.post(`/registrations/${route.params.id}`)
    ElMessage.success('报名成功！')
    isRegistered.value = true
    event.value.currentParticipants = (event.value.currentParticipants || 0) + 1
  } catch (error) {
    if (error !== 'cancel') {
      console.error('报名失败:', error)
    }
  } finally {
    registering.value = false
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消报名吗？', '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    canceling.value = true
    await request.delete(`/registrations/${route.params.id}`)
    ElMessage.success('取消报名成功！')
    isRegistered.value = false
    event.value.currentParticipants = (event.value.currentParticipants || 0) - 1
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消报名失败:', error)
    }
  } finally {
    canceling.value = false
  }
}

const goLogin = () => {
  router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
}

onMounted(() => {
  fetchEventDetail()
  if (userStore.isLoggedIn) {
    checkRegistration()
  }
})
</script>
