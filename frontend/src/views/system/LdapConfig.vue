<template>
  <div class="ldap-config-container">
    <el-card class="page-header">
      <template #header>
        <div class="card-header">
          <span>LDAP配置管理</span>
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
        <el-table-column prop="host" label="服务器地址" />
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="baseDn" label="Base DN" show-overflow-tooltip />
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
        <el-form-item label="服务器地址" prop="host">
          <el-input v-model="form.host" placeholder="例如: 192.168.1.200" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="form.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="Base DN" prop="baseDn">
          <el-input v-model="form.baseDn" placeholder="例如: dc=company,dc=com" />
        </el-form-item>
        <el-form-item label="管理员DN" prop="userDn">
          <el-input v-model="form.userDn" placeholder="例如: cn=admin,dc=company,dc=com" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入管理员密码" show-password />
        </el-form-item>
        <el-form-item label="用户搜索过滤" prop="userSearchFilter">
          <el-input v-model="form.userSearchFilter" placeholder="例如: (uid={0})" />
        </el-form-item>
        <el-form-item label="启用同步" prop="syncEnabled">
          <el-switch v-model="form.syncEnabled" />
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
import { getLdapConfigList, createLdapConfig, updateLdapConfig, deleteLdapConfig,
  enableLdapConfig, disableLdapConfig } from '@/api/ldap'
import request from '@/api/request'

const loading = ref(false)
const configList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const editId = ref(null)
const testLoading = ref(false)
const submitLoading = ref(false)

const dialogTitle = computed(() => editId.value ? '编辑LDAP配置' : '新增LDAP配置')

const form = reactive({
  name: '', host: '', port: 389, baseDn: '', userDn: '', password: '',
  userSearchFilter: '(uid={0})', syncEnabled: false, remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  host: [{ required: true, message: '请输入服务器地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  baseDn: [{ required: true, message: '请输入Base DN', trigger: 'blur' }],
  userDn: [{ required: true, message: '请输入管理员DN', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getLdapConfigList()
    configList.value = res.data || []
  } catch (e) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editId.value = row.id
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该配置吗？', '提示', { type: 'warning' })
    await deleteLdapConfig(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* cancelled */ }
}

const handleToggle = async (row) => {
  try {
    if (row.enabled) {
      await disableLdapConfig(row.id)
    } else {
      await enableLdapConfig(row.id)
    }
    ElMessage.success(row.enabled ? '已禁用' : '已启用')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (editId.value) {
      await updateLdapConfig(editId.value, form)
    } else {
      await createLdapConfig(form)
    }
    ElMessage.success(editId.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleTest = async (row) => {
  testLoading.value = true
  try {
    const res = await request({
      url: '/ldap/test-connection',
      method: 'post',
      data: { host: row.host, port: String(row.port), baseDn: row.baseDn, userDn: row.userDn, password: row.password }
    })
    if (res.data?.success) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(res.data?.message || '连接测试失败')
    }
  } catch (e) {
    ElMessage.error('连接测试失败')
  } finally {
    testLoading.value = false
  }
}

const handleTestForm = async () => {
  testLoading.value = true
  try {
    const res = await request({
      url: '/ldap/test-connection',
      method: 'post',
      data: { host: form.host, port: String(form.port), baseDn: form.baseDn, userDn: form.userDn, password: form.password }
    })
    if (res.data?.success) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(res.data?.message || '连接测试失败')
    }
  } catch (e) {
    ElMessage.error('连接测试失败')
  } finally {
    testLoading.value = false
  }
}

const handleTestCurrent = async () => {
  try {
    const res = await request({ url: '/ldap/test-current-connection', method: 'post' })
    if (res.data?.success) {
      ElMessage.success('当前配置连接测试成功')
    } else {
      ElMessage.error(res.data?.message || '当前配置连接测试失败')
    }
  } catch (e) {
    ElMessage.error('连接测试失败')
  }
}

const resetForm = () => {
  Object.assign(form, {
    name: '', host: '', port: 389, baseDn: '', userDn: '', password: '',
    userSearchFilter: '(uid={0})', syncEnabled: false, remark: ''
  })
  formRef.value?.resetFields()
}

onMounted(() => loadData())
</script>

<style scoped>
.ldap-config-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
</style>

