import request from '@/utils/request'

export function getRisks(params) {
  return request.get('/risks', { params })
}

export function processRisk(id, data) {
  return request.post(`/risks/${id}/process`, data)
}

export function resolveRisk(id) {
  return request.post(`/risks/${id}/resolve`)
}

export function ignoreRisk(id) {
  return request.post(`/risks/${id}/ignore`)
}

export function scanRisks() {
  return request.post('/risks/scan')
}

export function getUnresolvedCount() {
  return request.get('/risks/count/unresolved')
}

