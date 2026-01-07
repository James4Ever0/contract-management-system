package com.htguanl.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htguanl.common.Result;
import com.htguanl.entity.SysUser;
import com.htguanl.mapper.SysUserMapper;
import com.htguanl.service.LdapService;
import com.htguanl.service.LdapUserSyncService;
import com.htguanl.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 支持本地认证和LDAP认证
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private LdapService ldapService;

    @Autowired
    private LdapUserSyncService ldapUserSyncService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 登录接口
     * 支持本地账号和LDAP账号登录
     *
     * @param username 用户名
     * @param password 密码
     * @param loginType 登录类型：local-本地登录，ldap-LDAP登录
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(defaultValue = "local") String loginType) {

        log.info("用户登录请求: username={}, loginType={}", username, loginType);

        try {
            SysUser user;

            if ("ldap".equalsIgnoreCase(loginType)) {
                // LDAP认证
                log.info("使用LDAP认证: username={}", username);
                boolean authenticated = ldapService.authenticate(username, password);

                if (!authenticated) {
                    log.warn("LDAP认证失败: username={}", username);
                    return Result.error("LDAP认证失败，请检查用户名和密码");
                }

                // 同步用户到本地数据库
                user = ldapUserSyncService.syncUser(username);
                if (user == null) {
                    log.error("用户同步失败: username={}", username);
                    return Result.error("用户同步失败");
                }
                log.info("LDAP用户同步成功: username={}", username);

            } else {
                // 本地认证
                log.info("使用本地认证: username={}", username);

                // 查询本地用户
                LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysUser::getUsername, username);
                user = sysUserMapper.selectOne(wrapper);

                if (user == null) {
                    log.warn("本地认证失败，用户不存在: username={}", username);
                    return Result.error("用户名或密码错误");
                }

                // 验证密码
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    log.warn("本地认证失败，密码错误: username={}", username);
                    return Result.error("用户名或密码错误");
                }

                // 检查用户状态
                if (user.getStatus() != null && user.getStatus() != 1) {
                    log.warn("本地认证失败，用户已禁用: username={}", username);
                    return Result.error("用户已被禁用");
                }

                log.info("本地认证成功: username={}", username);
            }

            // 生成JWT令牌
            String token = JwtUtil.generateToken(user.getUsername(), user.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            log.info("用户登录成功: username={}", username);
            return Result.success(data);

        } catch (Exception e) {
            log.error("登录失败: username={}, error={}", username, e.getMessage(), e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * LDAP连接测试接口
     *
     * @param host LDAP服务器地址
     * @param port LDAP服务器端口
     * @param baseDn 基础DN
     * @param userDn 管理员DN
     * @param password 管理员密码
     * @return 测试结果
     */
    @PostMapping("/ldap/test")
    public Result<Map<String, Object>> testLdapConnection(
            @RequestParam String host,
            @RequestParam Integer port,
            @RequestParam String baseDn,
            @RequestParam String userDn,
            @RequestParam String password) {

        log.info("测试LDAP连接: host={}, port={}", host, port);

        try {
            Map<String, Object> result = ldapService.testConnection(host, port, baseDn, userDn, password);
            return Result.success(result);

        } catch (Exception e) {
            log.error("LDAP连接测试失败: error={}", e.getMessage(), e);
            return Result.error("LDAP连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 验证令牌
     *
     * @param token JWT令牌
     * @return 验证结果
     */
    @GetMapping("/validate")
    public Result<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            boolean valid = JwtUtil.validateToken(token);

            Map<String, Object> data = new HashMap<>();
            data.put("valid", valid);

            if (valid) {
                String username = JwtUtil.getUsernameFromToken(token);
                data.put("username", username);
            }

            return Result.success(data);

        } catch (Exception e) {
            log.error("令牌验证失败: error={}", e.getMessage(), e);
            return Result.error("令牌验证失败");
        }
    }

    /**
     * 登出接口
     *
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        log.info("用户登出");
        // JWT是无状态的，客户端删除令牌即可
        return Result.success(null);
    }
}
