<template>
  <div class="contract-upload-container">
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <span>合同PDF上传</span>
          <el-tag type="info">支持AI智能提取合同信息</el-tag>
        </div>
      </template>

      <!-- 关联合同选择 -->
      <el-form :model="uploadForm" label-width="100px" class="upload-form">
        <el-form-item label="关联合同">
          <el-select v-model="uploadForm.contractId" placeholder="选择要关联的合同（可选）" clearable filterable
            style="width: 100%">
            <el-option v-for="item in contractList" :key="item.id" :label="`${item.contractNo} - ${item.contractName}`"
              :value="item.id" />
          </el-select>
        </el-form-item>

        <!-- 文件上传区域 -->
        <el-form-item label="PDF文件">
          <el-upload ref="uploadRef" class="upload-dragger" drag :auto-upload="false" :limit="1" accept=".pdf"
            :on-change="handleFileChange" :on-remove="handleFileRemove" :file-list="fileList">
            <el-icon class="el-icon--upload">
              <UploadFilled />
            </el-icon>
            <div class="el-upload__text">将PDF文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">仅支持PDF格式文件，文件大小不超过50MB</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleUpload" :loading="uploading" :disabled="!selectedFile">
            {{ uploading ? '上传中...' : '上传并提取' }}
          </el-button>
          <el-button @click="resetUpload">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- AI提取进度 -->
    <el-card v-if="extractStatus" class="progress-card">
      <template #header><span>AI提取进度</span></template>
      <div class="progress-content">
        <el-steps :active="currentStep" finish-status="success">
          <el-step title="文件上传" :status="stepStatus(1)" />
          <el-step title="PDF解析" :status="stepStatus(2)" />
          <el-step title="AI提取" :status="stepStatus(3)" />
          <el-step title="完成" :status="stepStatus(4)" />
        </el-steps>
        <div class="status-message">
          <el-tag :type="statusTagType">{{ extractStatusText }}</el-tag>
          <span v-if="extractResult?.errorMessage" class="error-msg">{{ extractResult.errorMessage }}</span>
        </div>
      </div>
    </el-card>

    <!-- 提取结果预览 -->
    <el-card v-if="extractResult && extractResult.extractStatus === 'SUCCESS'" class="result-card">
      <template #header>
        <div class="card-header">
          <span>AI提取结果</span>
          <el-button type="primary" size="small" @click="applyToContract">应用到合同</el-button>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item v-for="(value, key) in parsedExtractData" :key="key" :label="key">
          {{ value }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 上传历史 -->
    <el-card class="history-card">
      <template #header><span>上传历史</span></template>
      <el-table :data="uploadHistory" v-loading="historyLoading" stripe>
        <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="aiExtractStatus" label="提取状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getExtractStatusType(row.aiExtractStatus)">{{ row.aiExtractStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="viewResult(row)">查看结果</el-button>
            <el-button size="small" type="danger" @click="deleteAttachment(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import fileApi from '@/api/file'
import { getContractList, createContract } from '@/api/contract'
import { createPayment } from '@/api/payment'
import { createCustomer } from '@/api/customer'

const uploadRef = ref(null)
const selectedFile = ref(null)
const fileList = ref([])
const uploading = ref(false)
const contractList = ref([])
const uploadHistory = ref([])
const historyLoading = ref(false)
const extractStatus = ref(null)
const extractResult = ref(null)
const currentStep = ref(0)

const uploadForm = reactive({ contractId: null }) // instead of null

const extractStatusText = computed(() => {
  const map = { PENDING: '等待处理', PROCESSING: '正在处理', SUCCESS: '提取成功', FAILED: '提取失败' }
  return map[extractStatus.value] || extractStatus.value
})

const statusTagType = computed(() => {
  const map = { PENDING: 'info', PROCESSING: 'warning', SUCCESS: 'success', FAILED: 'danger' }
  return map[extractStatus.value] || 'info'
})

const parsedExtractData = computed(() => {
  if (!extractResult.value?.extractData) return {}
  try { return JSON.parse(extractResult.value.extractData) } catch { return {} }
})

const stepStatus = (step) => {
  if (currentStep.value > step) return 'success'
  if (currentStep.value === step) return extractStatus.value === 'FAILED' ? 'error' : 'process'
  return 'wait'
}

const handleFileChange = (file) => { selectedFile.value = file.raw; fileList.value = [file] }
const handleFileRemove = () => { selectedFile.value = null; fileList.value = [] }


const handleUpload = async () => {
  if (!selectedFile.value) { ElMessage.warning('请选择文件'); return }
  uploading.value = true; extractStatus.value = 'PENDING'; currentStep.value = 1
  try {
    const res = await fileApi.uploadContractPdf(selectedFile.value, uploadForm.contractId)
    currentStep.value = 2; extractStatus.value = 'PROCESSING'
    pollExtractStatus(res.data.attachmentId)
    ElMessage.success('文件上传成功，正在进行AI提取...')
    loadUploadHistory()
  } catch (e) { ElMessage.error('上传失败: ' + (e.message || '未知错误')); extractStatus.value = 'FAILED' }
  finally { uploading.value = false }
}
// BUG: won't trigger "SUCCESS" or "完成" progress (step 4) on webpage once received success from server
const pollExtractStatus = async (attachmentId, maxRetries = 3000) => {
  for (let i = 0; i < maxRetries; i++) {
    await new Promise(r => setTimeout(r, 2000))
    try {
      const res = await fileApi.getAiExtractResult(attachmentId)
      extractResult.value = res.data
      extractStatus.value = res.data?.extractStatus || 'PROCESSING'
      if (extractStatus.value === 'SUCCESS') { currentStep.value = 4; return }
      if (extractStatus.value === 'FAILED') { currentStep.value = 3; return }
      currentStep.value = 3
    } catch (e) { console.error(e) }
  }
}

const resetUpload = () => {
  selectedFile.value = null; fileList.value = []
  extractStatus.value = null; extractResult.value = null; currentStep.value = 0
  uploadRef.value?.clearFiles()
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024, sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getExtractStatusType = (status) => {
  const map = { PENDING: 'info', PROCESSING: 'warning', SUCCESS: 'success', FAILED: 'danger' }
  return map[status] || 'info'
}

const loadContracts = async () => {
  try {
    const res = await getContractList({ pageNum: 1, pageSize: 10000000 })
    // console.log("received contract list:");
    // console.dir(res);
    // console.log(res.data?.records);
    contractList.value = res.data ? res.data : [];
  } catch (e) { console.error(e) }
}

const loadUploadHistory = async () => {
  historyLoading.value = true
  try {
    let res
    if (uploadForm.contractId) {
      res = await fileApi.getAttachments(uploadForm.contractId)
    } else {
      res = await fileApi.getAllAttachments()
    }
    uploadHistory.value = res.data || []
  } catch (e) { console.error(e) }
  finally { historyLoading.value = false }
}

const viewResult = async (row) => {
  try {
    const res = await fileApi.getAiExtractResult(row.id)
    extractResult.value = res.data
    extractStatus.value = res.data?.extractStatus
  } catch (e) { ElMessage.error('获取结果失败') }
}

const deleteAttachment = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该附件吗？', '提示', { type: 'warning' })
    await fileApi.deleteAttachment(row.id)
    ElMessage.success('删除成功')
    loadUploadHistory()
  } catch (e) { /* cancelled */ }
}

const applyToContract = () => {
  //  ElMessage.info('功能开发中...')
  // first, call api, then jump to contract detail or listing page
  // must send data to this api
  // create and get customer id

  let myCustomerName = parsedExtractData.value.partyA;
  let myContractName = parsedExtractData.value.contractName;
  let myContractType = parsedExtractData.value.contractType;
  let myContractStartDate = parsedExtractData.value.startDate;
  let myContractEndDate = parsedExtractData.value.endDate;
  let myContractAmount = parsedExtractData.value.contractAmount;
  createCustomer({
    id: null,
    customerName: myCustomerName,
    customerType: "",
    contactPerson: "",
    contactPhone: "",
    contactEmail: "",
    creditLevel: "",
    address: "",
  }).then((r1) => {
    console.log("Customer created: ", r1)
    let customerId = r1.data.id;

    let data_to_send = { "id": null, contractName: myContractName, customerId: customerId, contractType: myContractType, contractAmount: myContractAmount, startDate: myContractStartDate, endDate: myContractEndDate, remark: '' };
    createContract(data_to_send).then((response_data) => {
      console.log("Contract created");
      console.log("Response data:", response_data);
      // now create demo payment record

      let createdContractId = response_data.data.id;
      let url = "/contracts/" + createdContractId;

      createPayment({ "id": null, "contractId": createdContractId, "paymentType": "RECEIVE", "paymentAmount": 1000, "plannedPaymentDate": "2026-01-15", "remark": "" }).then(() => {
        window.location.href = url;
      })
    })

  })

}

onMounted(() => { loadContracts(); loadUploadHistory() })
</script>

<style scoped>
.contract-upload-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-form {
  max-width: 600px;
}

.upload-dragger {
  width: 100%;
}

.progress-content {
  padding: 20px 0;
}

.status-message {
  margin-top: 20px;
  text-align: center;
}

.error-msg {
  color: #f56c6c;
  margin-left: 10px;
}

@media (max-width: 768px) {
  .upload-form {
    max-width: 100%;
  }

  .el-descriptions {
    font-size: 12px;
  }
}
</style>
