package com.abc.expensevoucher.exception;

import com.abc.expensevoucher.entity.VoucherStatus;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public InvalidStateTransitionException(VoucherStatus from, VoucherStatus to) {
        super("Cannot change voucher status from " + from + " to " + to);
    }
}
