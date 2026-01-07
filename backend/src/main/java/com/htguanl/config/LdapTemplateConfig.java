package com.htguanl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * LDAP Template配置类
 * 根据LDAP配置创建LdapTemplate Bean
 */
@Configuration
@ConditionalOnProperty(name = "ldap.enabled", havingValue = "true", matchIfMissing = false)
public class LdapTemplateConfig {

    @Autowired
    private LdapConfig ldapConfig;

    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://" + ldapConfig.getHost() + ":" + ldapConfig.getPort());
        contextSource.setBase(ldapConfig.getBaseDn());
        contextSource.setUserDn(ldapConfig.getUserDn());
        contextSource.setPassword(ldapConfig.getPassword());
        contextSource.setPooled(true);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        LdapTemplate ldapTemplate = new LdapTemplate(ldapContextSource());
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate;
    }
}

