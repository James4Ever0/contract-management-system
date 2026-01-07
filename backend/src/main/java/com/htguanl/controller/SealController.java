package com.htguanl.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htguanl.common.Result;
import com.htguanl.entity.Seal;
import com.htguanl.entity.SealBorrow;
import com.htguanl.service.SealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seals")
public class SealController {

    @Autowired
    private SealService sealService;

    @GetMapping
    public Result<List<Seal>> getSealList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        IPage<Seal> page = sealService.getSealPage(pageNum, pageSize, keyword, status);
        return Result.success(page.getRecords(), page.getTotal());
    }

    @GetMapping("/{id}")
    public Result<Seal> getSealDetail(@PathVariable Long id) {
        return Result.success(sealService.getById(id));
    }

    @GetMapping("/all")
    public Result<List<Seal>> getAllSeals() {
        return Result.success(sealService.list());
    }

    @GetMapping("/stats-table")
    public Result<String> getAnalysisTable() {
        // TODO: 实现下载分析表格的功能
        return Result.success("合同类型,合同编号,合同名称,合同客户,合同金额,收款状态,收款金额,未收款金额,到期时间,逾期时间,备注\n销售合同,HT-2023-00123,年度软件许可协议,北京星辰科技有限公司,500000.00,部分收款,300000.00,200000.00,2023-06-30,,二期款即将到期\n服务合同,FW-2022-00087,系统维护与支持服务,上海云海信息技术有限公司,180000.00,已全额收款,180000.00,0.00,2022-12-31,,\n采购合同,CG-2023-00215,服务器硬件采购,深圳创新数据中心,1200000.00,未收款,0.00,1200000.00,2023-09-15,2023-10-01,客户反馈资金周转问题，正在沟通\n工程合同,GC-2021-00056,数据中心建设工程,广州华南建设集团,3500000.00,部分收款,2000000.00,1500000.00,2022-05-31,2022-08-15,尾款涉及部分验收争议\n框架合同,KJ-2023-00333,年度物流运输框架协议,成都快运通物流有限公司,800000.00,已全额收款,800000.00,0.00,2023-12-31,,\n销售合同,HT-2023-00482,CRM系统定制开发,杭州智创软件有限公司,650000.00,未收款,0.00,650000.00,2023-08-20,2023-09-10,项目已交付，等待客户内部付款流程\n咨询合同,ZX-2022-00091,战略管理咨询服务,南京先锋企业管理顾问,450000.00,已全额收款,450000.00,0.00,2022-11-30,,"
);
    }

    @GetMapping("/available")
    public Result<List<Seal>> getAvailableSeals() {
        LambdaQueryWrapper<Seal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Seal::getSealStatus, "AVAILABLE");
        return Result.success(sealService.list(wrapper));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getSealStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", sealService.count());
        stats.put("available", sealService.countAvailableSeals());
        stats.put("borrowing", sealService.countBorrowing());
        return Result.success(stats);
    }

    @PostMapping
    public Result<Seal> createSeal(@RequestBody Seal seal) {
        return Result.success(sealService.createSeal(seal));
    }

    @PutMapping("/{id}")
    public Result<Seal> updateSeal(@PathVariable Long id, @RequestBody Seal seal) {
        seal.setId(id);
        sealService.updateById(seal);
        return Result.success(sealService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSeal(@PathVariable Long id) {
        sealService.removeById(id);
        return Result.success();
    }

    // 印章借用相关接口
    @GetMapping("/borrows")
    public Result<List<SealBorrow>> getBorrowList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        IPage<SealBorrow> page = sealService.getBorrowPage(pageNum, pageSize, keyword, status);
        return Result.success(page.getRecords(), page.getTotal());
    }

    @GetMapping("/borrows/{id}")
    public Result<SealBorrow> getBorrowDetail(@PathVariable Long id) {
        return Result.success(sealService.getBorrowDetail(id));
    }

    @GetMapping("/{sealId}/borrows")
    public Result<List<SealBorrow>> getBorrowsBySeal(@PathVariable Long sealId) {
        return Result.success(sealService.getBorrowsBySeal(sealId));
    }

    @PostMapping("/borrows")
    public Result<SealBorrow> createBorrow(@RequestBody SealBorrow borrow) {
        try {
            return Result.success(sealService.createBorrow(borrow));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/borrows/{id}/return")
    public Result<SealBorrow> returnSeal(@PathVariable Long id) {
        try {
            return Result.success(sealService.returnSeal(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}

