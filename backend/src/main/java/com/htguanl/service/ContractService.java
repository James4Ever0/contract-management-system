package com.htguanl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htguanl.entity.Contract;
import com.htguanl.mapper.ContractMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ContractService extends ServiceImpl<ContractMapper, Contract> {

    public IPage<Contract> getContractPage(Integer pageNum, Integer pageSize, String keyword, String status) {
        Page<Contract> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectContractPage(page, keyword, status);
    }

    public Contract getContractDetail(Long id) {
        return baseMapper.selectContractWithCustomer(id);
    }

    @Transactional
    public Contract createContract(Contract contract) {
        // 生成合同编号
        String contractNo = generateContractNo();
        contract.setContractNo(contractNo);
        contract.setContractStatus("DRAFT");
        save(contract);
        return contract;
    }

    @Transactional
    public Contract updateContract(Contract contract) {
        updateById(contract);
        return getContractDetail(contract.getId());
    }

    public List<Contract> getExpiringContracts() {
        return baseMapper.selectExpiringContracts();
    }

    public List<Contract> getRecentContracts(int limit) {
        return baseMapper.selectRecentContracts(limit);
    }

    public Long countByStatus(String status) {
        return baseMapper.countByStatus(status);
    }

    public Long countExpiring() {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysLater = now.plusDays(30);
        wrapper.between(Contract::getEndDate, now, thirtyDaysLater);
        return count(wrapper);
    }

    public Long countExpired() {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Contract::getEndDate, LocalDate.now());
        wrapper.notIn(Contract::getContractStatus, "COMPLETED", "TERMINATED", "ARCHIVED");
        return count(wrapper);
    }

    public BigDecimal getTotalAmount() {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        List<Contract> contracts = list(wrapper);
        return contracts.stream()
                .map(Contract::getContractAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Map<String, Object>> getContractTypeDistribution() {
        return baseMapper.selectContractTypeDistribution();
    }

    private String generateContractNo() {
        String prefix = "HT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long todayCount = count(new LambdaQueryWrapper<Contract>()
                .likeRight(Contract::getContractNo, prefix));
        return prefix + String.format("%04d", todayCount + 1);
    }
}

