import request from './request'

export function getSeals(params) {
  return request.get('/seals', { params })
}

export function getAllSeals() {
  return request.get('/seals/all')
}

export function getAvailableSeals() {
  return request.get('/seals/available')
}

export function getSealDetail(id) {
  return request.get(`/seals/${id}`)
}

export function getSealStats() {
  return request.get('/seals/stats')
}

export function createSeal(data) {
  return request.post('/seals', data)
}

export function updateSeal(id, data) {
  return request.put(`/seals/${id}`, data)
}

export function deleteSeal(id) {
  return request.delete(`/seals/${id}`)
}

// 印章借用
export function getSealBorrows(params) {
  return request.get('/seals/borrows', { params })
}

export function getSealBorrowDetail(id) {
  return request.get(`/seals/borrows/${id}`)
}

export function getAnalysisTable(){
  return request.get('/seals/stats-table')
}

export function getBorrowsBySeal(sealId) {
  return request.get(`/seals/${sealId}/borrows`)
}

export function createSealBorrow(data) {
  return request.post('/seals/borrows', data)
}

export function returnSeal(id) {
  return request.put(`/seals/borrows/${id}/return`)
}

