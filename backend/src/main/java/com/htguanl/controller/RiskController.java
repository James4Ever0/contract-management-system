package com.htguanl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htguanl.common.Result;
import com.htguanl.entity.ContractRisk;
import com.htguanl.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risks")
public class RiskController {

    @Autowired
    private RiskService riskService;

    /**
     * 获取风险列表
     *
     * @param pageNum     页码
     * @param pageSize  每页条数
     * @param keyword  关键字
     * @param level     风等级
     * @param status   状态
     * @return List<ContractRisk>    */
    
    @GetMapping
    public Result<List<ContractRisk>> getRiskList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status) {
        IPage<ContractRisk> page = riskService.getRiskPage(pageNum, pageSize, keyword, level, status);
        return Result.success(page.getRecords(), page.getTotal());
    }

    @GetMapping("/{id}")
    public Result<ContractRisk> getRiskDetail(@PathVariable Long id) {
        return Result.success(riskService.getById(id));
    }

    @PostMapping("/{id}/process")
    public Result<ContractRisk> processRisk(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String handleMeasure = (String) params.get("handleMeasure");
        return Result.success(riskService.processRisk(id, handleMeasure));
    }

    @PostMapping("/{id}/resolve")
    public Result<ContractRisk> resolveRisk(@PathVariable Long id) {
        return Result.success(riskService.resolveRisk(id));
    }

    @PostMapping("/{id}/ignore")
    public Result<ContractRisk> ignoreRisk(@PathVariable Long id) {
        return Result.success(riskService.ignoreRisk(id));
    }

    @PostMapping("/scan")
    public Result<Integer> scanRisks() {
        int count = riskService.scanRisks();
        return Result.success(count);
    }

    @GetMapping("/count/unresolved")
    public Result<Long> countUnresolved() {
        return Result.success(riskService.countUnresolved());
    }
}

