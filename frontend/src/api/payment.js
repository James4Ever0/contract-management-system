import request from '@/utils/request'

export function getPayments(params) {
  return request.get('/payments', { params })
}

export function getPaymentDetail(id) {
  return request.get(`/payments/${id}`)
}

export function getPaymentsByContract(contractId) {
  return request.get(`/payments/contract/${contractId}`)
}

export function createPayment(data) {
  return request.post('/payments', data)
}

export function updatePayment(id, data) {
  return request.put(`/payments/${id}`, data)
}

export function confirmPayment(id) {
  return request.put(`/payments/${id}/confirm`)
}

export function deletePayment(id) {
  return request.delete(`/payments/${id}`)
}

