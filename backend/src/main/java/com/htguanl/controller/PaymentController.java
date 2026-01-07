package com.htguanl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htguanl.common.Result;
import com.htguanl.entity.Payment;
import com.htguanl.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public Result<List<Payment>> getPaymentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        IPage<Payment> page = paymentService.getPaymentPage(pageNum, pageSize, keyword, status, type);
        return Result.success(page.getRecords(), page.getTotal());
    }

    @GetMapping("/{id}")
    public Result<Payment> getPaymentDetail(@PathVariable Long id) {
        Payment payment = paymentService.getById(id);
        if (payment == null) {
            return Result.error("收款记录不存在");
        }
        return Result.success(payment);
    }

    @GetMapping("/contract/{contractId}")
    public Result<List<Payment>> getPaymentsByContract(@PathVariable Long contractId) {
        return Result.success(paymentService.getPaymentsByContract(contractId));
    }

    @PostMapping
    public Result<Payment> createPayment(@RequestBody Payment payment) {
        Payment created = paymentService.createPayment(payment);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
        payment.setId(id);
        Payment updated = paymentService.updatePayment(payment);
        return Result.success(updated);
    }

    @PutMapping("/{id}/confirm")
    public Result<Payment> confirmPayment(@PathVariable Long id) {
        Payment confirmed = paymentService.confirmPayment(id);
        return Result.success(confirmed);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePayment(@PathVariable Long id) {
        paymentService.removeById(id);
        return Result.success();
    }
}

