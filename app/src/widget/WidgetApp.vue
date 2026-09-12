<template>
  <div class="widget">
    <div class="widget-header">
      <div class="tabs">
        <span :class="{ active: tab === 'note' }" @click="switchTab('note')">快速记录</span>
        <span :class="{ active: tab === 'today' }" @click="switchTab('today')">今日计划</span>
      </div>
      <span class="actions">
        <span class="btn" title="打开主窗口" @click="openMain">⤢</span>
        <span class="btn" title="隐藏" @click="hide">✕</span>
      </span>
    </div>

    <template v-if="tab === 'note'">
      <el-input
        v-model="title"
        type="textarea"
        :rows="4"
        placeholder="记点什么，回车保存为草稿…"
        resize="none"
        @keydown.enter.exact.prevent="save"
      />
      <el-select v-model="tagIds" multiple filterable placeholder="标签（可选）" size="small" class="tag-select">
        <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
    </template>

    <template v-else>
      <div v-if="!schedules.length" class="empty">今天没有日程</div>
      <div v-else class="schedule-list">
        <div v-for="s in schedules" :key="s.id" class="s-item" :class="{ done: s.status === 'DONE' }">
          <el-checkbox
            :model-value="s.status === 'DONE'"
            size="small"
            @change="(v) => toggleDone(s, v)"
          />
          <span class="s-time">{{ fmtTime(s.startTime) }}</span>
          <span class="s-title">{{ s.title }}</span>
        </div>
      </div>
    </template>

    <div class="footer">
      <span class="hint">Ctrl+Shift+Space 显示 / 隐藏</span>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const tab = ref('note')
const title = ref('')
const tagIds = ref([])
const tags = ref([])
const schedules = ref([])
const saving = ref(false)

function fmtTime(t) {
  return t ? t.replace('T', ' ').slice(11, 16) : ''
}

function switchTab(t) {
  tab.value = t
  if (t === 'today') loadToday()
}

async function save() {
  if (!title.value.trim() || saving.value) return
  saving.value = true
  try {
    await api.post('/knowledge', {
      title: title.value.trim(),
      content: '',
      tagIds: tagIds.value,
      status: 'DRAFT',
    })
    ElMessage.success('已记录')
    title.value = ''
    tagIds.value = []
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function loadToday() {
  try {
    schedules.value = await api.get('/schedule/today')
  } catch (e) {
    /* 静默 */
  }
}

async function toggleDone(s, done) {
  try {
    await api.put('/schedule/' + s.id + '/status', { status: done ? 'DONE' : 'TODO' })
    s.status = done ? 'DONE' : 'TODO'
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function openMain() {
  window.kb?.openMain()
}

function hide() {
  window.kb?.hideWidget()
}

onMounted(async () => {
  try {
    tags.value = await api.get('/tag/list')
  } catch (e) {
    /* 后端未就绪时静默，等重试 */
  }
})
</script>

<style scoped>
.widget {
  height: 100vh;
  box-sizing: border-box;
  padding: 10px;
  background: #23272e;
  color: #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 10px;
  user-select: none;
}

.widget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  -webkit-app-region: drag;
}

.tabs {
  display: flex;
  gap: 12px;
  font-size: 13px;
}

.tabs span {
  cursor: pointer;
  color: #9ca3af;
  -webkit-app-region: no-drag;
}

.tabs span.active {
  color: #fff;
  font-weight: 600;
  border-bottom: 2px solid #409eff;
  padding-bottom: 2px;
}

.actions {
  -webkit-app-region: no-drag;
  display: flex;
  gap: 8px;
}

.btn {
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.btn:hover {
  background: #3a404a;
}

.tag-select {
  width: 100%;
}

.empty {
  color: #6b7280;
  font-size: 13px;
  text-align: center;
  padding: 30px 0;
}

.schedule-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.s-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #2b3038;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 13px;
}

.s-item.done .s-title {
  text-decoration: line-through;
  color: #6b7280;
}

.s-time {
  color: #9ca3af;
  font-family: Consolas, monospace;
  font-size: 12px;
  min-width: 42px;
}

.footer .hint {
  font-size: 11px;
  color: #6b7280;
}
</style>
