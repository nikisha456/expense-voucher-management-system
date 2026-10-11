package com.abc.expensevoucher.dto.dashboard;

import com.abc.expensevoucher.dto.voucher.VoucherSummaryResponse;
import com.abc.expensevoucher.entity.VoucherStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record MonthAmount(int month, String label, BigDecimal amount) {
    }

    public record MonthCount(int month, String label, long count) {
    }

    public record LabelAmount(String label, BigDecimal amount) {
    }

    public record StatusSlice(String status, String label, long count) {
    }

    public record ActivityItem(
            Long voucherId,
            String voucherNumber,
            String actorName,
            String action,
            VoucherStatus toStatus,
            Instant changedAt
    ) {
    }

    public record EmployeeDashboardResponse(
            long totalVouchers,
            long draftVouchers,
            long pendingApproval,
            long approvedVouchers,
            long rejectedVouchers,
            BigDecimal totalAmountClaimed,
            BigDecimal amountChangePercent,
            List<MonthAmount> monthlySpend,
            List<StatusSlice> statusDistribution,
            List<VoucherSummaryResponse> recentVouchers
    ) {
    }

    public record DirectorDashboardResponse(
            long pendingApprovalCount,
            long approvedToday,
            long rejectedToday,
            BigDecimal totalPendingAmount,
            List<ActivityItem> recentActivity,
            List<LabelAmount> categorySpend,
            List<MonthCount> approvalsTrend,
            List<VoucherSummaryResponse> pendingReview
    ) {
    }

    public record AccountsDashboardResponse(
            long totalVouchers,
            long pendingApproval,
            long approvedVouchers,
            long rejectedVouchers,
            BigDecimal totalApprovedExpenseAmount,
            List<VoucherSummaryResponse> recentApproved,
            List<LabelAmount> departmentApproved,
            List<MonthAmount> monthlyReimbursement
    ) {
    }
}
