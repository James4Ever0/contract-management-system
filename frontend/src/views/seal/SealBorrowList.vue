<template>
  <div class="borrow-list">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="借用单号/印章名称/借用人" clearable @keyup.enter="handleSearch" />
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
          <span>借用记录</span>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="borrowNo" label="借用单号" min-width="140" />
        <el-table-column prop="sealName" label="印章名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="borrowerName" label="借用人" width="100" />
        <el-table-column prop="borrowReason" label="借用原因" min-width="160" show-overflow-tooltip />
        <el-table-column prop="borrowTime" label="借用时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.borrowTime) }}</template>
        </el-table-column>
        <el-table-column prop="expectedReturnTime" label="预计归还" width="160">
          <template #default="{ row }">{{ formatDateTime(row.expectedReturnTime) }}</template>
        </el-table-column>
        <el-table-column prop="actualReturnTime" label="实际归还" width="160">
          <template #default="{ row }">{{ row.actualReturnTime ? formatDateTime(row.actualReturnTime) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="borrowStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.borrowStatus]?.type || 'info'">{{ statusMap[row.borrowStatus]?.label || row.borrowStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确认归还印章？" @confirm="handleReturn(row)" v-if="row.borrowStatus === 'BORROWED'">
              <template #reference><el-button type="success" link>归还</el-button></template>
            </el-popconfirm>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getSealBorrows, returnSeal } from '@/api/seal'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ keyword: '', status: '' })
const statusMap = { 'BORROWED': { label: '已借出', type: 'warning' }, 'RETURNED': { label: '已归还', type: 'success' }, 'OVERDUE': { label: '逾期未还', type: 'danger' } }

const formatDateTime = (val) => {
  if (!val) return ''
  const d = new Date(val)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSealBorrows({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm })
    tableData.value = res.data || []
    total.value = res.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pageNum.value = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = ''; handleSearch() }
const handleReturn = async (row) => {
  try {
    await returnSeal(row.id)
    ElMessage.success('归还成功')
    fetchData()
  } catch (e) { console.error(e) }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.search-form { display: flex; flex-wrap: wrap; gap: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.text-muted { color: #909399; }
@media (max-width: 768px) { .search-form .el-form-item { margin-right: 0; margin-bottom: 8px; width: 100%; } .search-form .el-input, .search-form .el-select { width: 100%; } }
</style>

