<template>
  <div class="customer-list">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="客户编号/名称/联系人" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable>
            <el-option label="企业客户" value="ENTERPRISE" />
            <el-option label="个人客户" value="PERSONAL" />
            <el-option label="政府机构" value="GOVERNMENT" />
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
          <span>客户列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增客户</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="customerCode" label="客户编号" min-width="140" />
        <el-table-column prop="customerName" label="客户名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="customerType" label="类型" width="100">
          <template #default="{ row }">{{ typeMap[row.customerType] || row.customerType }}</template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" min-width="100" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="130" />
        <el-table-column prop="creditLevel" label="信用等级" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.creditLevel" :type="creditMap[row.creditLevel]?.type">{{ creditMap[row.creditLevel]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '在网' : '离网' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="90%" :max-width="'700px'" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="top">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="客户类型" prop="customerType">
              <el-select v-model="form.customerType" placeholder="请选择" style="width: 100%">
                <el-option label="企业客户" value="ENTERPRISE" />
                <el-option label="个人客户" value="PERSONAL" />
                <el-option label="政府机构" value="GOVERNMENT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.contactEmail" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="信用等级">
              <el-select v-model="form.creditLevel" placeholder="请选择" style="width: 100%">
                <el-option label="A级" value="A" /><el-option label="B级" value="B" /><el-option label="C级" value="C" /><el-option label="D级" value="D" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="地址">
              <el-input v-model="form.address" placeholder="请输入地址" />
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
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getCustomers, createCustomer, updateCustomer, deleteCustomer } from '@/api/customer'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ keyword: '', type: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增客户')
const formRef = ref(null)
const form = reactive({ id: null, customerName: '', customerType: '', contactPerson: '', contactPhone: '', contactEmail: '', creditLevel: '', address: '' })
const rules = { customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }] }
const typeMap = { 'ENTERPRISE': '企业客户', 'PERSONAL': '个人客户', 'GOVERNMENT': '政府机构' }
const creditMap = { 'A': { label: 'A级', type: 'success' }, 'B': { label: 'B级', type: 'primary' }, 'C': { label: 'C级', type: 'warning' }, 'D': { label: 'D级', type: 'danger' } }

const fetchData = async () => {
  loading.value = true
  try { const res = await getCustomers({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm }); tableData.value = res.data || []; total.value = res.total || 0 } catch (e) { console.error(e) } finally { loading.value = false }
}
const handleSearch = () => { pageNum.value = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.type = ''; handleSearch() }
const handleAdd = () => { Object.assign(form, { id: null, customerName: '', customerType: '', contactPerson: '', contactPhone: '', contactEmail: '', creditLevel: '', address: '' }); dialogTitle.value = '新增客户'; dialogVisible.value = true }
const handleEdit = (row) => { Object.assign(form, row); dialogTitle.value = '编辑客户'; dialogVisible.value = true }
const handleDelete = async (row) => { try { await deleteCustomer(row.id); ElMessage.success('删除成功'); fetchData() } catch (e) { console.error(e) } }
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try { if (form.id) { await updateCustomer(form.id, form) } else { await createCustomer(form) }; ElMessage.success(form.id ? '更新成功' : '创建成功'); dialogVisible.value = false; fetchData() } catch (e) { console.error(e) } finally { submitting.value = false }
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
