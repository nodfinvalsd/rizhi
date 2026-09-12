<template>
  <div v-if="isWidget">
    <router-view />
  </div>
  <div v-else class="main-layout">
    <header class="app-header">
      <span class="logo">📚 个人知识库</span>
      <el-menu
        mode="horizontal"
        router
        :default-active="route.path"
        :ellipsis="false"
        class="nav"
      >
        <el-menu-item index="/">知识</el-menu-item>
        <el-menu-item index="/schedule">日程</el-menu-item>
        <el-menu-item index="/summary">今日总结</el-menu-item>
      </el-menu>
      <div style="flex: 1"></div>
      <el-button size="small" :loading="backingUp" @click="backup">一键备份</el-button>
    </header>
    <router-view />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from './api'

const route = useRoute()
const backingUp = ref(false)
const isWidget = computed(() => route.path === '/widget')

async function backup() {
  backingUp.value = true
  try {
    const path = await api.post('/backup')
    ElMessage.success('备份完成：' + path)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    backingUp.value = false
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.logo {
  font-weight: 600;
  font-size: 15px;
  white-space: nowrap;
}

.nav {
  border-bottom: none;
  /* 不让横向菜单被压缩，否则菜单项会被折叠进「···」里 */
  flex-shrink: 0;
}
</style>
