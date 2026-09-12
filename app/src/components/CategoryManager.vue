<template>
  <div>
    <div class="add-row">
      <el-input
        v-model="newName"
        placeholder="新分类名称"
        style="width: 170px"
        @keyup.enter="add"
      />
      <el-tree-select
        v-model="newParent"
        :data="options"
        check-strictly
        clearable
        placeholder="上级分类（可选）"
        style="width: 170px"
      />
      <el-button type="primary" @click="add">添加</el-button>
    </div>

    <el-tree :data="tree" node-key="id" default-expand-all :props="{ label: 'name' }">
      <template #default="{ data }">
        <div class="cat-row">
          <span>{{ data.name }}</span>
          <span class="ops">
            <el-button link size="small" @click.stop="rename(data)">改名</el-button>
            <el-button link size="small" type="danger" @click.stop="remove(data)">删除</el-button>
          </span>
        </div>
      </template>
    </el-tree>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const emit = defineEmits(['changed'])

const tree = ref([])
const newName = ref('')
const newParent = ref(null)

const options = computed(() => toOptions(tree.value))

function toOptions(nodes) {
  return nodes.map((n) => ({
    label: n.name,
    value: n.id,
    children: n.children && n.children.length ? toOptions(n.children) : undefined,
  }))
}

async function load() {
  tree.value = await api.get('/category/tree')
}

async function add() {
  if (!newName.value.trim()) return
  try {
    await api.post('/category', {
      name: newName.value.trim(),
      parentId: newParent.value || 0,
    })
    ElMessage.success('已添加')
    newName.value = ''
    newParent.value = null
    await load()
    emit('changed')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function rename(data) {
  try {
    const { value } = await ElMessageBox.prompt('新名称', '改名', { inputValue: data.name })
    if (!value.trim()) return
    await api.put('/category/' + data.id, { name: value.trim() })
    ElMessage.success('已改名')
    await load()
    emit('changed')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message)
  }
}

async function remove(data) {
  try {
    await ElMessageBox.confirm(`删除分类「${data.name}」？`, '确认', { type: 'warning' })
    await api.delete('/category/' + data.id)
    ElMessage.success('已删除')
    await load()
    emit('changed')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message)
  }
}

onMounted(load)
</script>

<style scoped>
.add-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.cat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex: 1;
  padding-right: 8px;
}

.ops {
  opacity: 0;
  transition: opacity 0.15s;
}

.cat-row:hover .ops {
  opacity: 1;
}
</style>
