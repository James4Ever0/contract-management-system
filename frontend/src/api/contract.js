import request from '@/utils/request'

export function getContracts(params) {
  return request.get('/contracts', { params })
}

export function getContractList(params) {
  return request.get('/contracts', { params })
}

export function getAllContracts() {
  return request.get('/contracts/all')
}


export function getContractDetail(id) {
  return request.get(`/contracts/${id}`)
}

export function createContract(data) {
  return request.post('/contracts', data)
}

export function updateContract(id, data) {
  return request.put(`/contracts/${id}`, data)
}

export function deleteContract(id) {
  return request.delete(`/contracts/${id}`)
}

export function updateContractStatus(id, status) {
  return request.put(`/contracts/${id}/status`, null, { params: { status } })
}

export function getExpiringContracts() {
  return request.get('/contracts/expiring')
}

