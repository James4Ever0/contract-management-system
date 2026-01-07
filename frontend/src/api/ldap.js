import request from './request'

/**
 * LDAP认证相关API
 */

/**
 * LDAP登录
 * @param {Object} data - 登录数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.loginType - 登录类型：local-本地登录，ldap-LDAP登录
 * @returns {Promise}
 */
export function ldapLogin(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    params: data
  })
}

/**
 * 测试LDAP连接
 * @param {Object} data - LDAP配置
 * @param {string} data.host - LDAP服务器地址
 * @param {number} data.port - LDAP服务器端口
 * @param {string} data.baseDn - 基础DN
 * @param {string} data.userDn - 管理员DN
 * @param {string} data.password - 管理员密码
 * @returns {Promise}
 */
export function testLdapConnection(data) {
  return request({
    url: '/auth/ldap/test',
    method: 'post',
    params: data
  })
}

/**
 * 获取LDAP配置列表
 * @returns {Promise}
 */
export function getLdapConfigList() {
  return request({
    url: '/ldap/config/list',
    method: 'get'
  })
}

/**
 * 获取LDAP配置详情
 * @param {number} id - 配置ID
 * @returns {Promise}
 */
export function getLdapConfig(id) {
  return request({
    url: `/ldap/config/${id}`,
    method: 'get'
  })
}

/**
 * 创建LDAP配置
 * @param {Object} data - LDAP配置数据
 * @returns {Promise}
 */
export function createLdapConfig(data) {
  return request({
    url: '/ldap/config',
    method: 'post',
    data: data
  })
}

/**
 * 更新LDAP配置
 * @param {number} id - 配置ID
 * @param {Object} data - LDAP配置数据
 * @returns {Promise}
 */
export function updateLdapConfig(id, data) {
  return request({
    url: `/ldap/config/${id}`,
    method: 'put',
    data: data
  })
}

/**
 * 删除LDAP配置
 * @param {number} id - 配置ID
 * @returns {Promise}
 */
export function deleteLdapConfig(id) {
  return request({
    url: `/ldap/config/${id}`,
    method: 'delete'
  })
}

/**
 * 启用LDAP配置
 * @param {number} id - 配置ID
 * @returns {Promise}
 */
export function enableLdapConfig(id) {
  return request({
    url: `/ldap/config/${id}/enable`,
    method: 'put'
  })
}

/**
 * 禁用LDAP配置
 * @param {number} id - 配置ID
 * @returns {Promise}
 */
export function disableLdapConfig(id) {
  return request({
    url: `/ldap/config/${id}/disable`,
    method: 'put'
  })
}

/**
 * 同步LDAP用户
 * @returns {Promise}
 */
export function syncLdapUsers() {
  return request({
    url: '/ldap/sync',
    method: 'post'
  })
}

/**
 * 获取LDAP用户列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getLdapUsers(params) {
  return request({
    url: '/ldap/users',
    method: 'get',
    params: params
  })
}
