package com.htguanl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htguanl.config.LdapConfig;
import com.htguanl.entity.SysUser;
import com.htguanl.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.time.LocalDateTime;
import java.util.List;

/**
 * LDAP用户同步服务
 * 负责将LDAP用户同步到本地数据库
 */
@Slf4j
@Service
public class LdapUserSyncService {

    @Autowired(required = false)
    private LdapTemplate ldapTemplate;

    @Autowired
    private LdapConfig ldapConfig;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 同步所有LDAP用户到本地数据库
     */
    @Transactional
    public void syncAllUsers() {
        if (!ldapConfig.getEnabled() || !ldapConfig.getSyncEnabled()) {
            log.warn("LDAP同步未启用，跳过用户同步");
            return;
        }

        log.info("开始同步LDAP用户到本地数据库");

        try {
            // 查询LDAP中的所有用户
            List<SysUser> ldapUsers = ldapTemplate.search(
                LdapQueryBuilder.query()
                    .base(ldapConfig.getBaseDn())
                    .where("objectClass").is("person"),
                new UserAttributesMapper()
            );

            log.info("从LDAP查询到 {} 个用户", ldapUsers.size());

            // 同步到本地数据库
            for (SysUser ldapUser : ldapUsers) {
                syncUser(ldapUser);
            }

            log.info("LDAP用户同步完成");
        } catch (Exception e) {
            log.error("LDAP用户同步失败", e);
            throw new RuntimeException("LDAP用户同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步单个用户
     */
    @Transactional
    public SysUser syncUser(SysUser ldapUser) {
        // 检查本地是否已存在该用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, ldapUser.getUsername());
        SysUser localUser = sysUserMapper.selectOne(wrapper);

        if (localUser == null) {
            // 新用户，插入数据库
            ldapUser.setCreateTime(LocalDateTime.now());
            ldapUser.setUpdateTime(LocalDateTime.now());
            ldapUser.setStatus(1); // 默认启用
            sysUserMapper.insert(ldapUser);
            log.info("新增LDAP用户: {}", ldapUser.getUsername());
        } else {
            // 已存在用户，更新LDAP相关信息
            localUser.setLdapDn(ldapUser.getLdapDn());
            localUser.setRealName(ldapUser.getRealName());
            localUser.setEmail(ldapUser.getEmail());
            localUser.setPhone(ldapUser.getPhone());
            localUser.setLdapSyncTime(LocalDateTime.now());
            localUser.setUpdateTime(LocalDateTime.now());
            sysUserMapper.updateById(localUser);
            log.info("更新LDAP用户: {}", ldapUser.getUsername());
            return localUser;
        }

        return ldapUser;
    }

    /**
     * 根据用户名同步用户（简化方法）
     */
    @Transactional
    public SysUser syncUser(String username) {
        return syncUserByUsername(username);
    }

    /**
     * 根据用户名从LDAP同步用户
     */
    @Transactional
    public SysUser syncUserByUsername(String username) {
        if (!ldapConfig.getEnabled()) {
            log.warn("LDAP未启用，无法同步用户: {}", username);
            return null;
        }

        try {
            List<SysUser> users = ldapTemplate.search(
                LdapQueryBuilder.query()
                    .base(ldapConfig.getBaseDn())
                    .where("objectClass").is("person")
                    .and("uid").is(username),
                new UserAttributesMapper()
            );

            if (users.isEmpty()) {
                log.warn("LDAP中未找到用户: {}", username);
                return null;
            }

            return syncUser(users.get(0));
        } catch (Exception e) {
            log.error("同步LDAP用户失败: {}", username, e);
            throw new RuntimeException("同步LDAP用户失败: " + e.getMessage());
        }
    }

    /**
     * LDAP属性映射器
     */
    private class UserAttributesMapper implements AttributesMapper<SysUser> {
        @Override
        public SysUser mapFromAttributes(Attributes attributes) throws NamingException {
            SysUser user = new SysUser();

            // 用户名 (uid)
            if (attributes.get("uid") != null) {
                user.setUsername((String) attributes.get("uid").get());
            }

            // 真实姓名 (cn)
            if (attributes.get("cn") != null) {
                user.setRealName((String) attributes.get("cn").get());
            }

            // 邮箱 (mail)
            if (attributes.get("mail") != null) {
                user.setEmail((String) attributes.get("mail").get());
            }

            // 电话 (telephoneNumber)
            if (attributes.get("telephoneNumber") != null) {
                user.setPhone((String) attributes.get("telephoneNumber").get());
            }

            // DN (distinguishedName)
            if (attributes.get("distinguishedName") != null) {
                user.setLdapDn((String) attributes.get("distinguishedName").get());
            }

            // 设置LDAP同步时间
            user.setLdapSyncTime(LocalDateTime.now());

            return user;
        }
    }

    /**
     * 测试LDAP连接
     */
    public boolean testConnection() {
        try {
            ldapTemplate.lookup(ldapConfig.getBaseDn());
            log.info("LDAP连接测试成功");
            return true;
        } catch (Exception e) {
            log.error("LDAP连接测试失败", e);
            return false;
        }
    }
}
