<template>
  <div class="ai-extract-result-container">
    <el-card class="result-card">
      <template #header>
        <div class="card-header">
          <span>AI提取结果详情</span>
          <div class="header-actions">
            <el-button @click="goBack">返回</el-button>
            <el-button type="primary" @click="applyToContract" :disabled="!extractData">应用到合同</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 基本信息 -->
        <el-descriptions title="提取状态" :column="2" border class="info-section">
          <el-descriptions-item label="附件ID">{{ extractResult?.attachmentId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="合同ID">{{ extractResult?.contractId || '未关联' }}</el-descriptions-item>
          <el-descriptions-item label="提取状态">
            <el-tag :type="statusTagType">{{ statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提取时间">{{ extractResult?.extractTime || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="extractResult?.errorMessage" label="错误信息" :span="2">
            <span class="error-text">{{ extractResult.errorMessage }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 提取的合同数据 -->
        <div v-if="extractData" class="extract-data-section">
          <h3>提取的合同信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="合同名称">{{ extractData.contractName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="合同编号">{{ extractData.contractNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="合同类型">{{ extractData.contractType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="合同金额">{{ formatAmount(extractData.contractAmount) }}</el-descriptions-item>
            <el-descriptions-item label="甲方">{{ extractData.partyA || '-' }}</el-descriptions-item>
            <el-descriptions-item label="乙方">{{ extractData.partyB || '-' }}</el-descriptions-item>
            <el-descriptions-item label="签署日期">{{ extractData.signDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生效日期">{{ extractData.startDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="到期日期">{{ extractData.endDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="服务期限">{{ extractData.servicePeriod || '-' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 条款信息 -->
          <h3>合同条款</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="付款条款">
              <div class="terms-content">{{ extractData.paymentTerms || '-' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="交付条款">
              <div class="terms-content">{{ extractData.deliveryTerms || '-' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="违约条款">
              <div class="terms-content">{{ extractData.breachClause || '-' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="其他条款">
              <div class="terms-content">{{ extractData.otherTerms || '-' }}</div>
            </el-descriptions-item>
          </el-descriptions>

          <!-- 原始JSON数据 -->
          <el-collapse class="raw-data-collapse">
            <el-collapse-item title="查看原始JSON数据">
              <pre class="json-data">{{ JSON.stringify(extractData, null, 2) }}</pre>
            </el-collapse-item>
          </el-collapse>
        </div>

        <!-- 无数据提示 -->
        <el-empty v-else-if="!loading" description="暂无提取数据">
          <el-button type="primary" @click="retryExtract">重新提取</el-button>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import fileApi from '@/api/file'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const extractResult = ref(null)

const attachmentId = computed(() => route.params.id || route.query.attachmentId)

const extractData = computed(() => {
  if (!extractResult.value?.extractData) return null
  try { return JSON.parse(extractResult.value.extractData) }
  catch { return null }
})

const statusText = computed(() => {
  const map = { PENDING: '等待处理', PROCESSING: '正在处理', SUCCESS: '提取成功', FAILED: '提取失败' }
  return map[extractResult.value?.extractStatus] || '未知'
})

const statusTagType = computed(() => {
  const map = { PENDING: 'info', PROCESSING: 'warning', SUCCESS: 'success', FAILED: 'danger' }
  return map[extractResult.value?.extractStatus] || 'info'
})

const formatAmount = (amount) => {
  if (!amount) return '-'
  return '¥ ' + Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const loadExtractResult = async () => {
  if (!attachmentId.value) { ElMessage.warning('缺少附件ID'); return }
  loading.value = true
  try {
    const res = await fileApi.getAiExtractResult(attachmentId.value)
    extractResult.value = res.data
  } catch (e) { ElMessage.error('加载提取结果失败') }
  finally { loading.value = false }
}

const retryExtract = async () => {
  try {
    await ElMessageBox.confirm('确定要重新提取吗？这将覆盖现有的提取结果。', '提示', { type: 'warning' })
    loading.value = true
    await fileApi.triggerAiExtract(attachmentId.value)
    ElMessage.success('已触发重新提取，请稍后刷新查看结果')
    setTimeout(loadExtractResult, 3000)
  } catch (e) { /* cancelled */ }
  finally { loading.value = false }
}

const applyToContract = () => { ElMessage.info('功能开发中，将自动填充合同表单') }
const goBack = () => router.back()

onMounted(() => loadExtractResult())
</script>

<style scoped>
.ai-extract-result-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
.info-section { margin-bottom: 24px; }
.extract-data-section h3 { margin: 24px 0 16px; color: #303133; font-size: 16px; }
.error-text { color: #f56c6c; }
.terms-content { white-space: pre-wrap; line-height: 1.6; }
.raw-data-collapse { margin-top: 24px; }
.json-data { background: #f5f7fa; padding: 16px; border-radius: 4px; overflow-x: auto; font-size: 12px; }
@media (max-width: 768px) {
  .el-descriptions { font-size: 12px; }
  .header-actions { flex-direction: column; gap: 8px; }
}
</style>

