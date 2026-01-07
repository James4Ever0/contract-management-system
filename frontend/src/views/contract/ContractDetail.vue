<template>
  <div class="contract-detail">
    <el-page-header @back="$router.back()" content="合同详情" />
    <el-card class="detail-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>{{ contract.contractName || '合同详情' }}</span>
          <el-tag :type="statusMap[contract.contractStatus]?.type || 'info'" size="large">
            {{ statusMap[contract.contractStatus]?.label || contract.contractStatus }}
          </el-tag>
        </div>
      </template>
      <el-descriptions :column="isMobile ? 1 : 2" border>
        <el-descriptions-item label="合同编号">{{ contract.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ contract.contractName }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ contract.customerName }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ typeMap[contract.contractType] || contract.contractType }}</el-descriptions-item>
        <el-descriptions-item label="合同金额">
          <span class="amount">¥{{ formatMoney(contract.contractAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="签署日期">{{ contract.signDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ contract.startDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ contract.endDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="剩余天数">
          <el-tag v-if="contract.daysRemaining !== null" :type="contract.daysRemaining <= 30 ? 'danger' : 'success'">
            {{ contract.daysRemaining }}天
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ contract.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ contract.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="payment-card">
      <template #header>
        <div class="card-header">
          <span>收款记录</span>
          <el-button type="primary" size="small" @click="handleAddPayment">添加收款</el-button>
        </div>
      </template>
      <el-table :data="payments" v-loading="paymentLoading" stripe>
        <el-table-column prop="paymentNo" label="收款编号" min-width="140" />
        <el-table-column prop="paymentType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.paymentType === 'RECEIVE' ? 'success' : 'warning'">
              {{ row.paymentType === 'RECEIVE' ? '收款' : '返款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentAmount" label="金额" min-width="100" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.paymentAmount) }}</template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 'PAID' ? 'success' : 'warning'">
              {{ row.paymentStatus === 'PAID' ? '已收' : '待收' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="plannedPaymentDate" label="计划日期" width="110" />
        <el-table-column prop="actualPaymentDate" label="实际日期" width="110" />
      </el-table>
    </el-card>

    <div class="action-bar">
      <el-button @click="$router.back()">返回</el-button>
      <el-button type="primary" @click="$router.push(`/contracts`)">返回列表</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { getContractDetail } from '@/api/contract'
import { getPaymentsByContract } from '@/api/payment'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const isMobile = computed(() => appStore.isMobile)
const loading = ref(false)
const paymentLoading = ref(false)
const contract = ref({})
const payments = ref([])
const statusMap = { 'DRAFT': { label: '草稿', type: 'info' }, 'PENDING_APPROVAL': { label: '待审批', type: 'warning' }, 'APPROVED': { label: '已审批', type: 'success' }, 'EXECUTING': { label: '执行中', type: 'primary' }, 'COMPLETED': { label: '已完成', type: 'success' }, 'TERMINATED': { label: '已终止', type: 'danger' } }
const typeMap = { 'SALES': '销售合同', 'PURCHASE': '采购合同', 'SERVICE': '服务合同', 'OTHER': '其他' }
const formatMoney = (val) => val ? Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00'

const fetchContract = async () => {
  loading.value = true
  try { const res = await getContractDetail(route.params.id); contract.value = res.data || {} } catch (e) { console.error(e) } finally { loading.value = false }
}
const fetchPayments = async () => {
  paymentLoading.value = true
  try { const res = await getPaymentsByContract(route.params.id); payments.value = res.data || [] } catch (e) { console.error(e) } finally { paymentLoading.value = false }
}
const handleAddPayment = () => { router.push('/payments') }
onMounted(() => { fetchContract(); fetchPayments() })
</script>

<style scoped>
.contract-detail { max-width: 1200px; margin: 0 auto; }
.detail-card { margin-top: 16px; }
.payment-card { margin-top: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.amount { font-size: 18px; font-weight: 600; color: #409eff; }
.action-bar { margin-top: 16px; display: flex; justify-content: center; gap: 16px; }
</style>
