<template>
  <div class="dify-config-container">
    <el-card class="page-header">
      <template #header>
        <div class="card-header">
          <span>Dify AI配置管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">新增配置</el-button>
            <el-button @click="handleTestCurrent">测试当前连接</el-button>
          </div>
        </div>
      </template>
      
      <!-- 配置列表 -->
      <el-table :data="configList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="配置名称" />
        <el-table-column prop="apiUrl" label="API地址" show-overflow-tooltip />
        <el-table-column prop="apiKey" label="API Key" width="150">
          <template #default="{ row }">
            <span>{{ maskApiKey(row.apiKey) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="handleTest(row)">测试</el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click="handleToggle(row)">
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="API地址" prop="apiUrl">
          <el-input v-model="form.apiUrl" placeholder="例如: http://dify.example.com/v1/workflows/run" />
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input v-model="form.apiKey" type="password" placeholder="请输入API Key" show-password />
        </el-form-item>
        <el-form-item label="工作流ID" prop="workflowId">
          <el-input v-model="form.workflowId" placeholder="可选，Dify工作流ID" />
        </el-form-item>
        <el-form-item label="超时时间(ms)" prop="timeout">
          <el-input-number v-model="form.timeout" :min="1000" :max="300000" :step="1000" />
        </el-form-item>
        <el-form-item label="重试次数" prop="retryCount">
          <el-input-number v-model="form.retryCount" :min="0" :max="10" />
        </el-form-item>
        <el-form-item label="立即启用" prop="enabled">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="info" @click="handleTestForm" :loading="testLoading">测试连接</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDifyConfigList, createDifyConfig, updateDifyConfig, deleteDifyConfig,
  enableDifyConfig, disableDifyConfig, testDifyConnection, testCurrentDifyConnection } from '@/api/dify'

const loading = ref(false)
const configList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const editId = ref(null)
const testLoading = ref(false)
const submitLoading = ref(false)

const dialogTitle = computed(() => editId.value ? '编辑Dify配置' : '新增Dify配置')

const form = reactive({
  name: '', apiUrl: '', apiKey: '', workflowId: '', timeout: 30000, retryCount: 3, enabled: false, remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  apiUrl: [{ required: true, message: '请输入API地址', trigger: 'blur' }],
  apiKey: [{ required: true, message: '请输入API Key', trigger: 'blur' }]
}

const maskApiKey = (key) => {
  if (!key || key.length <= 8) return '****'
  return key.substring(0, 4) + '****' + key.substring(key.length - 4)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDifyConfigList()
    configList.value = res.data || []
  } catch (e) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => { editId.value = null; resetForm(); dialogVisible.value = true }

const handleEdit = (row) => {
  editId.value = row.id
  Object.assign(form, { ...row, apiKey: '' })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该配置吗？', '提示', { type: 'warning' })
    await deleteDifyConfig(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* cancelled */ }
}

const handleToggle = async (row) => {
  try {
    row.enabled ? await disableDifyConfig(row.id) : await enableDifyConfig(row.id)
    ElMessage.success(row.enabled ? '已禁用' : '已启用')
    loadData()
  } catch (e) { ElMessage.error('操作失败') }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    editId.value ? await updateDifyConfig(editId.value, form) : await createDifyConfig(form)
    ElMessage.success(editId.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { ElMessage.error('操作失败') }
  finally { submitLoading.value = false }
}

const handleTest = async (row) => {
  testLoading.value = true
  try {
    const res = await testDifyConnection({ apiUrl: row.apiUrl, apiKey: row.apiKey })
    if (res.data?.success) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(res.data?.message || '连接测试失败')
    }
  } catch (e) { ElMessage.error('连接测试失败') }
  finally { testLoading.value = false }
}

const handleTestForm = async () => {
  testLoading.value = true
  try {
    const res = await testDifyConnection({ apiUrl: form.apiUrl, apiKey: form.apiKey })
    if (res.data?.success) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(res.data?.message || '连接测试失败')
    }
  } catch (e) { ElMessage.error('连接测试失败') }
  finally { testLoading.value = false }
}

const handleTestCurrent = async () => {
  try {
    const res = await testCurrentDifyConnection()
    if (res.data?.success) {
      ElMessage.success('当前配置连接测试成功')
    } else {
      ElMessage.error(res.data?.message || '当前配置连接测试失败')
    }
  } catch (e) { ElMessage.error('连接测试失败') }
}

const resetForm = () => {
  Object.assign(form, { name: '', apiUrl: '', apiKey: '', workflowId: '', timeout: 30000, retryCount: 3, enabled: false, remark: '' })
  formRef.value?.resetFields()
}

onMounted(() => loadData())
</script>

<style scoped>
.dify-config-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
</style>

