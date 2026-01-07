package com.htguanl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htguanl.common.Result;
import com.htguanl.entity.Contract;
import com.htguanl.entity.ContractAiExtract;
import com.htguanl.service.ContractAiExtractService;
import com.htguanl.service.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 合同控制器
 * 支持合同管理和PDF上传AI提取功能
 */
@Slf4j
@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractAiExtractService contractAiExtractService;

    @GetMapping
    public Result<List<Contract>> getContractList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        IPage<Contract> page = contractService.getContractPage(pageNum, pageSize, keyword, status);
        return Result.success(page.getRecords(), page.getTotal());
    }

    @GetMapping("/{id}")
    public Result<Contract> getContractDetail(@PathVariable Long id) {
        Contract contract = contractService.getContractDetail(id);
        if (contract == null) {
            return Result.error("合同不存在");
        }
        return Result.success(contract);
    }

    @PostMapping
    public Result<Contract> createContract(@RequestBody Contract contract) {
        Contract created = contractService.createContract(contract);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Contract> updateContract(@PathVariable Long id, @RequestBody Contract contract) {
        contract.setId(id);
        Contract updated = contractService.updateContract(contract);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteContract(@PathVariable Long id) {
        contractService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Contract> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Contract contract = contractService.getById(id);
        if (contract == null) {
            return Result.error("合同不存在");
        }
        contract.setContractStatus(status);
        contractService.updateById(contract);
        return Result.success(contract);
    }

    @GetMapping("/expiring")
    public Result<List<Contract>> getExpiringContracts() {
        return Result.success(contractService.getExpiringContracts());
    }

    @GetMapping("/all")
    public Result<List<Contract>> getAllContracts() {
        return Result.success(contractService.list());
    }

    /**
     * 获取合同的AI提取结果
     *
     * @param contractId 合同ID
     * @return AI提取结果
     */
    @GetMapping("/{contractId}/ai-extract")
    public Result<ContractAiExtract> getAiExtractResult(@PathVariable Long contractId) {
        ContractAiExtract extract = contractAiExtractService.getByContractId(contractId);
        if (extract == null) {
            return Result.error("未找到AI提取结果");
        }
        return Result.success(extract);
    }

    /**
     * 重新触发AI提取
     *
     * @param contractId 合同ID
     * @return 提取结果
     */
    @PostMapping("/{contractId}/ai-extract/retry")
    public Result<ContractAiExtract> retryAiExtract(@PathVariable Long contractId) {
        try {
            ContractAiExtract result = contractAiExtractService.retryExtract(contractId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("重新触发AI提取失败: contractId={}, error={}", contractId, e.getMessage(), e);
            return Result.error("重新触发AI提取失败: " + e.getMessage());
        }
    }
}

