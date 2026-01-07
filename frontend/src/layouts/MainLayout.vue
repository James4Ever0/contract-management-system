<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarWidth" class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <span v-if="!isCollapsed">合同管理系统</span>
        <span v-else>CMS</span>
      </div>
      <el-menu :default-active="activeMenu" :collapse="isCollapsed" :collapse-transition="false" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/dashboard"><el-icon><HomeFilled /></el-icon><span>首页</span></el-menu-item>
        <el-sub-menu index="/contract">
          <template #title><el-icon><Document /></el-icon><span>合同管理</span></template>
          <el-menu-item index="/contracts">合同列表</el-menu-item>
          <el-menu-item index="/contract-upload">合同上传</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/customers"><el-icon><User /></el-icon><span>客户管理</span></el-menu-item>
        <el-menu-item index="/payments"><el-icon><Money /></el-icon><span>收款管理</span></el-menu-item>
        <el-sub-menu index="/seal">
          <template #title><el-icon><Stamp /></el-icon><span>印章管理</span></template>
          <el-menu-item index="/seals">印章列表</el-menu-item>
          <!-- <el-menu-item index="/seal-borrows">借用记录</el-menu-item> -->
        </el-sub-menu>
        <el-menu-item index="/risks"><el-icon><Warning /></el-icon><span>风险预警</span></el-menu-item>
        <el-menu-item index="/statistics"><el-icon><DataAnalysis /></el-icon><span>统计分析</span></el-menu-item>
        <el-sub-menu index="/system">
          <template #title><el-icon><Setting /></el-icon><span>系统配置</span></template>
          <el-menu-item index="/system/ldap">LDAP配置</el-menu-item>
          <el-menu-item index="/system/dify">Dify AI配置</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="toggle-btn" @click="toggleSidebar"><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title && $route.path !== '/dashboard'">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.username || 'Admin' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <!-- 移动端遮罩 -->
    <div v-if="isMobile && !isCollapsed" class="sidebar-mask" @click="toggleSidebar"></div>
  </el-container>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { HomeFilled, Document, User, Money, Warning, Fold, Expand, Stamp, DataAnalysis, Setting } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const isMobile = computed(() => appStore.isMobile)
const isCollapsed = computed(() => appStore.sidebarCollapsed)
const sidebarWidth = computed(() => isCollapsed.value ? '64px' : '200px')
const activeMenu = computed(() => route.path)

const toggleSidebar = () => appStore.toggleSidebar()

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
      await userStore.logout()
      router.push('/login')
    } catch (e) { /* cancelled */ }
  }
}

watch(() => route.path, () => {
  if (isMobile.value && !isCollapsed.value) {
    appStore.toggleSidebar()
  }
})
</script>

<style scoped>
.main-layout { height: 100vh; }
.sidebar { background: #304156; transition: width 0.3s; overflow: hidden; position: relative; z-index: 1001; }
.sidebar.collapsed { width: 64px; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; font-weight: 600; background: #263445; white-space: nowrap; overflow: hidden; }
.el-menu { border-right: none; }
.main-container { display: flex; flex-direction: column; overflow: hidden; }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; box-shadow: 0 1px 4px rgba(0,21,41,.08); padding: 0 16px; }
.header-left { display: flex; align-items: center; gap: 16px; }
.toggle-btn { font-size: 20px; cursor: pointer; color: #606266; }
.header-right { display: flex; align-items: center; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { color: #606266; }
.main-content { background: #f0f2f5; padding: 16px; overflow-y: auto; }
.sidebar-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000; }

@media (max-width: 768px) {
  .sidebar { position: fixed; top: 0; left: 0; height: 100vh; z-index: 1001; }
  .sidebar.collapsed { left: -200px; width: 200px; }
  .username { display: none; }
  .main-content { padding: 12px; }
}
</style>
