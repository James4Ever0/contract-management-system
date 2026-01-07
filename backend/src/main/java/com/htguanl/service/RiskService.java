package com.htguanl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htguanl.entity.Contract;
import com.htguanl.entity.ContractRisk;
import com.htguanl.mapper.ContractMapper;
import com.htguanl.mapper.ContractRiskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RiskService extends ServiceImpl<ContractRiskMapper, ContractRisk> {

    @Autowired
    private ContractMapper contractMapper;

    public IPage<ContractRisk> getRiskPage(Integer pageNum, Integer pageSize, String keyword, String level, String status) {
        Page<ContractRisk> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectRiskPage(page, keyword, level, status);
    }

    @Transactional
    public ContractRisk processRisk(Long id, String handleMeasure) {
        ContractRisk risk = getById(id);
        if (risk != null) {
            risk.setRiskStatus("PROCESSING");
            risk.setHandleMeasure(handleMeasure);
            risk.setUpdateTime(LocalDateTime.now());
            updateById(risk);
        }
        return risk;
    }

    @Transactional
    public ContractRisk resolveRisk(Long id) {
        ContractRisk risk = getById(id);
        if (risk != null) {
            risk.setRiskStatus("RESOLVED");
            risk.setIsResolved(1);  // 1表示已解决
            risk.setResolveTime(LocalDateTime.now());
            updateById(risk);
        }
        return risk;
    }

    @Transactional
    public ContractRisk ignoreRisk(Long id) {
        ContractRisk risk = getById(id);
        if (risk != null) {
            risk.setRiskStatus("IGNORED");
            risk.setUpdateTime(LocalDateTime.now());
            updateById(risk);
        }
        return risk;
    }

    @Transactional
    public int scanRisks() {
        int count = 0;
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(30);

        // 扫描即将到期的合同
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contract::getContractStatus, "EXECUTING")
               .le(Contract::getEndDate, warningDate)
               .ge(Contract::getEndDate, today);
        List<Contract> expiringContracts = contractMapper.selectList(wrapper);

        for (Contract contract : expiringContracts) {
            // 检查是否已存在该风险
            LambdaQueryWrapper<ContractRisk> riskWrapper = new LambdaQueryWrapper<>();
            riskWrapper.eq(ContractRisk::getContractId, contract.getId())
                       .eq(ContractRisk::getRiskType, "CONTRACT_EXPIRING")
                       .eq(ContractRisk::getIsResolved, 0);  // 0表示未解决
            if (count(riskWrapper) == 0) {
                ContractRisk risk = new ContractRisk();
                risk.setContractId(contract.getId());
                risk.setRiskType("CONTRACT_EXPIRING");
                risk.setRiskLevel("MEDIUM");
                risk.setRiskDescription("合同将于" + contract.getEndDate() + "到期");
                risk.setRiskStatus("PENDING");
                risk.setIsResolved(0);  // 0表示未解决
                risk.setCreateTime(LocalDateTime.now());
                save(risk);
                count++;
            }
        }
        return count;
    }

    public Long countUnresolved() {
        LambdaQueryWrapper<ContractRisk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractRisk::getIsResolved, 0);  // 0表示未解决
        return count(wrapper);
    }
}

