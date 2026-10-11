package com.abc.expensevoucher.mapper;

import com.abc.expensevoucher.dto.voucher.StatusHistoryResponse;
import com.abc.expensevoucher.dto.voucher.VoucherResponse;
import com.abc.expensevoucher.dto.voucher.VoucherSummaryResponse;
import com.abc.expensevoucher.entity.Voucher;
import com.abc.expensevoucher.entity.VoucherStatus;
import com.abc.expensevoucher.entity.VoucherStatusHistory;
import com.abc.expensevoucher.util.AmountInWords;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VoucherMapper {

    private final AmountInWords amountInWords;

    public VoucherSummaryResponse toSummary(Voucher voucher) {
        return new VoucherSummaryResponse(
                voucher.getId(),
                voucher.getVoucherNumber(),
                voucher.getVoucherDate(),
                voucher.getExpenseDate(),
                voucher.getDepartmentName(),
                voucher.getExpenseTitle(),
                voucher.getExpenseCategory(),
                voucher.getExpenseCategory() == null ? null : voucher.getExpenseCategory().getLabel(),
                voucher.getAmount(),
                voucher.getEmployeeName(),
                voucher.getEmployeeCode(),
                voucher.getStatus(),
                voucher.getApprovalDate(),
                voucher.getRejectionReason() != null && !voucher.getRejectionReason().isBlank(),
                voucher.getUpdatedAt()
        );
    }

    public VoucherResponse toDetail(Voucher voucher, List<VoucherStatusHistory> history, boolean owner) {
        List<StatusHistoryResponse> timeline = history.stream()
                .map(entry -> new StatusHistoryResponse(
                        entry.getId(),
                        entry.getFromStatus(),
                        entry.getToStatus(),
                        entry.getChangedBy() == null ? null : entry.getChangedBy().getName(),
                        entry.getRemarks(),
                        entry.getChangedAt()
                ))
                .toList();
        String decisionByName = history.stream()
                .filter(entry -> entry.getToStatus() == VoucherStatus.APPROVED || entry.getToStatus() == VoucherStatus.REJECTED)
                .max(Comparator.comparing(VoucherStatusHistory::getChangedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(entry -> entry.getChangedBy() == null ? null : entry.getChangedBy().getName())
                .orElse(null);
        boolean draftOwner = owner && voucher.getStatus() == VoucherStatus.DRAFT;
        return VoucherResponse.builder()
                .id(voucher.getId())
                .voucherNumber(voucher.getVoucherNumber())
                .voucherDate(voucher.getVoucherDate())
                .expenseDate(voucher.getExpenseDate())
                .departmentName(voucher.getDepartmentName())
                .expenseTitle(voucher.getExpenseTitle())
                .expenseCategory(voucher.getExpenseCategory())
                .categoryLabel(voucher.getExpenseCategory() == null ? null : voucher.getExpenseCategory().getLabel())
                .expenseDescription(voucher.getExpenseDescription())
                .amount(voucher.getAmount())
                .amountInWords(amountInWords.toWords(voucher.getAmount()))
                .employeeName(voucher.getEmployeeName())
                .employeeCode(voucher.getEmployeeCode())
                .employeeSignatureFileName(voucher.getEmployeeSignaturePath())
                .status(voucher.getStatus())
                .directorSignatureFileName(voucher.getDirectorSignaturePath())
                .approvalDate(voucher.getApprovalDate())
                .rejectionReason(voucher.getRejectionReason())
                .decisionByName(decisionByName)
                .createdById(voucher.getCreatedBy() == null ? null : voucher.getCreatedBy().getId())
                .createdByEmail(voucher.getCreatedBy() == null ? null : voucher.getCreatedBy().getEmail())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .history(timeline)
                .editable(draftOwner)
                .deletable(draftOwner)
                .submittable(draftOwner)
                .build();
    }
}
