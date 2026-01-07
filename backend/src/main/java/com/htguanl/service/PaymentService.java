package com.htguanl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htguanl.entity.Payment;
import com.htguanl.mapper.PaymentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {

    public IPage<Payment> getPaymentPage(Integer pageNum, Integer pageSize, String keyword, String status, String type) {
        Page<Payment> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPaymentPage(page, keyword, status, type);
    }

    @Transactional
    public Payment createPayment(Payment payment) {
        String paymentNo = generatePaymentNo(payment.getPaymentType());
        payment.setPaymentNo(paymentNo);
        payment.setPaymentStatus("UNPAID");
        save(payment);
        return payment;
    }

    @Transactional
    public Payment updatePayment(Payment payment) {
        updateById(payment);
        return getById(payment.getId());
    }

    @Transactional
    public Payment confirmPayment(Long id) {
        Payment payment = getById(id);
        if (payment != null) {
            payment.setPaymentStatus("PAID");
            payment.setActualPaymentDate(LocalDate.now());
            updateById(payment);
        }
        return payment;
    }

    public List<Payment> getPaymentsByContract(Long contractId) {
        return baseMapper.selectByContractId(contractId);
    }

    public BigDecimal getTotalReceived() {
        return baseMapper.selectTotalReceived();
    }

    public List<Map<String, Object>> getPaymentTrend() {
        return baseMapper.selectPaymentTrend();
    }

    public BigDecimal getOutstandingAmount(BigDecimal totalAmount) {
        BigDecimal received = getTotalReceived();
        return totalAmount.subtract(received != null ? received : BigDecimal.ZERO);
    }

    private String generatePaymentNo(String type) {
        String prefix = ("RECEIVE".equals(type) ? "SK" : "FK") 
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long todayCount = count(new LambdaQueryWrapper<Payment>()
                .likeRight(Payment::getPaymentNo, prefix));
        return prefix + String.format("%04d", todayCount + 1);
    }
}

