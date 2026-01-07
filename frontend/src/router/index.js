import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'contracts',
        name: 'Contracts',
        component: () => import('@/views/contract/ContractList.vue'),
        meta: { title: '合同管理' }
      },
      {
        path: 'contracts/:id',
        name: 'ContractDetail',
        component: () => import('@/views/contract/ContractDetail.vue'),
        meta: { title: '合同详情' }
      },
      {
        path: 'customers',
        name: 'Customers',
        component: () => import('@/views/customer/CustomerList.vue'),
        meta: { title: '客户管理' }
      },
      {
        path: 'payments',
        name: 'Payments',
        component: () => import('@/views/payment/PaymentList.vue'),
        meta: { title: '收款管理' }
      },
      {
        path: 'risks',
        name: 'Risks',
        component: () => import('@/views/risk/RiskList.vue'),
        meta: { title: '风险预警' }
      },
      {
        path: 'seals',
        name: 'Seals',
        component: () => import('@/views/seal/SealList.vue'),
        meta: { title: '印章管理' }
      },
      {
        path: 'seal-borrows',
        name: 'SealBorrows',
        component: () => import('@/views/seal/SealBorrowList.vue'),
        meta: { title: '印章借用' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/StatisticsPage.vue'),
        meta: { title: '统计分析' }
      },
      // 合同上传与AI提取
      // used in MainLayout.vue
      {
        path: 'contract-upload',
        name: 'ContractUpload',
        component: () => import('@/views/contract/ContractUpload.vue'),
        meta: { title: '合同上传' }
      },
      { // TODO: examine where the page "ai-extract" is used 
        // current result 1/4/26: nowhere this page is used.
        // TODO: place retry button on failed attempt items, while place view detail button (like this page) on success items
        path: 'ai-extract/:id',
        name: 'AiExtractResult',
        component: () => import('@/views/contract/AiExtractResult.vue'),
        meta: { title: 'AI提取结果' }
      },
      // 系统配置
      {
        path: 'system/ldap',
        name: 'LdapConfig',
        component: () => import('@/views/system/LdapConfig.vue'),
        meta: { title: 'LDAP配置', roles: ['admin'] }
      },
      {
        path: 'system/dify',
        name: 'DifyConfig',
        component: () => import('@/views/system/DifyConfig.vue'),
        meta: { title: 'Dify AI配置', roles: ['admin'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 合同管理系统` : '合同管理系统'
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router

