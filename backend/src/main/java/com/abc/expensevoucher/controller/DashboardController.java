package com.abc.expensevoucher.controller;

import com.abc.expensevoucher.dto.common.ApiResponse;
import com.abc.expensevoucher.dto.dashboard.DashboardDtos.AccountsDashboardResponse;
import com.abc.expensevoucher.dto.dashboard.DashboardDtos.DirectorDashboardResponse;
import com.abc.expensevoucher.dto.dashboard.DashboardDtos.EmployeeDashboardResponse;
import com.abc.expensevoucher.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/employee")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Employee dashboard metrics")
    public ApiResponse<EmployeeDashboardResponse> employee() {
        return ApiResponse.ok(dashboardService.employeeDashboard());
    }

    @GetMapping("/director")
    @PreAuthorize("hasRole('DIRECTOR')")
    @Operation(summary = "Director dashboard metrics")
    public ApiResponse<DirectorDashboardResponse> director() {
        return ApiResponse.ok(dashboardService.directorDashboard());
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasRole('ACCOUNTS')")
    @Operation(summary = "Accounts dashboard metrics")
    public ApiResponse<AccountsDashboardResponse> accounts() {
        return ApiResponse.ok(dashboardService.accountsDashboard());
    }
}
