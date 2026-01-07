<template>
  <div class="risk-list">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="风险等级">
          <el-select v-model="searchForm.level" placeholder="全部" clearable>
            <el-option label="高风险" value="HIGH" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="低风险" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已忽略" value="IGNORED" />
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
          <span>风险预警列表</span>
          <el-button type="warning" @click="handleScan" :loading="scanning"><el-icon><Refresh /></el-icon>扫描风险</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="riskType" label="风险类型" min-width="120">
          <template #default="{ row }">{{ typeMap[row.riskType] || row.riskType }}</template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelMap[row.riskLevel]?.type">{{ levelMap[row.riskLevel]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contractNo" label="关联合同" min-width="140" />
        <el-table-column prop="riskDescription" label="风险描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="riskStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.riskStatus]?.type">{{ statusMap[row.riskStatus]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发现时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.riskStatus === 'PENDING'" type="primary" link @click="handleProcess(row)">处理</el-button>
            <el-button v-if="row.riskStatus === 'PENDING'" type="info" link @click="handleIgnore(row)">忽略</el-button>
            <el-button v-if="row.riskStatus === 'PROCESSING'" type="success" link @click="handleResolve(row)">解决</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="处理风险" width="90%" :max-width="'500px'" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="top">
        <el-form-item label="处理措施" prop="handleMeasure">
          <el-input v-model="form.handleMeasure" type="textarea" :rows="4" placeholder="请输入处理措施" />
        </el-form-item>
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
import { Search, Refresh } from '@element-plus/icons-vue'
import { getRisks, processRisk, resolveRisk, ignoreRisk, scanRisks } from '@/api/risk'

const loading = ref(false)
const submitting = ref(false)
const scanning = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ level: '', status: '' })
const dialogVisible = ref(false)
const formRef = ref(null)
const currentRisk = ref(null)
const form = reactive({ handleMeasure: '' })
const rules = { handleMeasure: [{ required: true, message: '请输入处理措施', trigger: 'blur' }] }
const typeMap = { 'CONTRACT_EXPIRING': '合同即将到期', 'PAYMENT_OVERDUE': '收款逾期', 'CUSTOMER_CREDIT': '客户信用风险', 'CONTRACT_AMOUNT': '合同金额异常' }
const levelMap = { 'HIGH': { label: '高风险', type: 'danger' }, 'MEDIUM': { label: '中风险', type: 'warning' }, 'LOW': { label: '低风险', type: 'info' } }
const statusMap = { 'PENDING': { label: '待处理', type: 'warning' }, 'PROCESSING': { label: '处理中', type: 'primary' }, 'RESOLVED': { label: '已解决', type: 'success' }, 'IGNORED': { label: '已忽略', type: 'info' } }

const fetchData = async () => {
  loading.value = true
  try { const res = await getRisks({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm }); tableData.value = res.data || []; total.value = res.total || 0 } catch (e) { console.error(e) } finally { loading.value = false }
}
const handleSearch = () => { pageNum.value = 1; fetchData() }
const handleReset = () => { searchForm.level = ''; searchForm.status = ''; handleSearch() }
const handleScan = async () => {
  scanning.value = true
  try { await scanRisks(); ElMessage.success('扫描完成'); fetchData() } catch (e) { console.error(e) } finally { scanning.value = false }
}
const handleProcess = (row) => { currentRisk.value = row; form.handleMeasure = ''; dialogVisible.value = true }
const handleIgnore = async (row) => {
  try { await ElMessageBox.confirm('确定忽略该风险？', '提示', { type: 'warning' }); await ignoreRisk(row.id); ElMessage.success('操作成功'); fetchData() } catch (e) { if (e !== 'cancel') console.error(e) }
}
const handleResolve = async (row) => {
  try { await ElMessageBox.confirm('确定该风险已解决？', '提示', { type: 'warning' }); await resolveRisk(row.id); ElMessage.success('操作成功'); fetchData() } catch (e) { if (e !== 'cancel') console.error(e) }
}
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try { await processRisk(currentRisk.value.id, form); ElMessage.success('操作成功'); dialogVisible.value = false; fetchData() } catch (e) { console.error(e) } finally { submitting.value = false }
}
onMounted(fetchData)
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.search-form { display: flex; flex-wrap: wrap; gap: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
@media (max-width: 768px) { .search-form .el-form-item { margin-right: 0; margin-bottom: 8px; width: 100%; } }
</style>
