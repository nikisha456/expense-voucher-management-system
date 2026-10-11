package com.abc.expensevoucher.specification;

import com.abc.expensevoucher.dto.voucher.VoucherFilter;
import com.abc.expensevoucher.entity.Voucher;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class VoucherSpecifications {

    private VoucherSpecifications() {
    }

    public static Specification<Voucher> withFilter(VoucherFilter filter, Long ownerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ownerId != null) {
                predicates.add(cb.equal(root.get("createdBy").get("id"), ownerId));
            }
            if (hasText(filter.getVoucherNumber())) {
                predicates.add(cb.like(cb.lower(root.get("voucherNumber")), like(filter.getVoucherNumber())));
            }
            if (hasText(filter.getEmployeeName())) {
                predicates.add(cb.like(cb.lower(root.get("employeeName")), like(filter.getEmployeeName())));
            }
            if (hasText(filter.getDepartment())) {
                predicates.add(cb.equal(cb.lower(root.get("departmentName")), filter.getDepartment().trim().toLowerCase()));
            }
            if (filter.getCategory() != null) {
                predicates.add(cb.equal(root.get("expenseCategory"), filter.getCategory()));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), filter.getToDate()));
            }
            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }
            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }
            if (hasText(filter.getSearch())) {
                String pattern = like(filter.getSearch());
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("voucherNumber")), pattern),
                        cb.like(cb.lower(root.get("expenseTitle")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("expenseDescription"), "")), pattern),
                        cb.like(cb.lower(root.get("employeeName")), pattern),
                        cb.like(cb.lower(root.get("departmentName")), pattern)
                ));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String like(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
