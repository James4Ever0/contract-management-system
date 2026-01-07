package com.htguanl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 合同管理系统主应用类
 */
@SpringBootApplication
public class ContractManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContractManagementApplication.class, args);
        System.out.println("========================================");
        System.out.println("合同管理系统启动成功！");
        System.out.println("========================================");
    }
}
