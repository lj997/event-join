<template>
  <div>
    <h2 style="margin-bottom: 20px;">数据概览</h2>

    <el-row :gutter="20" style="margin-bottom: 30px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-number">{{ stats.totalEvents || 0 }}</div>
            <div class="stat-label">活动总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-number" style="color: #67c23a;">{{ stats.openEvents || 0 }}</div>
            <div class="stat-label">开放活动</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-number" style="color: #e6a23c;">{{ stats.totalRegistrations || 0 }}</div>
            <div class="stat-label">报名总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-number" style="color: #909399;">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">注册用户</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span style="font-weight: bold;">最近活动</span>
          </template>
          <el-table :data="recentEvents" stripe>
            <el-table-column prop="title" label="活动名称" min-width="150">
              <template #default="{ row }">
                <el-link type="primary" @click="goEventDetail(row.id)">
                  {{ row.title }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="eventDateTime" label="活动时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.eventDateTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="currentParticipants" label="报名情况" width="120">
              <template #default="{ row }">
                {{ row.currentParticipants }}/{{ row.maxParticipants }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'OPEN' ? 'success' : 'info'" size="small">
                  {{ row.status === 'OPEN' ? '开放' : '关闭' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span style="font-weight: bold;">活动报名趋势</span>
          </template>
          <div ref="chartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const chartRef = ref(null)

const stats = ref({
  totalEvents: 0,
  openEvents: 0,
  totalRegistrations: 0,
  totalUsers: 0
})

const recentEvents = ref([])

const formatDateTime = (dt) => {
  if (!dt) return ''
  return dayjs(dt).format('YYYY-MM-DD HH:mm')
}

const fetchStats = async () => {
  try {
    const eventsRes = await request.get('/admin/events', { params: { page: 1, size: 1000 } })
    const events = eventsRes.data.records || []
    
    stats.value.totalEvents = eventsRes.data.total
    stats.value.openEvents = events.filter(e => e.status === 'OPEN').length
    
    let totalReg = 0
    events.forEach(e => {
      totalReg += e.currentParticipants || 0
    })
    stats.value.totalRegistrations = totalReg
    
    recentEvents.value = events.slice(0, 5)
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const initChart = () => {
  if (!chartRef.value) return
  
  const chart = echarts.init(chartRef.value)
  
  const dates = []
  for (let i = 6; i >= 0; i--) {
    dates.push(dayjs().subtract(i, 'day').format('MM-DD'))
  }
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '报名人数',
        type: 'line',
        smooth: true,
        data: [3, 5, 2, 8, 4, 6, 3],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      }
    ]
  }
  
  chart.setOption(option)
  
  window.addEventListener('resize', () => chart.resize())
}

const goEventDetail = (id) => {
  router.push(`/events/${id}`)
}

onMounted(async () => {
  await fetchStats()
  await nextTick()
  initChart()
})
</script>
