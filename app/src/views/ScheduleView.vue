<template>
  <div class="page">
    <div class="toolbar">
      <el-radio-group v-model="tab" @change="load">
        <el-radio-button value="today">今日</el-radio-button>
        <el-radio-button value="tomorrow">明日</el-radio-button>
        <el-radio-button value="week">本周</el-radio-button>
        <el-radio-button value="all">全部</el-radio-button>
      </el-radio-group>
      <div style="flex: 1"></div>
      <el-button type="primary" @click="openCreate">新建日程</el-button>
    </div>

    <el-empty v-if="!list.length" description="暂无日程" />
    <div v-else class="schedule-list">
      <div v-for="s in list" :key="s.id" class="schedule-item" :class="{ done: s.status === 'DONE' }">
        <el-checkbox
          :model-value="s.status === 'DONE'"
          @change="(v) => toggleDone(s, v)"
        />
        <span class="time">{{ fmtTime(s.startTime) }}<template v-if="s.endTime"> - {{ fmtTime(s.endTime) }}</template></span>
        <el-tag size="small" :type="prioType(s.priority)">{{ prioText(s.priority) }}</el-tag>
        <span class="title">{{ s.title }}</span>
        <div style="flex: 1"></div>
        <el-button link size="small" @click="openEdit(s)">编辑</el-button>
        <el-button link size="small" type="danger" @click="remove(s)">删除</el-button>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑日程' : '新建日程'" width="460px">
      <el-form label-width="70px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="要做什么" />
        </el-form-item>
        <el-form-item label="开始">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DD[T]HH:mm:ss"
            placeholder="开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DD[T]HH:mm:ss"
            placeholder="结束时间（可选）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const tab = ref('today')
const list = ref([])
const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)

const form = reactive({ title: '', description: '', startTime: null, endTime: null, priority: 'MEDIUM' })

function fmtTime(t) {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

function prioText(p) {
  return { HIGH: '高', MEDIUM: '中', LOW: '低' }[p] || '中'
}

function prioType(p) {
  return { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }[p] || 'warning'
}

async function load() {
  try {
    list.value = await api.get('/schedule/' + tab.value)
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function openCreate() {
  editing.value = null
  Object.assign(form, { title: '', description: '', startTime: null, endTime: null, priority: 'MEDIUM' })
  dialogVisible.value = true
}

function openEdit(s) {
  editing.value = s
  Object.assign(form, {
    title: s.title,
    description: s.description,
    startTime: s.startTime,
    endTime: s.endTime,
    priority: s.priority,
  })
  dialogVisible.value = true
}

async function save() {
  if (!form.title.trim()) {
    ElMessage.warning('标题不能为空')
    return
  }
  if (!form.startTime) {
    ElMessage.warning('请选择开始时间')
    return
  }
  saving.value = true
  try {
    // 只发本地时间字符串（YYYY-MM-DDTHH:mm:ss）；Date 对象会被 JSON 序列化成 UTC，导致时区偏移
    const payload = {
      ...form,
      startTime: form.startTime || null,
      endTime: form.endTime || null,
    }
    if (editing.value) {
      await api.put('/schedule/' + editing.value.id, payload)
    } else {
      await api.post('/schedule', payload)
    }
    dialogVisible.value = false
    ElMessage.success('已保存')
    load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
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

async function remove(s) {
  try {
    await ElMessageBox.confirm(`删除日程「${s.title}」？`, '确认', { type: 'warning' })
    await api.delete('/schedule/' + s.id)
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message)
  }
}

onMounted(load)
</script>

<style scoped>
.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.schedule-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.schedule-item.done .title {
  text-decoration: line-through;
  color: #b0b3b8;
}

.time {
  font-family: Consolas, monospace;
  color: #606266;
  min-width: 140px;
}

.title {
  font-size: 14px;
}
</style>
