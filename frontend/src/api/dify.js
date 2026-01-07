import request from './request'

/**
 * Dify配置管理API
 */

/**
 * 获取Dify配置列表
 * @returns {Promise}
 */
export function getDifyConfigList() {
  return request({
    url: '/dify/config/list',
    method: 'get'
  })
}

/**
 * 获取当前激活的Dify配置
 * @returns {Promise}
 */
export function getActiveDifyConfig() {
  return request({
    url: '/dify/config/active',
    method: 'get'
  })
}

/**
 * 获取Dify配置详情
 * @param {number} id - 配置ID
 * @returns {Promise}
 */
export function getDifyConfig(id) {
  return request({
    url: `/dify/config/${id}`,
    method: 'get'
  })
}

/**
 * 创建Dify配置
 * @param {Object} data - Dify配置数据
 * @returns {Promise}
 */
export function createDifyConfig(data) {
  return request({
    url: '/dify/config',
    method: 'post',
    data: data
  })
}

/**
 * 更新Dify配置
 * @param {number} id - 配置ID
 * @param {Object} data - Dify配置数据
 * @returns {Promise}
 */
export function updateDifyConfig(id, data) {
  return request({
    url: `/dify/config/${id}`,
    method: 'put',
    data: data
  })
}

/**
 * 删除Dify配置
 * @param {number} id - 配置ID
 * @returns {Promise}
 */
export function deleteDifyConfig(id) {
  return request({
    url: `/dify/config/${id}`,
    method: 'delete'
  })
}

/**
 * 启用Dify配置
 * @param {number} id - 配置ID
 * @returns {Promise}
 */
export function enableDifyConfig(id) {
  return request({
    url: `/dify/config/${id}/enable`,
    method: 'put'
  })
}

/**
 * 禁用Dify配置
 * @param {number} id - 配置ID
 * @returns {Promise}
 */
export function disableDifyConfig(id) {
  return request({
    url: `/dify/config/${id}/disable`,
    method: 'put'
  })
}

/**
 * 测试Dify连接
 * @param {Object} data - 测试配置
 * @param {string} data.apiUrl - API地址
 * @param {string} data.apiKey - API Key
 * @returns {Promise}
 */
export function testDifyConnection(data) {
  return request({
    url: '/dify/config/test',
    method: 'post',
    data: data
  })
}

/**
 * 使用当前配置测试Dify连接
 * @returns {Promise}
 */
export function testCurrentDifyConnection() {
  return request({
    url: '/dify/config/test/current',
    method: 'post'
  })
}

/**
 * 获取Dify配置状态
 * @returns {Promise}
 */
export function getDifyStatus() {
  return request({
    url: '/dify/config/status',
    method: 'get'
  })
}

