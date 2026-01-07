<template>
  <div class="seal-list">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="印章编号/名称/保管人" clearable @keyup.enter="handleSearch" />
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
          <span>印章列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增印章</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <!-- <el-table-column prop="sealCode" label="印章编号" min-width="140" /> -->
        <el-table-column prop="sealName" label="印章名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="sealType" label="类型" width="100">
          <template #default="{ row }">{{ typeMap[row.sealType] || row.sealType }}</template>
        </el-table-column>
        <!-- <el-table-column prop="keeper" label="保管人" width="100" />-->
        <el-table-column prop="sealStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.sealStatus]?.type || 'info'">{{ statusMap[row.sealStatus]?.label || row.sealStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <!-- <el-button type="primary" link @click="handleEdit(row)">编辑</el-button> -->
            <!-- <el-button type="success" link @click="handleBorrow(row)" :disabled="row.sealStatus !== 'AVAILABLE'">借用</el-button> -->
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="90%" :max-width="'600px'" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="top">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="印章名称" prop="sealName">
              <el-input v-model="form.sealName" placeholder="请输入印章名称" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="印章类型" prop="sealType">
              <el-select v-model="form.sealType" placeholder="请选择类型" style="width: 100%">
                <el-option label="公章" value="OFFICIAL" />
                <el-option label="合同章" value="CONTRACT" />
                <el-option label="财务章" value="FINANCE" />
                <el-option label="法人章" value="LEGAL" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="保管人" prop="keeper">
              <el-input v-model="form.keeper" placeholder="请输入保管人姓名" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="状态" prop="sealStatus">
              <el-select v-model="form.sealStatus" placeholder="请选择状态" style="width: 100%">
                <el-option label="可用" value="AVAILABLE" />
                <el-option label="维护中" value="MAINTENANCE" />
                <el-option label="已遗失" value="LOST" />
              </el-select>
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

    <!-- 借用对话框 -->
    <el-dialog v-model="borrowDialogVisible" title="印章借用" width="90%" :max-width="'500px'" destroy-on-close>
      <el-form ref="borrowFormRef" :model="borrowForm" :rules="borrowRules" label-width="100px" label-position="top">
        <el-form-item label="借用人" prop="borrower">
          <el-input v-model="borrowForm.borrower" placeholder="请输入借用人姓名" />
        </el-form-item>
        <el-form-item label="借用原因" prop="borrowReason">
          <el-input v-model="borrowForm.borrowReason" type="textarea" :rows="3" placeholder="请输入借用原因" />
        </el-form-item>
        <el-form-item label="预计归还时间" prop="expectedReturnTime">
          <el-date-picker v-model="borrowForm.expectedReturnTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBorrowSubmit" :loading="submitting">确定借用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getSeals, createSeal, updateSeal, deleteSeal, createSealBorrow } from '@/api/seal'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ keyword: '', status: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增印章')
const formRef = ref(null)
const form = reactive({ id: null, sealName: '', sealType: '', keeper: '', sealStatus: 'AVAILABLE', remark: '' })
const rules = { sealName: [{ required: true, message: '请输入印章名称', trigger: 'blur' }], sealType: [{ required: true, message: '请选择印章类型', trigger: 'change' }] }
const statusMap = { 'AVAILABLE': { label: '可用', type: 'success' }, 'BORROWED': { label: '已借出', type: 'warning' }, 'MAINTENANCE': { label: '维护中', type: 'info' }, 'LOST': { label: '已遗失', type: 'danger' } }
const typeMap = { 'OFFICIAL': '公章', 'CONTRACT': '合同章', 'FINANCE': '财务章', 'LEGAL': '法人章', 'OTHER': '其他' }

const borrowDialogVisible = ref(false)
const borrowFormRef = ref(null)
const borrowForm = reactive({ sealId: null, borrower: '', borrowReason: '', expectedReturnTime: '' })
const borrowRules = { borrower: [{ required: true, message: '请输入借用人', trigger: 'blur' }], borrowReason: [{ required: true, message: '请输入借用原因', trigger: 'blur' }] }

const fetchData = async () => { loading.value = true; try { const res = await getSeals({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm }); tableData.value = res.data || []; total.value = res.total || 0 } catch (e) { console.error(e) } finally { loading.value = false } }
const handleSearch = () => { pageNum.value = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = ''; handleSearch() }
const handleAdd = () => { Object.assign(form, { id: null, sealName: '', sealType: '', keeper: '', sealStatus: 'AVAILABLE', remark: '' }); dialogTitle.value = '新增印章'; dialogVisible.value = true }
const handleEdit = (row) => { Object.assign(form, row); dialogTitle.value = '编辑印章'; dialogVisible.value = true }
const handleDelete = async (row) => { try { await deleteSeal(row.id); ElMessage.success('删除成功'); fetchData() } catch (e) { console.error(e) } }
const handleSubmit = async () => { if (!formRef.value) return; await formRef.value.validate(); submitting.value = true; try { if (form.id) { await updateSeal(form.id, form) } else { await createSeal(form) }; ElMessage.success(form.id ? '更新成功' : '创建成功'); dialogVisible.value = false; fetchData() } catch (e) { console.error(e) } finally { submitting.value = false } }
const handleBorrow = (row) => { Object.assign(borrowForm, { sealId: row.id, borrower: '', borrowReason: '', expectedReturnTime: '' }); borrowDialogVisible.value = true }
const handleBorrowSubmit = async () => { if (!borrowFormRef.value) return; await borrowFormRef.value.validate(); submitting.value = true; try { await createSealBorrow(borrowForm); ElMessage.success('借用成功'); borrowDialogVisible.value = false; fetchData() } catch (e) { console.error(e) } finally { submitting.value = false } }
onMounted(() => { fetchData() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.search-form { display: flex; flex-wrap: wrap; gap: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
@media (max-width: 768px) { .search-form .el-form-item { margin-right: 0; margin-bottom: 8px; width: 100%; } .search-form .el-input, .search-form .el-select { width: 100%; } }
</style>

