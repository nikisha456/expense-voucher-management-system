package com.abc.expensevoucher.dto.voucher;

import com.abc.expensevoucher.entity.ExpenseCategory;
import com.abc.expensevoucher.entity.VoucherStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record VoucherSummaryResponse(
        Long id,
        String voucherNumber,
        LocalDate voucherDate,
        LocalDate expenseDate,
        String departmentName,
        String expenseTitle,
        ExpenseCategory expenseCategory,
        String categoryLabel,
        BigDecimal amount,
        String employeeName,
        String employeeCode,
        VoucherStatus status,
        LocalDate approvalDate,
        boolean hasRejectionReason,
        Instant updatedAt
) {
}
