<template>
  <div class="page">
    <div class="toolbar">
      <el-button @click="router.back()">← 返回</el-button>
      <span style="font-weight: 600">{{ isNew ? '新建知识' : '编辑知识' }}</span>
      <div style="flex: 1"></div>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </div>

    <el-input v-model="form.title" placeholder="标题" size="large" style="margin-bottom: 12px" />

    <div class="toolbar" style="margin-bottom: 12px">
      <el-tree-select
        v-model="form.categoryId"
        :data="categoryOptions"
        check-strictly
        clearable
        placeholder="分类"
        style="width: 200px"
      />
      <el-select
        v-model="form.tagIds"
        multiple
        filterable
        allow-create
        default-first-option
        placeholder="标签（可输入新标签回车创建）"
        style="width: 320px"
      >
        <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-radio-group v-model="form.status">
        <el-radio-button value="DRAFT">草稿</el-radio-button>
        <el-radio-button value="PUBLISHED">发布</el-radio-button>
      </el-radio-group>
      <el-button link type="primary" @click="catDialogVisible = true">分类管理</el-button>
    </div>

    <MdEditor v-model="form.content" :on-upload-img="uploadImg" style="height: calc(100vh - 220px)" />

    <el-dialog v-model="catDialogVisible" title="分类管理" width="420px">
      <CategoryManager @changed="loadMeta" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import CategoryManager from '../components/CategoryManager.vue'
import api, { BASE_URL } from '../api'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const tags = ref([])
const categoryTree = ref([])
const categoryOptions = ref([])
const catDialogVisible = ref(false)

const isNew = computed(() => !route.params.id)
const form = reactive({ title: '', content: '', categoryId: null, tagIds: [], status: 'DRAFT' })

function toOptions(nodes) {
  return nodes.map((n) => ({
    label: n.name,
    value: n.id,
    children: n.children && n.children.length ? toOptions(n.children) : undefined,
  }))
}

// 输入新标签（allow-create 产生字符串值）时先创建标签，再替换为 id
watch(
  () => form.tagIds,
  async (vals) => {
    const toCreate = vals.filter((v) => typeof v === 'string')
    if (!toCreate.length) return
    for (const name of toCreate) {
      try {
        const t = await api.post('/tag', { name })
        const i = form.tagIds.indexOf(name)
        if (i >= 0) form.tagIds.splice(i, 1, t.id)
        tags.value.push(t)
      } catch (e) {
        ElMessage.error(e.message)
      }
    }
  }
)

async function uploadImg(files, callback) {
  const urls = []
  for (const f of files) {
    const fd = new FormData()
    fd.append('file', f)
    if (route.params.id) fd.append('knowledgeId', route.params.id)
    try {
      const a = await api.post('/attachment', fd)
      urls.push(BASE_URL + '/upload/' + a.filePath)
    } catch (e) {
      ElMessage.error('图片上传失败: ' + e.message)
    }
  }
  callback(urls)
}

async function save() {
  if (!form.title.trim()) {
    ElMessage.warning('标题不能为空')
    return
  }
  saving.value = true
  try {
    if (isNew.value) {
      await api.post('/knowledge', form)
    } else {
      await api.put('/knowledge/' + route.params.id, form)
    }
    ElMessage.success('已保存')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function loadMeta() {
  const [t, c] = await Promise.all([api.get('/tag/list'), api.get('/category/tree')])
  tags.value = t
  categoryTree.value = c
  categoryOptions.value = toOptions(c)
}

onMounted(async () => {
  await loadMeta()

  if (!isNew.value) {
    const d = await api.get('/knowledge/' + route.params.id)
    form.title = d.title
    form.content = d.content || ''
    form.categoryId = d.categoryId > 0 ? d.categoryId : null
    form.tagIds = (d.tags || []).map((x) => x.id)
    form.status = d.status
  }
})
</script>
