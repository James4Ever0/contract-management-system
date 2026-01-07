package com.htguanl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htguanl.entity.Customer;
import com.htguanl.mapper.CustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class CustomerService extends ServiceImpl<CustomerMapper, Customer> {

    public IPage<Customer> getCustomerPage(Integer pageNum, Integer pageSize, String keyword, String type) {
        Page<Customer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(Customer::getCustomerCode, keyword)
                .or().like(Customer::getCustomerName, keyword)
                .or().like(Customer::getContactPerson, keyword)
            );
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Customer::getCustomerType, type);
        }
        wrapper.orderByDesc(Customer::getCreateTime);
        
        return page(page, wrapper);
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        String customerCode = generateCustomerCode();
        customer.setCustomerCode(customerCode);
        customer.setStatus(1);
        save(customer);
        return customer;
    }

    @Transactional
    public Customer updateCustomer(Customer customer) {
        updateById(customer);
        return getById(customer.getId());
    }

    public Long countTotal() {
        return count();
    }

    public Long countActive() {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getStatus, 1);
        return count(wrapper);
    }

    private String generateCustomerCode() {
        String prefix = "CUS" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long todayCount = count(new LambdaQueryWrapper<Customer>()
                .likeRight(Customer::getCustomerCode, prefix));
        return prefix + String.format("%04d", todayCount + 1);
    }
}

