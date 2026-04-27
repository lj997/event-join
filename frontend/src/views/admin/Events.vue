<template>
  <div>
    <div class="page-header">
      <h2>活动管理</h2>
      <el-button type="primary" @click="createEvent">
        <el-icon><Plus /></el-icon>
        新建活动
      </el-button>
    </div>

    <div class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索活动标题" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="开放报名" value="OPEN" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchEvents">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-loading="loading">
      <el-table :data="events" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="活动名称" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row.id)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="150">
          <template #default="{ row }">
            {{ row.location || '待定' }}
          </template>
        </el-table-column>
        <el-table-column prop="eventDateTime" label="活动时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.eventDateTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="报名截止" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.deadline) }}
          </template>
        </el-table-column>
        <el-table-column prop="currentParticipants" label="报名人数" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.currentParticipants >= row.maxParticipants ? '#f56c6c' : '#67c23a' }">
              {{ row.currentParticipants }}/{{ row.maxParticipants }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'OPEN' ? 'success' : 'info'" size="small">
              {{ row.status === 'OPEN' ? '开放' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewRegistrations(row.id)">
              报名列表
            </el-button>
            <el-button type="primary" link size="small" @click="editEvent(row.id)">
              编辑
            </el-button>
            <el-button 
              v-if="row.status === 'OPEN'" 
              type="danger" 
              link 
              size="small"
              @click="closeEvent(row)"
            >
              关闭
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
          @size-change="fetchEvents"
          @current-change="fetchEvents"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const events = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  status: null
})

const formatDateTime = (dt) => {
  if (!dt) return ''
  return dayjs(dt).format('YYYY-MM-DD HH:mm')
}

const fetchEvents = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/events', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    
    let list = res.data.records || []
    
    if (searchForm.keyword) {
      list = list.filter(e => e.title.includes(searchForm.keyword))
    }
    if (searchForm.status) {
      list = list.filter(e => e.status === searchForm.status)
    }
    
    events.value = list
    total.value = res.data.total
  } catch (error) {
    console.error('获取活动列表失败:', error)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = null
  fetchEvents()
}

const createEvent = () => {
  router.push('/admin/events/create')
}

const editEvent = (id) => {
  router.push(`/admin/events/edit/${id}`)
}

const viewDetail = (id) => {
  router.push(`/events/${id}`)
}

const viewRegistrations = (id) => {
  router.push(`/admin/events/${id}/registrations`)
}

const closeEvent = async (row) => {
  try {
    await ElMessageBox.confirm('确定要关闭此活动吗？关闭后将无法报名。', '确认关闭', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request.put(`/admin/events/${row.id}/close`)
    ElMessage.success('活动已关闭')
    fetchEvents()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭活动失败:', error)
    }
  }
}

onMounted(() => {
  fetchEvents()
})
</script>
