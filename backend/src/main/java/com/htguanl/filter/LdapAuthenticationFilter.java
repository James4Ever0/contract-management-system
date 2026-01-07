package com.htguanl.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htguanl.common.Result;
import com.htguanl.config.LdapConfig;
import com.htguanl.entity.SysUser;
import com.htguanl.service.LdapService;
import com.htguanl.service.LdapUserSyncService;
import com.htguanl.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * LDAP认证过滤器
 * 处理LDAP账号登录请求
 */
@Component
public class LdapAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LdapAuthenticationFilter.class);

    @Autowired
    private LdapConfig ldapConfig;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private LdapUserSyncService ldapUserSyncService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 只处理LDAP登录请求
        if ("/api/auth/ldap-login".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            handleLdapLogin(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 处理LDAP登录
     */
    private void handleLdapLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            // 检查LDAP是否启用
            if (ldapConfig.getEnabled() == null || !ldapConfig.getEnabled()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Result.error("LDAP认证未启用"));
                return;
            }

            // 解析请求参数
            Map<String, String> params = objectMapper.readValue(request.getInputStream(), Map.class);
            String username = params.get("username");
            String password = params.get("password");

            // 参数验证
            if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Result.error("用户名和密码不能为空"));
                return;
            }

            log.info("LDAP登录请求: username={}", username);

            // LDAP认证
            boolean authenticated = ldapService.authenticate(username, password);
            if (!authenticated) {
                log.warn("LDAP认证失败: username={}", username);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                objectMapper.writeValue(response.getWriter(), Result.error("用户名或密码错误"));
                return;
            }

            // 同步用户到本地数据库
            SysUser user = ldapUserSyncService.syncUser(username);
            if (user == null) {
                log.error("LDAP用户同步失败: username={}", username);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Result.error("用户同步失败"));
                return;
            }

            // 生成JWT令牌
            String token = JwtUtil.generateToken(user.getUsername(), user.getId());

            // 设置认证信息
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 返回登录成功响应
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            log.info("LDAP登录成功: username={}, userId={}", username, user.getId());
            objectMapper.writeValue(response.getWriter(), Result.success(data));

        } catch (Exception e) {
            log.error("LDAP登录处理异常", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Result.error("登录处理异常: " + e.getMessage()));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 不过滤静态资源
        return path.startsWith("/static/") || path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/");
    }
}
