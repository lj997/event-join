<template>
  <div>
    <div class="page-header">
      <h2>{{ isEdit ? '编辑活动' : '创建活动' }}</h2>
    </div>

    <el-card>
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        style="max-width: 800px;"
      >
        <div class="form-section">
          <div class="form-section-title">基本信息</div>
          
          <el-form-item label="活动标题" prop="title">
            <el-input v-model="formData.title" placeholder="请输入活动标题" maxlength="100" />
          </el-form-item>

          <el-form-item label="活动描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="5"
              placeholder="请输入活动描述"
            />
          </el-form-item>

          <el-form-item label="活动地点" prop="location">
            <el-input v-model="formData.location" placeholder="请输入活动地点" maxlength="200" />
          </el-form-item>
        </div>

        <div class="form-section">
          <div class="form-section-title">时间安排</div>
          
          <el-form-item label="活动时间" prop="eventDateTime">
            <el-date-picker
              v-model="formData.eventDateTime"
              type="datetime"
              placeholder="选择活动时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%;"
            />
          </el-form-item>

          <el-form-item label="报名截止" prop="deadline">
            <el-date-picker
              v-model="formData.deadline"
              type="datetime"
              placeholder="选择报名截止时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%;"
            />
            <div style="color: #909399; font-size: 12px; margin-top: 5px;">
              报名截止时间必须早于活动时间
            </div>
          </el-form-item>
        </div>

        <div class="form-section">
          <div class="form-section-title">报名设置</div>
          
          <el-form-item label="人数上限" prop="maxParticipants">
            <el-input-number
              v-model="formData.maxParticipants"
              :min="1"
              :max="10000"
              placeholder="请输入人数上限"
              style="width: 200px;"
            />
          </el-form-item>

          <el-form-item label="封面图">
            <el-input v-model="formData.coverImage" placeholder="请输入封面图片URL（可选）" />
            <div style="color: #909399; font-size: 12px; margin-top: 5px;">
              支持输入图片URL地址
            </div>
          </el-form-item>
        </div>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建活动' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)

const isEdit = computed(() => route.path.includes('/edit/'))

const formData = reactive({
  title: '',
  description: '',
  location: '',
  eventDateTime: null,
  deadline: null,
  maxParticipants: 100,
  coverImage: ''
})

const validateDeadline = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请选择报名截止时间'))
  } else if (formData.eventDateTime && dayjs(value).isAfter(dayjs(formData.eventDateTime))) {
    callback(new Error('报名截止时间不能晚于活动时间'))
  } else {
    callback()
  }
}

const validateEventTime = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请选择活动时间'))
  } else if (dayjs(value).isBefore(dayjs())) {
    callback(new Error('活动时间必须是未来时间'))
  } else {
    callback()
  }
}

const rules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在2-100个字符', trigger: 'blur' }
  ],
  eventDateTime: [
    { required: true, validator: validateEventTime, trigger: 'change' }
  ],
  deadline: [
    { required: true, validator: validateDeadline, trigger: 'change' }
  ],
  maxParticipants: [
    { required: true, message: '请输入人数上限', trigger: 'blur' }
  ]
}

const fetchEventDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await request.get(`/admin/events/${route.params.id}`)
    const event = res.data
    
    formData.title = event.title
    formData.description = event.description
    formData.location = event.location
    formData.eventDateTime = event.eventDateTime
    formData.deadline = event.deadline
    formData.maxParticipants = event.maxParticipants
    formData.coverImage = event.coverImage
  } catch (error) {
    console.error('获取活动详情失败:', error)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = {
      ...formData,
      eventDateTime: dayjs(formData.eventDateTime).format('YYYY-MM-DDTHH:mm:ss'),
      deadline: dayjs(formData.deadline).format('YYYY-MM-DDTHH:mm:ss')
    }

    if (isEdit.value) {
      await request.put(`/admin/events/${route.params.id}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/events', data)
      ElMessage.success('创建成功')
    }
    
    router.push('/admin/events')
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchEventDetail()
})
</script>
