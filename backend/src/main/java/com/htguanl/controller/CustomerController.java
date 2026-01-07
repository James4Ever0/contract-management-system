package com.htguanl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htguanl.common.Result;
import com.htguanl.entity.Customer;
import com.htguanl.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public Result<List<Customer>> getCustomerList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        IPage<Customer> page = customerService.getCustomerPage(pageNum, pageSize, keyword, type);
        return Result.success(page.getRecords(), page.getTotal());
    }

    @GetMapping("/{id}")
    public Result<Customer> getCustomerDetail(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        return Result.success(customer);
    }

    @GetMapping("/all")
    public Result<List<Customer>> getAllCustomers() {
        return Result.success(customerService.list());
    }

    @PostMapping
    public Result<Customer> createCustomer(@RequestBody Customer customer) {
        Customer created = customerService.createCustomer(customer);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id);
        Customer updated = customerService.updateCustomer(customer);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCustomer(@PathVariable Long id) {
        customerService.removeById(id);
        return Result.success();
    }
}

