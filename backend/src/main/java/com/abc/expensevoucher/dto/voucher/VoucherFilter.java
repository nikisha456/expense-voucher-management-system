package com.abc.expensevoucher.dto.voucher;

import com.abc.expensevoucher.entity.ExpenseCategory;
import com.abc.expensevoucher.entity.VoucherStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VoucherFilter {

    private String voucherNumber;
    private String employeeName;
    private String department;
    private ExpenseCategory category;
    private VoucherStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String search;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
    private Integer page = 0;
    private Integer size = 10;
}
