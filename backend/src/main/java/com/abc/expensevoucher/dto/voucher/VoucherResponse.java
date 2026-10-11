package com.abc.expensevoucher.dto.voucher;

import com.abc.expensevoucher.entity.ExpenseCategory;
import com.abc.expensevoucher.entity.VoucherStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class VoucherResponse {

    private final Long id;
    private final String voucherNumber;
    private final LocalDate voucherDate;
    private final LocalDate expenseDate;
    private final String departmentName;
    private final String expenseTitle;
    private final ExpenseCategory expenseCategory;
    private final String categoryLabel;
    private final String expenseDescription;
    private final BigDecimal amount;
    private final String amountInWords;
    private final String employeeName;
    private final String employeeCode;
    private final String employeeSignatureFileName;
    private final VoucherStatus status;
    private final String directorSignatureFileName;
    private final LocalDate approvalDate;
    private final String rejectionReason;
    private final String decisionByName;
    private final Long createdById;
    private final String createdByEmail;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final List<StatusHistoryResponse> history;
    private final boolean editable;
    private final boolean deletable;
    private final boolean submittable;
}
