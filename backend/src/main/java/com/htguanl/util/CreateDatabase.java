package com.htguanl.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateDatabase {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://172.16.30.51:15432/postgres";
        String user = "postgres";
        String password = "Postgres@2025";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("CREATE DATABASE contract_management");
            System.out.println("数据库 contract_management 创建成功！");
            
        } catch (Exception e) {
            if (e.getMessage().contains("already exists")) {
                System.out.println("数据库 contract_management 已存在");
            } else {
                e.printStackTrace();
            }
        }
    }
}

