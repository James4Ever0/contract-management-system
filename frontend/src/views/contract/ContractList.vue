<template>
  <div class="contract-list">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="合同编号/名称/客户" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option v-for="(item, key) in statusMap" :key="key" :label="item.label" :value="key" />
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
          <span>合同列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增合同</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="contractNo" label="合同编号" min-width="140" />
        <el-table-column prop="contractName" label="合同名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户" min-width="140" show-overflow-tooltip />
        <el-table-column prop="contractAmount" label="金额(元)" min-width="120" align="right">
          <template #default="{ row }">{{ formatMoney(row.contractAmount) }}</template>
        </el-table-column>
        <el-table-column prop="endDate" label="到期日" width="110" />
        <el-table-column prop="contractStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.contractStatus]?.type || 'info'">{{ statusMap[row.contractStatus]?.label || row.contractStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row)">
              <template #reference><el-button type="danger" link>删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="90%" :max-width="'700px'" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="top">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="合同名称" prop="contractName">
              <el-input v-model="form.contractName" placeholder="请输入合同名称" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" filterable style="width: 100%">
                <el-option v-for="c in customerList" :key="c.id" :label="c.customerName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="合同类型" prop="contractType">
              <el-select v-model="form.contractType" placeholder="请选择类型" style="width: 100%">
                <el-option label="销售合同" value="SALES" />
                <el-option label="采购合同" value="PURCHASE" />
                <el-option label="服务合同" value="SERVICE" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="合同金额" prop="contractAmount">
              <el-input-number v-model="form.contractAmount" :precision="2" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getContracts, createContract, updateContract, deleteContract } from '@/api/contract'
import { getAllCustomers } from '@/api/customer'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ keyword: '', status: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增合同')
const formRef = ref(null)
const customerList = ref([])
const form = reactive({ id: null, contractName: '', customerId: null, contractType: '', contractAmount: null, startDate: '', endDate: '', remark: '' })
const rules = { contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }], customerId: [{ required: true, message: '请选择客户', trigger: 'change' }] }
const statusMap = { 'DRAFT': { label: '草稿', type: 'info' }, 'PENDING_APPROVAL': { label: '待审批', type: 'warning' }, 'APPROVED': { label: '已审批', type: 'success' }, 'EXECUTING': { label: '执行中', type: 'primary' }, 'COMPLETED': { label: '已完成', type: 'success' }, 'TERMINATED': { label: '已终止', type: 'danger' } }
const formatMoney = (val) => val ? Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00'

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getContracts({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm })
    tableData.value = res.data || []
    total.value = res.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}
const fetchCustomers = async () => { try { const res = await getAllCustomers(); customerList.value = res.data || [] } catch (e) { console.error(e) } }
const handleSearch = () => { pageNum.value = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = ''; handleSearch() }
const handleAdd = () => { Object.assign(form, { id: null, contractName: '', customerId: null, contractType: '', contractAmount: null, startDate: '', endDate: '', remark: '' }); dialogTitle.value = '新增合同'; dialogVisible.value = true }
const handleEdit = (row) => { Object.assign(form, row); dialogTitle.value = '编辑合同'; dialogVisible.value = true }
const handleView = (row) => { router.push(`/contracts/${row.id}`) }
const handleDelete = async (row) => { try { await deleteContract(row.id); ElMessage.success('删除成功'); fetchData() } catch (e) { console.error(e) } }
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) { await updateContract(form.id, form) } else { await createContract(form) }
    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) { console.error(e) } finally { submitting.value = false }
}
onMounted(() => { fetchData(); fetchCustomers() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.search-form { display: flex; flex-wrap: wrap; gap: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
@media (max-width: 768px) { .search-form .el-form-item { margin-right: 0; margin-bottom: 8px; width: 100%; } .search-form .el-input, .search-form .el-select { width: 100%; } }
</style>
