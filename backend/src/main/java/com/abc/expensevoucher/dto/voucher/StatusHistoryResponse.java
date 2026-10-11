package com.abc.expensevoucher.dto.voucher;

import com.abc.expensevoucher.entity.VoucherStatus;

import java.time.Instant;

public record StatusHistoryResponse(
        Long id,
        VoucherStatus fromStatus,
        VoucherStatus toStatus,
        String changedByName,
        String remarks,
        Instant changedAt
) {
}
