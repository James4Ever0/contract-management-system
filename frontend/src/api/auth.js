import request from '@/utils/request'

export function login(data) {
  // 支持本地登录和LDAP登录
  const params = new URLSearchParams()
  params.append('username', data.username)
  params.append('password', data.password)
  params.append('loginType', data.loginType || 'local')

  return request.post('/auth/login', null, { params })
}

export function logout() {
  return request.post('/auth/logout')
}

export function getUserInfo() {
  return request.get('/auth/info')
}

// 测试LDAP连接
export function testLdapConnection(config) {
  return request.post('/auth/ldap/test', null, { params: config })
}
