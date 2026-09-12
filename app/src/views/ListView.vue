<template>
  <div class="page">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索标题 / 正文"
        clearable
        style="width: 220px"
        @keyup.enter="load(1)"
        @clear="load(1)"
      />
      <el-tree-select
        v-model="query.categoryId"
        :data="categoryOptions"
        check-strictly
        clearable
        placeholder="分类"
        style="width: 150px"
        @change="load(1)"
      />
      <el-select v-model="query.tagId" clearable placeholder="标签" style="width: 130px" @change="load(1)">
        <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 96px" @change="load(1)">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="发布" value="PUBLISHED" />
      </el-select>
      <el-select v-model="query.sort" style="width: 118px" @change="load(1)">
        <el-option label="最近更新" value="update_desc" />
        <el-option label="最早更新" value="update_asc" />
        <el-option label="最近创建" value="create_desc" />
        <el-option label="最早创建" value="create_asc" />
      </el-select>
      <el-radio-group v-model="tab" @change="load(1)">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="fav">收藏</el-radio-button>
      </el-radio-group>
    </div>

    <div class="toolbar actions">
      <el-button @click="importVisible = true">导入 .md</el-button>
      <el-button @click="catDialogVisible = true">分类管理</el-button>
      <el-button @click="exportAll">全库导出</el-button>
      <el-button type="primary" @click="router.push('/edit')">新建</el-button>
    </div>

    <el-empty v-if="!items.length" description="还没有内容" />

    <div v-else class="card-list">
      <div v-for="row in items" :key="row.id" class="row-card" @click="open(row)">
        <div class="row-main">
          <div class="row-title">
            <span v-if="row.status === 'DRAFT'" class="draft-badge">草稿</span>
            {{ row.title }}
          </div>
          <div class="row-meta">
            <span v-if="row.categoryName" class="row-cat">{{ row.categoryName }}</span>
            <el-tag v-for="t in row.tags" :key="t.id" size="small" effect="plain">{{ t.name }}</el-tag>
          </div>
        </div>

        <span class="row-time">{{ fmt(row.updateTime) }}</span>

        <div class="row-actions">
          <el-button
            link
            :type="row.favorite ? 'warning' : 'default'"
            @click.stop="toggleFavorite(row)"
          >
            {{ row.favorite ? '★' : '☆' }}
          </el-button>
          <el-button link type="danger" @click.stop="remove(row)">删除</el-button>
        </div>
      </div>
    </div>

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

    <el-dialog v-model="importVisible" title="导入 Markdown 文件" width="480px">
      <el-tree-select
        v-model="importCategoryId"
        :data="categoryOptions"
        check-strictly
        clearable
        placeholder="导入到分类（可选）"
        style="width: 100%; margin-bottom: 12px"
      />
      <el-upload drag multiple :auto-upload="false" accept=".md" v-model:file-list="fileList">
        <div style="padding: 20px 0">把 .md 文件拖到这里，或点击选择</div>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">导入</el-button>
      </template>
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
const importVisible = ref(false)
const importing = ref(false)
const importCategoryId = ref(null)
const fileList = ref([])

const query = reactive({ keyword: '', categoryId: null, tagId: null, status: '', sort: 'update_desc', page: 1, size: 20 })

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

async function doImport() {
  if (!fileList.value.length) {
    ElMessage.warning('请先选择 .md 文件')
    return
  }
  importing.value = true
  try {
    const fd = new FormData()
    fileList.value.forEach((f) => fd.append('files', f.raw))
    if (importCategoryId.value) fd.append('categoryId', importCategoryId.value)
    const n = await api.post('/import', fd)
    ElMessage.success('导入成功 ' + n + ' 篇')
    importVisible.value = false
    fileList.value = []
    importCategoryId.value = null
    load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  loadMeta()
  load()
})
</script>

<style scoped>
/* 操作按钮单独一行靠右，跟筛选分开，不然挤成一片看不出分组 */
.actions {
  justify-content: flex-end;
  margin-top: -4px;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.row-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 13px 18px;
  background: rgba(255, 255, 255, 0.045);
  backdrop-filter: blur(24px) saturate(140%);
  -webkit-backdrop-filter: blur(24px) saturate(140%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, transform 0.15s;
}

.row-card:hover {
  background: rgba(255, 255, 255, 0.07);
  border-color: rgba(255, 255, 255, 0.14);
  transform: translateY(-1px);
}

.row-main {
  flex: 1;
  min-width: 0;
}

.row-title {
  font-size: 14px;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.draft-badge {
  display: inline-block;
  font-size: 11px;
  line-height: 16px;
  padding: 0 6px;
  margin-right: 6px;
  border-radius: 5px;
  color: var(--el-text-color-secondary);
  background: rgba(255, 255, 255, 0.07);
  vertical-align: 1px;
}

.row-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  height: 20px;
}

.row-cat {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.row-time {
  flex-shrink: 0;
  font-family: Consolas, monospace;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.row-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 2px;
  opacity: 0.45;
  transition: opacity 0.15s;
}

.row-card:hover .row-actions {
  opacity: 1;
}
</style>
