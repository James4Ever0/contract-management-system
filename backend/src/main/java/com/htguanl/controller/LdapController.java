package com.htguanl.controller;

import com.htguanl.common.Result;
import com.htguanl.config.LdapConfig;
import com.htguanl.entity.LdapConfigEntity;
import com.htguanl.mapper.LdapConfigMapper;
import com.htguanl.service.LdapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LDAP配置管理控制器
 * 提供LDAP配置的增删改查和连接测试功能
 */
@Slf4j
@RestController
@RequestMapping("/api/ldap")
@RequiredArgsConstructor
public class LdapController {

    private final LdapService ldapService;
    private final LdapConfigMapper ldapConfigMapper;
    private final LdapConfig ldapConfig;

    /**
     * 获取LDAP配置列表
     */
    @GetMapping("/config/list")
    public Result<List<LdapConfigEntity>> getConfigList() {
        List<LdapConfigEntity> list = ldapConfigMapper.selectList(null);
        return Result.success(list);
    }

    /**
     * 获取当前使用的LDAP配置
     */
    @GetMapping("/config/current")
    public Result<Map<String, Object>> getCurrentConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", ldapConfig.getEnabled());
        config.put("host", ldapConfig.getHost());
        config.put("port", ldapConfig.getPort());
        config.put("baseDn", ldapConfig.getBaseDn());
        config.put("userDn", ldapConfig.getUserDn());
        config.put("syncEnabled", ldapConfig.getSyncEnabled());
        // 密码不返回
        return Result.success(config);
    }

    /**
     * 创建LDAP配置
     */
    @PostMapping("/config")
    public Result<Void> createConfig(@RequestBody LdapConfigEntity config) {
        // 密码加密存储
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            config.setPassword(ldapService.encryptPassword(config.getPassword()));
        }
        ldapConfigMapper.insert(config);
        log.info("创建LDAP配置: {}", config.getName());
        return Result.success();
    }

    /**
     * 更新LDAP配置
     */
    @PutMapping("/config/{id}")
    public Result<Void> updateConfig(@PathVariable Long id, @RequestBody LdapConfigEntity config) {
        config.setId(id);
        // 密码加密存储
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            config.setPassword(ldapService.encryptPassword(config.getPassword()));
        }
        ldapConfigMapper.updateById(config);
        log.info("更新LDAP配置: {}", config.getName());
        return Result.success();
    }

    /**
     * 删除LDAP配置
     */
    @DeleteMapping("/config/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        ldapConfigMapper.deleteById(id);
        log.info("删除LDAP配置: {}", id);
        return Result.success();
    }

    /**
     * 启用LDAP配置
     */
    @PutMapping("/config/{id}/enable")
    public Result<Void> enableConfig(@PathVariable Long id) {
        LdapConfigEntity config = ldapConfigMapper.selectById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }
        config.setEnabled(true);
        ldapConfigMapper.updateById(config);
        log.info("启用LDAP配置: {}", config.getName());
        return Result.success();
    }

    /**
     * 禁用LDAP配置
     */
    @PutMapping("/config/{id}/disable")
    public Result<Void> disableConfig(@PathVariable Long id) {
        LdapConfigEntity config = ldapConfigMapper.selectById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }
        config.setEnabled(false);
        ldapConfigMapper.updateById(config);
        log.info("禁用LDAP配置: {}", config.getName());
        return Result.success();
    }

    /**
     * 测试LDAP连接
     */
    @PostMapping("/test-connection")
    public Result<Map<String, Object>> testConnection(@RequestBody Map<String, String> params) {
        String host = params.get("host");
        Integer port = Integer.parseInt(params.getOrDefault("port", "389"));
        String baseDn = params.get("baseDn");
        String userDn = params.get("userDn");
        String password = params.get("password");

        Map<String, Object> result = ldapService.testConnection(host, port, baseDn, userDn, password);
        return Result.success(result);
    }

    /**
     * 测试当前配置的LDAP连接
     */
    @PostMapping("/test-current-connection")
    public Result<Map<String, Object>> testCurrentConnection() {
        Map<String, Object> result = ldapService.testConnection(
            ldapConfig.getHost(),
            ldapConfig.getPort(),
            ldapConfig.getBaseDn(),
            ldapConfig.getUserDn(),
            ldapConfig.getPassword()
        );
        return Result.success(result);
    }

    /**
     * 同步LDAP用户到本地数据库
     */
    @PostMapping("/sync-users")
    public Result<Map<String, Object>> syncUsers() {
        Map<String, Object> result = ldapService.syncUsersFromLdap();
        return Result.success(result);
    }

    /**
     * 验证LDAP用户凭据
     */
    @PostMapping("/authenticate")
    public Result<Map<String, Object>> authenticate(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        Map<String, Object> result = new HashMap<>();
        boolean authenticated = ldapService.authenticate(username, password);
        result.put("success", authenticated);
        result.put("message", authenticated ? "认证成功" : "认证失败");
        return Result.success(result);
    }
}
