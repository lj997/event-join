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
          <router-link to="/notifications" style="color: white;">通知</router-link>
          <template v-if="userStore.isAdmin">
            <router-link to="/admin" style="color: white;">管理后台</router-link>
          </template>
          <span>{{ userStore.user?.username }}</span>
        </div>
      </div>
    </el-header>

    <el-main style="padding: 20px;">
      <div class="container">
        <h2 style="margin-bottom: 20px;">我的报名</h2>

        <div v-loading="loading">
          <el-empty v-if="registrations.length === 0" description="暂无报名记录" />

          <el-table v-else :data="registrations" stripe>
            <el-table-column prop="eventTitle" label="活动名称" min-width="200">
              <template #default="{ row }">
                <el-link type="primary" @click="goEventDetail(row.eventId)">
                  {{ row.eventTitle }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="eventLocation" label="活动地点" min-width="150">
              <template #default="{ row }">
                <el-icon><Location /></el-icon>
                {{ row.eventLocation || '待定' }}
              </template>
            </el-table-column>
            <el-table-column prop="eventDateTime" label="活动时间" min-width="180">
              <template #default="{ row }">
                <el-icon><Calendar /></el-icon>
                {{ formatDateTime(row.eventDateTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="registeredAt" label="报名时间" min-width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.registeredAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="isCancelled" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isCancelled ? 'danger' : 'success'">
                  {{ row.isCancelled ? '已取消' : '已报名' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button 
                  type="danger" 
                  size="small" 
                  :disabled="row.isCancelled || isEventPassed(row.eventDateTime)"
                  @click="handleCancel(row)"
                >
                  取消报名
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="text-center mt-20">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchRegistrations"
              @current-change="fetchRegistrations"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const registrations = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatDateTime = (dt) => {
  if (!dt) return ''
  return dayjs(dt).format('YYYY-MM-DD HH:mm')
}

const isEventPassed = (dt) => {
  if (!dt) return false
  return dayjs().isAfter(dayjs(dt))
}

const fetchRegistrations = async () => {
  loading.value = true
  try {
    const res = await request.get('/registrations/my', {
      params: { page: currentPage.value, size: pageSize.value }
    })
    registrations.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取报名记录失败:', error)
  } finally {
    loading.value = false
  }
}

const goEventDetail = (id) => {
  router.push(`/events/${id}`)
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消报名吗？', '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request.delete(`/registrations/${row.eventId}`)
    ElMessage.success('取消报名成功！')
    fetchRegistrations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消报名失败:', error)
    }
  }
}

onMounted(() => {
  fetchRegistrations()
})
</script>
