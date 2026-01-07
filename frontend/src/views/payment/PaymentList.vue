<template>
  <div class="payment-list">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="收款编号/合同编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待收" value="PENDING" />
            <el-option label="已收" value="PAID" />
            <el-option label="逾期" value="OVERDUE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>收款记录</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增收款</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="paymentNo" label="收款编号" min-width="140" />
        <el-table-column prop="contractNo" label="合同编号" min-width="140" />
        <el-table-column prop="paymentType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.paymentType === 'RECEIVE' ? 'success' : 'warning'">{{ row.paymentType === 'RECEIVE' ? '收款' : '返款' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentAmount" label="金额(元)" min-width="120" align="right">
          <template #default="{ row }">{{ formatMoney(row.paymentAmount) }}</template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.paymentStatus]?.type">{{ statusMap[row.paymentStatus]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="plannedPaymentDate" label="计划日期" width="110" />
        <el-table-column prop="actualPaymentDate" label="实际日期" width="110" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.paymentStatus !== 'PAID'" type="success" link @click="handleConfirm(row)">确认收款</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="90%" :max-width="'600px'" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="top">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="关联合同" prop="contractId">
              <el-select v-model="form.contractId" placeholder="请选择合同" filterable style="width: 100%">
                <el-option v-for="c in contractList" :key="c.id" :label="`${c.contractNo} - ${c.contractName}`" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="收款类型" prop="paymentType">
              <el-select v-model="form.paymentType" placeholder="请选择" style="width: 100%">
                <el-option label="收款" value="RECEIVE" />
                <el-option label="返款" value="REFUND" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="收款金额" prop="paymentAmount">
              <el-input-number v-model="form.paymentAmount" :precision="2" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="计划日期" prop="plannedPaymentDate">
              <el-date-picker v-model="form.plannedPaymentDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getPayments, createPayment, updatePayment, confirmPayment } from '@/api/payment'
import { getAllContracts } from '@/api/contract'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ keyword: '', status: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增收款')
const formRef = ref(null)
const contractList = ref([])
const form = reactive({ id: null, contractId: null, paymentType: 'RECEIVE', paymentAmount: null, plannedPaymentDate: '', remark: '' })
const rules = { contractId: [{ required: true, message: '请选择合同', trigger: 'change' }], paymentAmount: [{ required: true, message: '请输入金额', trigger: 'blur' }] }
const statusMap = { 'PENDING': { label: '待收', type: 'warning' }, 'PAID': { label: '已收', type: 'success' }, 'OVERDUE': { label: '逾期', type: 'danger' } }
const formatMoney = (val) => val ? Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00'

const fetchData = async () => {
  loading.value = true
  try { const res = await getPayments({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm }); tableData.value = res.data || []; total.value = res.total || 0 } catch (e) { console.error(e) } finally { loading.value = false }
}
const fetchContracts = async () => { try { const res = await getAllContracts(); contractList.value = res.data || [] } catch (e) { console.error(e) } }
const handleSearch = () => { pageNum.value = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = ''; handleSearch() }
const handleAdd = () => { Object.assign(form, { id: null, contractId: null, paymentType: 'RECEIVE', paymentAmount: null, plannedPaymentDate: '', remark: '' }); dialogTitle.value = '新增收款'; dialogVisible.value = true }
const handleEdit = (row) => { Object.assign(form, row); dialogTitle.value = '编辑收款'; dialogVisible.value = true }
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确认已收到该笔款项？', '确认收款', { type: 'warning' })
    await confirmPayment(row.id)
    ElMessage.success('确认成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try { if (form.id) { await updatePayment(form.id, form) } else { await createPayment(form) }; ElMessage.success(form.id ? '更新成功' : '创建成功'); dialogVisible.value = false; fetchData() } catch (e) { console.error(e) } finally { submitting.value = false }
}
onMounted(() => { fetchData(); fetchContracts() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.search-form { display: flex; flex-wrap: wrap; gap: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
@media (max-width: 768px) { .search-form .el-form-item { margin-right: 0; margin-bottom: 8px; width: 100%; } }
</style>
