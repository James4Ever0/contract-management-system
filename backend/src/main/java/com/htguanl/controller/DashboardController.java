package com.htguanl.controller;

import com.htguanl.common.Result;
import com.htguanl.entity.Dashboard;
import com.htguanl.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public Result<Dashboard> getDashboard() {
        return Result.success(dashboardService.getDashboardData());
    }
}

