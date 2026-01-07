package com.htguanl.controller;

import com.htguanl.common.Result;
import com.htguanl.config.DifyConfig;
import com.htguanl.entity.DifyConfigEntity;
import com.htguanl.mapper.DifyConfigMapper;
import com.htguanl.service.DifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dify配置管理控制器
 * 提供Dify API配置的增删改查和连接测试功能
 */
@Slf4j
@RestController
@RequestMapping("/api/dify/config")
@RequiredArgsConstructor
public class DifyConfigController {

    private final DifyConfigMapper difyConfigMapper;
    private final DifyService difyService;
    private final DifyConfig difyConfig;

    /**
     * 获取Dify配置列表
     */
    @GetMapping("/list")
    public Result<List<DifyConfigEntity>> list() {
        List<DifyConfigEntity> list = difyConfigMapper.selectList(null);
        return Result.success(list);
    }

    /**
     * 获取当前激活的Dify配置
     */
    @GetMapping("/active")
    public Result<DifyConfigEntity> getActiveConfig() {
        DifyConfigEntity config = difyConfigMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DifyConfigEntity>()
                .eq(DifyConfigEntity::getEnabled, true)
                .last("LIMIT 1")
        );
        return Result.success(config);
    }

    /**
     * 获取Dify配置详情
     */
    @GetMapping("/{id}")
    public Result<DifyConfigEntity> getById(@PathVariable Long id) {
        DifyConfigEntity config = difyConfigMapper.selectById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }
        // 脱敏处理API Key
        DifyConfigEntity maskedConfig = new DifyConfigEntity();
        maskedConfig.setId(config.getId());
        maskedConfig.setName(config.getName());
        maskedConfig.setApiUrl(config.getApiUrl());
        maskedConfig.setApiKey(maskApiKey(config.getApiKey()));
        maskedConfig.setEnabled(config.getEnabled());
        maskedConfig.setRemark(config.getRemark());
        maskedConfig.setCreateTime(config.getCreateTime());
        maskedConfig.setUpdateTime(config.getUpdateTime());
        return Result.success(maskedConfig);
    }

    /**
     * 创建Dify配置
     */
    @PostMapping
    public Result<DifyConfigEntity> create(@RequestBody DifyConfigEntity config) {
        // 验证必填字段
        if (config.getName() == null || config.getName().trim().isEmpty()) {
            return Result.error("配置名称不能为空");
        }
        if (config.getApiUrl() == null || config.getApiUrl().trim().isEmpty()) {
            return Result.error("API地址不能为空");
        }
        if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            return Result.error("API Key不能为空");
        }

        // 如果设置为启用，先禁用其他配置
        if (Boolean.TRUE.equals(config.getEnabled())) {
            difyConfigMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<DifyConfigEntity>()
                    .set(DifyConfigEntity::getEnabled, false)
            );
        }

        difyConfigMapper.insert(config);
        log.info("创建Dify配置成功: id={}, name={}", config.getId(), config.getName());
        return Result.success(config);
    }

    /**
     * 更新Dify配置
     */
    @PutMapping("/{id}")
    public Result<DifyConfigEntity> update(@PathVariable Long id, @RequestBody DifyConfigEntity config) {
        DifyConfigEntity existing = difyConfigMapper.selectById(id);
        if (existing == null) {
            return Result.error("配置不存在");
        }

        // 如果设置为启用，先禁用其他配置
        if (Boolean.TRUE.equals(config.getEnabled())) {
            difyConfigMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<DifyConfigEntity>()
                    .set(DifyConfigEntity::getEnabled, false)
                    .ne(DifyConfigEntity::getId, id)
            );
        }

        config.setId(id);
        difyConfigMapper.updateById(config);
        log.info("更新Dify配置成功: id={}, name={}", id, config.getName());
        return Result.success(config);
    }

    /**
     * 删除Dify配置
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        DifyConfigEntity config = difyConfigMapper.selectById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }

        difyConfigMapper.deleteById(id);
        log.info("删除Dify配置成功: id={}, name={}", id, config.getName());
        return Result.success();
    }

    /**
     * 测试Dify连接
     */
    @PostMapping("/test")
    public Result<Map<String, Object>> testConnection(@RequestBody Map<String, String> request) {
        String apiUrl = request.get("apiUrl");
        String apiKey = request.get("apiKey");

        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            return Result.error("API地址不能为空");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return Result.error("API Key不能为空");
        }

        try {
            Map<String, Object> result = difyService.testConnection(apiUrl, apiKey);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试Dify连接失败", e);
            return Result.error("连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 使用当前配置测试Dify连接
     */
    @PostMapping("/test/current")
    public Result<Map<String, Object>> testCurrentConnection() {
        if (!Boolean.TRUE.equals(difyConfig.getEnabled())) {
            return Result.error("Dify功能未启用");
        }

        try {
            Map<String, Object> result = difyService.testConnection(
                difyConfig.getApiUrl(),
                difyConfig.getApiKey()
            );
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试Dify连接失败", e);
            return Result.error("连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 启用配置
     */
    @PutMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        DifyConfigEntity config = difyConfigMapper.selectById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }

        // 禁用所有配置
        difyConfigMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<DifyConfigEntity>()
                .set(DifyConfigEntity::getEnabled, false)
        );

        // 启用指定配置
        difyConfigMapper.updateById(new DifyConfigEntity() {{
            setId(id);
            setEnabled(true);
        }});

        log.info("启用Dify配置成功: id={}, name={}", id, config.getName());
        return Result.success();
    }

    /**
     * 禁用配置
     */
    @PutMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        DifyConfigEntity config = difyConfigMapper.selectById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }

        difyConfigMapper.updateById(new DifyConfigEntity() {{
            setId(id);
            setEnabled(false);
        }});

        log.info("禁用Dify配置成功: id={}, name={}", id, config.getName());
        return Result.success();
    }

    /**
     * 获取Dify配置状态
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", difyConfig.getEnabled());
        status.put("apiUrl", difyConfig.getApiUrl());
        status.put("hasApiKey", difyConfig.getApiKey() != null && !difyConfig.getApiKey().isEmpty());
        return Result.success(status);
    }

    /**
     * API Key脱敏处理
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
