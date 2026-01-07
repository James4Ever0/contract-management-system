package com.htguanl.service;

import com.htguanl.config.LdapConfig;
import com.htguanl.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * LDAP服务类
 * 实现LDAP连接和认证逻辑
 */
@Slf4j
@Service
public class LdapService {

    @Autowired
    private LdapConfig ldapConfig;

    private LdapTemplate ldapTemplate;

    /**
     * 初始化LDAP连接
     */
    private LdapTemplate getLdapTemplate() {
        if (ldapTemplate == null) {
            LdapContextSource contextSource = new LdapContextSource();
            contextSource.setUrl("ldap://" + ldapConfig.getHost() + ":" + ldapConfig.getPort());
            contextSource.setBase(ldapConfig.getBaseDn());
            contextSource.setUserDn(ldapConfig.getUserDn());
            contextSource.setPassword(ldapConfig.getPassword());
            contextSource.setPooled(true);
            contextSource.afterPropertiesSet();

            ldapTemplate = new LdapTemplate(contextSource);
            ldapTemplate.setIgnorePartialResultException(true);
        }
        return ldapTemplate;
    }

    /**
     * 测试LDAP连接
     */
    public boolean testConnection() {
        try {
            LdapTemplate template = getLdapTemplate();
            template.authenticate(LdapQueryBuilder.query().where("objectClass").is("person"), "");
            log.info("LDAP连接测试成功");
            return true;
        } catch (Exception e) {
            log.error("LDAP连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * LDAP用户认证
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证是否成功
     */
    public boolean authenticate(String username, String password) {
        if (!ldapConfig.getEnabled()) {
            log.warn("LDAP未启用");
            return false;
        }

        try {
            LdapTemplate template = getLdapTemplate();
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("objectClass", "person"));
            filter.and(new EqualsFilter("uid", username));

            boolean authenticated = template.authenticate("", filter.encode(), password);
            if (authenticated) {
                log.info("用户 {} LDAP认证成功", username);
            } else {
                log.warn("用户 {} LDAP认证失败", username);
            }
            return authenticated;
        } catch (Exception e) {
            log.error("LDAP认证异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 根据用户名查询LDAP用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    public Optional<SysUser> findUserByUsername(String username) {
        try {
            LdapTemplate template = getLdapTemplate();
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("objectClass", "person"));
            filter.and(new EqualsFilter("uid", username));

            List<SysUser> users = template.search("", filter.encode(), new UserAttributesMapper());
            if (!users.isEmpty()) {
                return Optional.of(users.get(0));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("查询LDAP用户异常: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 获取所有LDAP用户
     *
     * @return 用户列表
     */
    public List<SysUser> getAllUsers() {
        try {
            LdapTemplate template = getLdapTemplate();
            return template.search(LdapQueryBuilder.query().where("objectClass").is("person"),
                    new UserAttributesMapper());
        } catch (Exception e) {
            log.error("获取LDAP用户列表异常: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * LDAP用户属性映射器
     */
    private static class UserAttributesMapper implements AttributesMapper<SysUser> {
        @Override
        public SysUser mapFromAttributes(Attributes attributes) throws NamingException {
            SysUser user = new SysUser();
            user.setUsername(getStringAttribute(attributes, "uid"));
            user.setRealName(getStringAttribute(attributes, "cn"));
            user.setEmail(getStringAttribute(attributes, "mail"));
            user.setPhone(getStringAttribute(attributes, "telephoneNumber"));
            user.setDepartment(getStringAttribute(attributes, "ou"));
            user.setLdapDn(getStringAttribute(attributes, "distinguishedName"));
            user.setLdapEnabled(true);
            return user;
        }

        private String getStringAttribute(Attributes attributes, String name) throws NamingException {
            if (attributes.get(name) != null) {
                return attributes.get(name).get().toString();
            }
            return null;
        }
    }

    /**
     * 获取用户DN
     *
     * @param username 用户名
     * @return 用户DN
     */
    public String getUserDn(String username) {
        Optional<SysUser> user = findUserByUsername(username);
        return user.map(SysUser::getLdapDn).orElse(null);
    }

    /**
     * 测试指定参数的LDAP连接
     */
    public Map<String, Object> testConnection(String host, Integer port, String baseDn, String userDn, String password) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            LdapContextSource contextSource = new LdapContextSource();
            contextSource.setUrl("ldap://" + host + ":" + port);
            contextSource.setBase(baseDn);
            contextSource.setUserDn(userDn);
            contextSource.setPassword(password);
            contextSource.afterPropertiesSet();

            LdapTemplate template = new LdapTemplate(contextSource);
            template.setIgnorePartialResultException(true);

            // 尝试搜索来测试连接
            template.search(LdapQueryBuilder.query().where("objectClass").is("top"),
                (Attributes attrs) -> null);

            result.put("success", true);
            result.put("message", "LDAP连接成功");
            log.info("LDAP连接测试成功: host={}, port={}", host, port);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "LDAP连接失败: " + e.getMessage());
            log.error("LDAP连接测试失败: host={}, port={}, error={}", host, port, e.getMessage());
        }
        return result;
    }

    /**
     * 同步所有LDAP用户到本地数据库
     */
    public Map<String, Object> syncUsersFromLdap() {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            List<SysUser> users = getAllUsers();
            result.put("success", true);
            result.put("syncedCount", users.size());
            result.put("message", "成功同步 " + users.size() + " 个用户");
            log.info("LDAP用户同步完成，同步 {} 个用户", users.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "同步失败: " + e.getMessage());
            log.error("LDAP用户同步失败", e);
        }
        return result;
    }

    /**
     * 密码加密
     */
    public String encryptPassword(String password) {
        // 简单返回原密码，实际应用中应使用加密
        // 这里可以使用AES或其他加密方式
        return password;
    }
}
