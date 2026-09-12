<template>
  <div class="page">
    <div class="toolbar">
      <el-button @click="router.back()">← 返回</el-button>
      <span style="font-weight: 600">{{ isNew ? '新建知识' : '编辑知识' }}</span>
      <el-tag v-if="dirty" size="small" type="warning">未保存</el-tag>
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

    <MdEditor
      v-model="form.content"
      :on-upload-img="uploadImg"
      theme="dark"
      style="height: calc(100vh - 220px)"
    />

    <el-dialog v-model="catDialogVisible" title="分类管理" width="420px">
      <CategoryManager @changed="loadMeta" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave, onBeforeRouteUpdate, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
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
const attachmentIds = ref([])

// 脏检查：拿加载/保存后的快照跟当前表单比，用来拦住"改了一半就走"
const snapshot = ref('')
const dirty = computed(() => snapshot.value !== '' && JSON.stringify(snapshotPayload()) !== snapshot.value)

function snapshotPayload() {
  return { ...form, attachmentIds: attachmentIds.value }
}

function takeSnapshot() {
  snapshot.value = JSON.stringify(snapshotPayload())
}

async function confirmLeave() {
  if (!dirty.value) return true
  try {
    await ElMessageBox.confirm('当前修改还没保存，离开会丢失，确定吗？', '提示', {
      type: 'warning',
      confirmButtonText: '丢弃并离开',
      cancelButtonText: '留下',
    })
    return true
  } catch (e) {
    return false
  }
}

// 去别的页面
onBeforeRouteLeave(() => confirmLeave())

// 只换 :id（同一个路由复用组件）时，先确认再重载
onBeforeRouteUpdate(() => confirmLeave())

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
      // 新建时知识还没 id，记录附件 id，保存后由后端绑定到知识
      if (!route.params.id) attachmentIds.value.push(a.id)
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
    const payload = { ...form, attachmentIds: attachmentIds.value }
    if (isNew.value) {
      await api.post('/knowledge', payload)
    } else {
      await api.put('/knowledge/' + route.params.id, payload)
    }
    takeSnapshot() // 保存后重新记快照，否则离开时会把"已保存"误判成未保存
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

async function loadArticle() {
  attachmentIds.value = []

  if (isNew.value) {
    Object.assign(form, { title: '', content: '', categoryId: null, tagIds: [], status: 'DRAFT' })
    takeSnapshot()
    return
  }

  try {
    const d = await api.get('/knowledge/' + route.params.id)
    form.title = d.title
    form.content = d.content || ''
    form.categoryId = d.categoryId > 0 ? d.categoryId : null
    form.tagIds = (d.tags || []).map((x) => x.id)
    form.status = d.status
    takeSnapshot()
  } catch (e) {
    // 文章不存在时不要停在空编辑器上，否则点保存会往一个不存在的 id 上写
    ElMessage.error(e.message || '内容加载失败')
    snapshot.value = '' // 已经跳走了，别再弹一次"未保存"
    router.replace('/')
  }
}

onMounted(async () => {
  try {
    await loadMeta()
  } catch (e) {
    ElMessage.error(e.message)
  }
  await loadArticle()
})

// 只换 :id 时组件实例被复用，onMounted 不再触发，得自己重载
watch(() => route.params.id, () => loadArticle())
</script>
