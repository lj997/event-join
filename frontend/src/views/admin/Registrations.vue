<template>
  <div>
    <div class="page-header">
      <div>
        <el-button type="primary" link @click="$router.back()" style="margin-bottom: 10px; padding: 0;">
          <el-icon><ArrowLeft /></el-icon> 返回活动列表
        </el-button>
        <h2>报名列表</h2>
        <p style="color: #909399; margin-top: 5px;">
          活动：<strong>{{ event.title }}</strong>
          &nbsp;&nbsp;|&nbsp;&nbsp;
          已报名：<strong style="color: #409eff;">{{ event.currentParticipants }}/{{ event.maxParticipants }}</strong> 人
        </p>
      </div>
      <el-button type="success" @click="exportExcel">
        <el-icon><Download /></el-icon>
        导出Excel
      </el-button>
    </div>

    <el-card>
      <div v-loading="loading">
        <el-empty v-if="registrations.length === 0" description="暂无报名记录" />

        <el-table v-else :data="registrations" stripe class="registration-list">
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="email" label="邮箱" min-width="180">
            <template #default="{ row }">
              {{ row.email || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="130">
            <template #default="{ row }">
              {{ row.phone || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="registeredAt" label="报名时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.registeredAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="isCancelled" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isCancelled ? 'danger' : 'success'" size="small">
                {{ row.isCancelled ? '已取消' : '已报名' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'

const route = useRoute()

const loading = ref(false)
const registrations = ref([])
const event = ref({
  title: '',
  currentParticipants: 0,
  maxParticipants: 0
})

const formatDateTime = (dt) => {
  if (!dt) return ''
  return dayjs(dt).format('YYYY-MM-DD HH:mm')
}

const fetchEventDetail = async () => {
  try {
    const res = await request.get(`/admin/events/${route.params.id}`)
    event.value = res.data
  } catch (error) {
    console.error('获取活动详情失败:', error)
  }
}

const fetchRegistrations = async () => {
  loading.value = true
  try {
    const res = await request.get(`/admin/registrations/event/${route.params.id}`)
    registrations.value = res.data
  } catch (error) {
    console.error('获取报名列表失败:', error)
  } finally {
    loading.value = false
  }
}

const exportExcel = async () => {
  try {
    const res = await request.get(`/admin/export/registrations/${route.params.id}`, {
      responseType: 'blob'
    })
    
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${event.value.title}_报名列表_${dayjs().format('YYYYMMDD')}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
  }
}

onMounted(() => {
  fetchEventDetail()
  fetchRegistrations()
})
</script>
