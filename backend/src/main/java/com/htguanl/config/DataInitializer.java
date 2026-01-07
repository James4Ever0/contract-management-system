package com.htguanl.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htguanl.entity.SysUser;
import com.htguanl.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 在应用启动时自动创建默认管理员用户和执行数据库迁移
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        migrateDatabase();
        initAdminUser();
    }

    /**
     * 数据库迁移 - 添加缺失的列
     */
    private void migrateDatabase() {
        try {
            // 添加 biz_contract_attachment 表的缺失列
            addColumnIfNotExists("biz_contract_attachment", "ai_extract_status", "VARCHAR(20) DEFAULT 'PENDING'");
            addColumnIfNotExists("biz_contract_attachment", "upload_status", "VARCHAR(20) DEFAULT 'SUCCESS'");
            addColumnIfNotExists("biz_contract_attachment", "error_message", "TEXT");

            // 添加 sys_ldap_config 表的缺失列
            addColumnIfNotExists("sys_ldap_config", "config_name", "VARCHAR(100)");

            // 添加 sys_dify_config 表的缺失列
            addColumnIfNotExists("sys_dify_config", "config_name", "VARCHAR(100)");

            // 添加 biz_seal_borrow 表的缺失列
            addColumnIfNotExists("biz_seal_borrow", "borrow_status", "VARCHAR(20) DEFAULT 'BORROWED'");
            addColumnIfNotExists("biz_seal_borrow", "contract_id", "BIGINT");
            addColumnIfNotExists("biz_seal_borrow", "borrower_id", "BIGINT");
            addColumnIfNotExists("biz_seal_borrow", "borrow_reason", "TEXT");

            log.info("数据库迁移完成");
        } catch (Exception e) {
            log.warn("数据库迁移时出现警告: {}", e.getMessage());
        }
    }

    /**
     * 如果列不存在则添加
     */
    private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition) {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName, columnName);
            if (count == null || count == 0) {
                String alterSql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
                jdbcTemplate.execute(alterSql);
                log.info("已添加列: {}.{}", tableName, columnName);
            }
        } catch (Exception e) {
            log.debug("列 {}.{} 可能已存在: {}", tableName, columnName, e.getMessage());
        }
    }

    /**
     * 初始化管理员用户
     */
    private void initAdminUser() {
        try {
            String adminUsername = "admin";
            String adminPassword = "admin123";

            // 检查是否已存在admin用户
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getUsername, adminUsername);
            SysUser existingUser = sysUserMapper.selectOne(queryWrapper);

            if (existingUser == null) {
                // 创建新的admin用户
                SysUser adminUser = new SysUser();
                adminUser.setUsername(adminUsername);
                adminUser.setPassword(passwordEncoder.encode(adminPassword));
                adminUser.setRealName("系统管理员");
                adminUser.setEmail("admin@htguanl.com");
                adminUser.setStatus(1);

                sysUserMapper.insert(adminUser);
                log.info("默认管理员用户已创建: username={}, password={}", adminUsername, adminPassword);
            } else {
                // 更新密码（确保密码正确）
                existingUser.setPassword(passwordEncoder.encode(adminPassword));
                sysUserMapper.updateById(existingUser);
                log.info("管理员用户密码已更新: username={}, password={}", adminUsername, adminPassword);
            }
        } catch (Exception e) {
            log.error("初始化管理员用户失败: {}", e.getMessage(), e);
        }
    }
}

