package com.abc.expensevoucher.entity;

public enum VoucherStatus {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    PENDING_APPROVAL("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String label;

    VoucherStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
