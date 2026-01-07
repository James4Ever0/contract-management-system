<template>
  <div class="dashboard">
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="6" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.totalContracts || 0 }}</div>
              <div class="stat-label">合同总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatMoney(dashboard.receivedAmount) }}</div>
              <div class="stat-label">已收金额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.expiringContracts || 0 }}</div>
              <div class="stat-label">即将到期</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.riskWarnings || 0 }}</div>
              <div class="stat-label">风险预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header><span>合同状态分布</span></template>
          <div class="chart-placeholder">
            <el-row :gutter="8">
              <el-col :span="8" v-for="item in contractStatusList" :key="item.label">
                <div class="status-item">
                  <div class="status-value">{{ item.value }}</div>
                  <div class="status-label">{{ item.label }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header><span>金额统计</span></template>
          <div class="amount-stats">
            <div class="amount-item"><span class="label">合同总额</span><span class="value">¥{{ formatMoney(dashboard.totalAmount) }}</span></div>
            <el-divider />
            <div class="amount-item"><span class="label">已收金额</span><span class="value success">¥{{ formatMoney(dashboard.receivedAmount) }}</span></div>
            <el-divider />
            <div class="amount-item"><span class="label">待收金额</span><span class="value warning">¥{{ formatMoney(dashboard.outstandingAmount) }}</span></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="recent-card">
      <template #header>
        <div class="card-header"><span>最近合同</span><el-button type="primary" link @click="$router.push('/contracts')">查看全部</el-button></div>
      </template>
      <el-table :data="dashboard.recentContracts" style="width: 100%" v-loading="loading">
        <el-table-column prop="contractNo" label="合同编号" min-width="140" />
        <el-table-column prop="contractName" label="合同名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户" min-width="140" show-overflow-tooltip />
        <el-table-column prop="contractAmount" label="金额" min-width="120">
          <template #default="{ row }">¥{{ formatMoney(row.contractAmount) }}</template>
        </el-table-column>
        <el-table-column prop="contractStatus" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="getStatusType(row.contractStatus)">{{ getStatusLabel(row.contractStatus) }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getDashboard } from '@/api/dashboard'
import { Document, Money, Clock, Warning } from '@element-plus/icons-vue'

const loading = ref(false)
const dashboard = ref({})
const contractStatusList = computed(() => [
  { label: '执行中', value: dashboard.value.executingContracts || 0 },
  { label: '待审批', value: dashboard.value.pendingApprovals || 0 },
  { label: '已到期', value: dashboard.value.expiredContracts || 0 }
])
const statusMap = { 'DRAFT': { label: '草稿', type: 'info' }, 'PENDING_APPROVAL': { label: '待审批', type: 'warning' }, 'APPROVED': { label: '已审批', type: 'success' }, 'EXECUTING': { label: '执行中', type: 'primary' }, 'COMPLETED': { label: '已完成', type: 'success' }, 'TERMINATED': { label: '已终止', type: 'danger' } }
const formatMoney = (val) => val ? Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00'
const getStatusType = (status) => statusMap[status]?.type || 'info'
const getStatusLabel = (status) => statusMap[status]?.label || status
const fetchData = async () => {
  loading.value = true
  try { const res = await getDashboard(); dashboard.value = res.data || {} } catch (e) { console.error(e) } finally { loading.value = false }
}
onMounted(fetchData)
</script>

<style scoped>
.dashboard { max-width: 1400px; margin: 0 auto; }
.stat-cards, .chart-row { margin-bottom: 16px; }
.stat-card, .chart-card { margin-bottom: 16px; }
.chart-card { min-height: 200px; }
.stat-content { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 48px; height: 48px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; font-size: 24px; }
.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: 600; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.chart-placeholder { padding: 20px 0; }
.status-item { text-align: center; padding: 16px 8px; background: #f5f7fa; border-radius: 8px; }
.status-value { font-size: 28px; font-weight: 600; color: #409eff; }
.status-label { font-size: 14px; color: #909399; margin-top: 8px; }
.amount-stats { padding: 10px 0; }
.amount-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.amount-item .label { color: #606266; }
.amount-item .value { font-size: 18px; font-weight: 600; }
.amount-item .value.success { color: #67c23a; }
.amount-item .value.warning { color: #e6a23c; }
.recent-card .card-header { display: flex; justify-content: space-between; align-items: center; }
@media (max-width: 768px) { .stat-icon { width: 40px; height: 40px; font-size: 20px; } .stat-value { font-size: 20px; } .stat-label { font-size: 12px; } .status-value { font-size: 22px; } }
</style>
