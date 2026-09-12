<template>
  <div v-if="isWidget">
    <router-view />
  </div>
  <div v-else class="main-layout">
    <!-- 原生标题栏已隐藏，这条顶栏就是窗口的拖拽区 -->
    <header class="titlebar">
      <div class="brand">
        <span class="brand-mark">📚</span>
        <span class="brand-text">日知</span>
      </div>
      <span class="page-title">{{ pageTitle }}</span>
      <div class="spacer"></div>
      <el-button size="small" :loading="backingUp" @click="backup">一键备份</el-button>
    </header>

    <div class="shell">
      <aside class="sidebar">
        <router-link
          v-for="n in NAVS"
          :key="n.path"
          :to="n.path"
          class="nav-item"
          :class="{ active: route.path === n.path }"
        >
          {{ n.label }}
        </router-link>
      </aside>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from './api'

const NAVS = [
  { path: '/', label: '知识' },
  { path: '/schedule', label: '日程' },
  { path: '/summary', label: '今日总结' },
]

const route = useRoute()
const backingUp = ref(false)
const isWidget = computed(() => route.path === '/widget')
const pageTitle = computed(() => NAVS.find((n) => n.path === route.path)?.label || '')

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
  overflow: hidden;
}

/* ── 顶栏（拖拽区）──────────────────────────────────── */
.titlebar {
  height: 48px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  /* 右侧留出系统窗口按钮的位置，宽度与 titleBarOverlay 一致 */
  padding: 0 148px 0 16px;
  -webkit-app-region: drag;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(24px) saturate(140%);
  -webkit-backdrop-filter: blur(24px) saturate(140%);
}

/* 拖拽区里的可交互元素必须排除，否则点不动 */
.titlebar :deep(.el-button) {
  -webkit-app-region: no-drag;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 168px;
  flex-shrink: 0;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.3px;
}

.brand-mark {
  font-size: 15px;
}

.page-title {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.spacer {
  flex: 1;
}

/* ── 主体 ───────────────────────────────────────────── */
.shell {
  flex: 1;
  display: flex;
  min-height: 0;
}

.sidebar {
  width: 200px;
  flex-shrink: 0;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

.nav-item {
  display: block;
  padding: 9px 12px;
  border-radius: 10px;
  border: 1px solid transparent;
  color: var(--el-text-color-regular);
  text-decoration: none;
  font-size: 14px;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.055);
  color: var(--el-text-color-primary);
}

/* 选中项就是一枚玻璃胶囊 */
.nav-item.active {
  background: rgba(255, 255, 255, 0.075);
  border-color: rgba(255, 255, 255, 0.1);
  color: #fff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

.content {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
}
</style>
