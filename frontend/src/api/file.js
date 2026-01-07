import request from './request'

/**
 * 文件上传API
 */
export default {
  /**
   * 上传PDF文件
   * @param {File} file - PDF文件
   * @param {Object} data - 附加数据
   * @returns {Promise}
   */
  uploadPdf(file, data = {}) {
    const formData = new FormData()
    formData.append('file', file)
    
    // 添加附加数据
    Object.keys(data).forEach(key => {
      if (data[key] !== undefined){
        if (data[key] !== null){
      formData.append(key, data[key])}
      }
    })
    
    return request({
      url: '/contract-attachment/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 上传合同PDF
   * @param {File} file - PDF文件
   * @param {Number} contractId - 合同ID
   * @returns {Promise}
   */
  uploadContractPdf(file, contractId) {
    return this.uploadPdf(file, { contractId })
  },

  /**
   * 获取所有附件列表
   * @returns {Promise}
   */
  getAllAttachments() {
    return request({
      url: '/contract-attachment/all',
      method: 'get'
    })
  },

  /**
   * 获取指定合同的附件列表
   * @param {Number} contractId - 合同ID
   * @returns {Promise}
   */
  getAttachments(contractId) {
    return request({
      url: `/contract-attachment/contract/${contractId}`,
      method: 'get'
    })
  },

  /**
   * 获取附件详情
   * @param {Number} id - 附件ID
   * @returns {Promise}
   */
  getAttachmentDetail(id) {
    return request({
      url: `/contract-attachment/${id}`,
      method: 'get'
    })
  },

  /**
   * 下载附件
   * @param {Number} id - 附件ID
   * @returns {Promise}
   */
  downloadAttachment(id) {
    return request({
      url: `/contract-attachment/${id}/download`,
      method: 'get',
      responseType: 'blob'
    })
  },

  /**
   * 删除附件
   * @param {Number} id - 附件ID
   * @returns {Promise}
   */
  deleteAttachment(id) {
    return request({
      url: `/contract-attachment/${id}`,
      method: 'delete'
    })
  },

  /**
   * 触发AI提取（重新提取）
   * @param {Number} attachmentId - 附件ID
   * @returns {Promise}
   */
  triggerAiExtract(attachmentId) {
    return request({
      url: `/contract-attachment/re-extract/${attachmentId}`,
      method: 'post'
    })
  },

  /**
   * 获取AI提取结果
   * @param {Number} attachmentId - 附件ID
   * @returns {Promise}
   */
  getAiExtractResult(attachmentId) {
    return request({
      url: `/contract-attachment/extract/${attachmentId}`,
      method: 'get'
    })
  }
}
