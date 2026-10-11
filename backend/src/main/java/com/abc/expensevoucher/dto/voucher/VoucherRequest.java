package com.abc.expensevoucher.dto.voucher;

import com.abc.expensevoucher.entity.ExpenseCategory;
import com.abc.expensevoucher.validation.NotFutureDate;
import com.abc.expensevoucher.validation.ValidDepartment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VoucherRequest {

    @NotNull(message = "Voucher date is required")
    private LocalDate voucherDate;

    @NotNull(message = "Expense date is required")
    @NotFutureDate
    private LocalDate expenseDate;

    @NotBlank(message = "Department is required")
    @ValidDepartment
    @Size(max = 100, message = "Department must be at most 100 characters")
    private String departmentName;

    @NotBlank(message = "Expense title is required")
    @Size(max = 200, message = "Expense title must be at most 200 characters")
    private String expenseTitle;

    @NotNull(message = "Expense category is required")
    private ExpenseCategory expenseCategory;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String expenseDescription;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;
}
