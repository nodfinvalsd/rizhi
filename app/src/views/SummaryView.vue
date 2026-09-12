<template>
  <div class="page">
    <div class="toolbar">
      <el-date-picker
        v-model="date"
        type="date"
        value-format="YYYY-MM-DD"
        :clearable="false"
        @change="onDateChange"
      />
      <span class="day-label">{{ dayLabel }}</span>
      <el-tag v-if="dirty" size="small" type="warning">未保存</el-tag>
      <div style="flex: 1"></div>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </div>

    <div class="summary-body" style="height: calc(100vh - 210px)">
      <MdEditor v-model="content" class="editor" />
      <div class="side">
        <div class="side-title">当日日程</div>
        <el-empty v-if="!schedules.length" description="当天没有日程" :image-size="52" />
        <div v-else class="s-list">
          <div
            v-for="s in schedules"
            :key="s.id"
            class="s-item"
            :class="{ done: s.status === 'DONE' }"
          >
            <span class="s-time">{{ fmtTime(s.startTime) }}</span>
            <span class="s-title">{{ s.title }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import api from '../api'

function pad(n) {
  return String(n).padStart(2, '0')
}

function fmtDate(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const date = ref(fmtDate(new Date()))
const loadedDate = ref(date.value)
const content = ref('')
const savedContent = ref('')
const saving = ref(false)
const schedules = ref([])

const dirty = computed(() => content.value !== savedContent.value)

const dayLabel = computed(() => {
  const today = new Date()
  const yesterday = new Date(today.getTime() - 86400000)
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][
    new Date(date.value + 'T00:00:00').getDay()
  ]
  if (date.value === fmtDate(today)) return '今天 · ' + week
  if (date.value === fmtDate(yesterday)) return '昨天 · ' + week
  return week
})

function fmtTime(t) {
  return t ? t.replace('T', ' ').slice(11, 16) : '全天'
}

async function load() {
  try {
    const [summary, list] = await Promise.all([
      api.get('/summary/day', { params: { date: date.value } }),
      api.get('/schedule/day', { params: { date: date.value } }),
    ])
    content.value = summary.content || ''
    savedContent.value = content.value
    schedules.value = list
    loadedDate.value = date.value
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function onDateChange() {
  if (dirty.value) {
    try {
      await ElMessageBox.confirm('当前内容还没保存，切换日期会丢失，继续吗？', '提示', {
        type: 'warning',
      })
    } catch (e) {
      date.value = loadedDate.value // 取消：把日期改回去
      return
    }
  }
  load()
}

async function save() {
  saving.value = true
  try {
    await api.post('/summary', { summaryDate: date.value, content: content.value })
    savedContent.value = content.value
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.day-label {
  font-size: 13px;
  color: #606266;
}

.summary-body {
  display: flex;
  gap: 12px;
  min-height: 0;
}

.editor {
  flex: 1;
  min-width: 0;
}

/* 工具栏按钮默认不换行，会把编辑器顶到很宽；允许换行后窄窗口也能用 */
.editor :deep(.md-editor-toolbar) {
  flex-wrap: wrap;
}

.side {
  width: 240px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  overflow-y: auto;
}

.side-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.s-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.s-item {
  display: flex;
  gap: 8px;
  align-items: baseline;
  font-size: 13px;
  padding: 6px 8px;
  background: #f7f8fa;
  border-radius: 6px;
}

.s-item.done .s-title {
  text-decoration: line-through;
  color: #b0b3b8;
}

.s-time {
  font-family: Consolas, monospace;
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.s-title {
  word-break: break-all;
}
</style>
