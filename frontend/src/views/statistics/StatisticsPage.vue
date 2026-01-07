<template>
  <div class="statistics-page">
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;"><el-icon><Document /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalContracts || 0 }}</div>
              <div class="stat-label">合同总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;"><el-icon><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCustomers || 0 }}</div>
              <div class="stat-label">客户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;"><el-icon><Money /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(stats.totalAmount) }}</div>
              <div class="stat-label">合同总额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c;"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.riskWarnings || 0 }}</div>
              <div class="stat-label">风险预警</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content" v-on:click="downloadAnalysisTable">
            <div class="stat-icon" style="background: #f56c6c;"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.riskWarnings || 0 }}</div>
              <div class="stat-label">生成表格</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header><span>合同状态分布</span></template>
          <div class="status-grid">
            <div class="status-item" v-for="item in contractStatusList" :key="item.label">
              <div class="status-value" :style="{ color: item.color }">{{ item.value }}</div>
              <div class="status-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header><span>合同类型分布</span></template>
          <div class="type-list">
            <div class="type-item" v-for="item in contractTypeList" :key="item.type">
              <div class="type-info">
                <span class="type-name">{{ typeMap[item.type] || item.type }}</span>
                <span class="type-count">{{ item.count }}个</span>
              </div>
              <el-progress :percentage="getPercentage(item.count)" :stroke-width="10" :show-text="false" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header><span>收款统计</span></template>
          <div class="amount-stats">
            <div class="amount-item"><span class="label">合同总额</span><span class="value">¥{{ formatMoney(stats.totalAmount) }}</span></div>
            <el-divider />
            <div class="amount-item"><span class="label">已收金额</span><span class="value success">¥{{ formatMoney(stats.receivedAmount) }}</span></div>
            <el-divider />
            <div class="amount-item"><span class="label">待收金额</span><span class="value warning">¥{{ formatMoney(stats.outstandingAmount) }}</span></div>
            <el-divider />
            <div class="amount-item"><span class="label">收款率</span><span class="value primary">{{ getReceivedRate() }}%</span></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header><span>印章统计</span></template>
          <div class="seal-stats">
            <div class="seal-item"><span class="label">印章总数</span><span class="value">{{ sealStats.total || 0 }}</span></div>
            <el-divider />
            <div class="seal-item"><span class="label">可用印章</span><span class="value success">{{ sealStats.available || 0 }}</span></div>
            <el-divider />
            <div class="seal-item"><span class="label">借用中</span><span class="value warning">{{ sealStats.borrowing || 0 }}</span></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Document, User, Money, Warning } from '@element-plus/icons-vue'
import { getDashboard } from '@/api/dashboard'
import { getSealStats, getAnalysisTable} from '@/api/seal'

const stats = ref({})
const sealStats = ref({})
const typeMap = { 'SALES': '销售合同', 'PURCHASE': '采购合同', 'SERVICE': '服务合同', 'OTHER': '其他' }
const formatMoney = (val) => val ? Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00'

const contractStatusList = computed(() => [
  { label: '执行中', value: stats.value.executingContracts || 0, color: '#409eff' },
  { label: '待审批', value: stats.value.pendingApprovals || 0, color: '#e6a23c' },
  { label: '即将到期', value: stats.value.expiringContracts || 0, color: '#f56c6c' },
  { label: '已到期', value: stats.value.expiredContracts || 0, color: '#909399' }
])

const contractTypeList = computed(() => stats.value.contractTypeDistribution || [])
const totalTypeCount = computed(() => contractTypeList.value.reduce((sum, item) => sum + (item.count || 0), 0))
const getPercentage = (count) => totalTypeCount.value > 0 ? Math.round((count / totalTypeCount.value) * 100) : 0
const getReceivedRate = () => {
  const total = stats.value.totalAmount || 0
  const received = stats.value.receivedAmount || 0
  return total > 0 ? Math.round((received / total) * 100) : 0
}

const fetchData = async () => {
  try {
    const [dashRes, sealRes] = await Promise.all([getDashboard(), getSealStats()])
    stats.value = dashRes.data || {}
    sealStats.value = sealRes.data || {}
  } catch (e) { console.error(e) }
}

/**
 * Download the analysis table in CSV format
 */
function downloadAnalysisTable() {
  getAnalysisTable().then(res => {
    const blob = new Blob([res.data], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'analysis_table.csv'
    link.click()
    window.URL.revokeObjectURL(url)
  })
}

onMounted(fetchData)
</script>

<style scoped>
.statistics-page { max-width: 1400px; margin: 0 auto; }
.stat-cards { margin-bottom: 16px; }
.stat-card { margin-bottom: 16px; }
.stat-content { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 48px; height: 48px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; font-size: 24px; }
.stat-info { flex: 1; }
.stat-value { font-size: 20px; font-weight: 600; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.chart-card { min-height: 200px; margin-bottom: 16px; }
.status-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; padding: 16px 0; }
.status-item { text-align: center; padding: 16px; background: #f5f7fa; border-radius: 8px; }
.status-value { font-size: 28px; font-weight: 600; }
.status-label { font-size: 14px; color: #909399; margin-top: 8px; }
.type-list { padding: 16px 0; }
.type-item { margin-bottom: 16px; }
.type-info { display: flex; justify-content: space-between; margin-bottom: 8px; }
.type-name { color: #606266; }
.type-count { color: #909399; }
.amount-stats, .seal-stats { padding: 10px 0; }
.amount-item, .seal-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.amount-item .label, .seal-item .label { color: #606266; }
.amount-item .value, .seal-item .value { font-size: 18px; font-weight: 600; }
.value.success { color: #67c23a; }
.value.warning { color: #e6a23c; }
.value.primary { color: #409eff; }
@media (max-width: 768px) { .stat-icon { width: 40px; height: 40px; font-size: 20px; } .stat-value { font-size: 18px; } .status-value { font-size: 22px; } }
</style>

