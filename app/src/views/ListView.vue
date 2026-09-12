<template>
  <div class="page">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索标题 / 正文"
        clearable
        style="width: 240px"
        @keyup.enter="load(1)"
        @clear="load(1)"
      />
      <el-tree-select
        v-model="query.categoryId"
        :data="categoryOptions"
        check-strictly
        clearable
        placeholder="分类"
        style="width: 180px"
        @change="load(1)"
      />
      <el-select v-model="query.tagId" clearable placeholder="标签" style="width: 140px" @change="load(1)">
        <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-radio-group v-model="tab" @change="load(1)">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="fav">收藏</el-radio-button>
      </el-radio-group>
      <div style="flex: 1"></div>
      <el-button @click="catDialogVisible = true">分类管理</el-button>
      <el-button @click="exportAll">全库导出</el-button>
      <el-button type="primary" @click="router.push('/edit')">新建</el-button>
    </div>

    <el-table :data="items" style="cursor: pointer" @row-click="open">
      <el-table-column label="标题" min-width="300">
        <template #default="{ row }">
          <span :class="{ 'draft-title': row.status === 'DRAFT' }">
            {{ row.status === 'DRAFT' ? '[草稿] ' : '' }}{{ row.title }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="140">
        <template #default="{ row }">{{ row.categoryName || '-' }}</template>
      </el-table-column>
      <el-table-column label="标签" width="220">
        <template #default="{ row }">
          <el-tag v-for="t in row.tags" :key="t.id" size="small" style="margin-right: 4px">{{ t.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="160">
        <template #default="{ row }">{{ fmt(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            link
            :type="row.favorite ? 'warning' : 'default'"
            @click.stop="toggleFavorite(row)"
          >
            {{ row.favorite ? '★ 已收藏' : '☆ 收藏' }}
          </el-button>
          <el-button link type="danger" @click.stop="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      :page-size="query.size"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 12px; justify-content: flex-end"
      @current-change="load()"
    />

    <el-dialog v-model="catDialogVisible" title="分类管理" width="420px">
      <CategoryManager @changed="loadMeta" />
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import CategoryManager from '../components/CategoryManager.vue'
import api, { BASE_URL } from '../api'

const router = useRouter()
const items = ref([])
const total = ref(0)
const tags = ref([])
const categoryTree = ref([])
const categoryOptions = ref([])
const tab = ref('all')
const catDialogVisible = ref(false)

const query = reactive({ keyword: '', categoryId: null, tagId: null, page: 1, size: 20 })

function fmt(t) {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

function toOptions(nodes) {
  return nodes.map((n) => ({
    label: n.name,
    value: n.id,
    children: n.children && n.children.length ? toOptions(n.children) : undefined,
  }))
}

async function load(page) {
  if (page) query.page = page
  try {
    if (tab.value === 'fav') {
      const data = await api.get('/favorite/page', {
        params: { page: query.page, size: query.size },
      })
      items.value = data.records
      total.value = data.total
    } else {
      const data = await api.get('/knowledge/page', { params: query })
      items.value = data.records
      total.value = data.total
    }
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function loadMeta() {
  const [t, c] = await Promise.all([api.get('/tag/list'), api.get('/category/tree')])
  tags.value = t
  categoryTree.value = c
  categoryOptions.value = toOptions(c)
}

function open(row) {
  router.push('/detail/' + row.id)
}

async function toggleFavorite(row) {
  try {
    if (row.favorite) {
      await api.delete('/knowledge/' + row.id + '/favorite')
      row.favorite = false
    } else {
      await api.post('/knowledge/' + row.id + '/favorite')
      row.favorite = true
    }
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`删除「${row.title}」？`, '确认', { type: 'warning' })
    await api.delete('/knowledge/' + row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message)
  }
}

function exportAll() {
  window.open(BASE_URL + '/api/export')
}

onMounted(() => {
  loadMeta()
  load()
})
</script>
