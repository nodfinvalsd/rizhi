<template>
  <div class="page" v-if="d">
    <div class="toolbar">
      <el-button @click="router.back()">← 返回</el-button>
      <div style="flex: 1"></div>
      <el-button :type="d.favorite ? 'warning' : 'default'" @click="toggleFavorite">
        {{ d.favorite ? '★ 取消收藏' : '☆ 收藏' }}
      </el-button>
      <el-button @click="exportMd">导出 .md</el-button>
      <el-button @click="router.push('/edit/' + d.id)">编辑</el-button>
      <el-button type="danger" @click="remove">删除</el-button>
    </div>

    <h1 style="margin: 8px 0">{{ d.title }}</h1>
    <div style="color: #909399; margin-bottom: 16px">
      分类：{{ d.categoryName || '-' }} ｜
      <el-tag v-for="t in d.tags" :key="t.id" size="small" style="margin-right: 4px">{{ t.name }}</el-tag>
      ｜ 创建：{{ fmt(d.createTime) }} ｜ 更新：{{ fmt(d.updateTime) }}
    </div>

    <MdPreview :model-value="d.content || ''" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import api, { BASE_URL } from '../api'

const route = useRoute()
const router = useRouter()
const d = ref(null)

function fmt(t) {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

async function toggleFavorite() {
  try {
    if (d.value.favorite) {
      await api.delete('/knowledge/' + d.value.id + '/favorite')
      d.value.favorite = false
    } else {
      await api.post('/knowledge/' + d.value.id + '/favorite')
      d.value.favorite = true
    }
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function exportMd() {
  window.open(BASE_URL + '/api/knowledge/' + d.value.id + '/export')
}

async function remove() {
  try {
    await ElMessageBox.confirm(`删除「${d.value.title}」？`, '确认', { type: 'warning' })
    await api.delete('/knowledge/' + d.value.id)
    ElMessage.success('已删除')
    router.push('/')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message)
  }
}

onMounted(async () => {
  d.value = await api.get('/knowledge/' + route.params.id)
  document.title = d.value.title + ' - 个人知识库'
})
</script>
