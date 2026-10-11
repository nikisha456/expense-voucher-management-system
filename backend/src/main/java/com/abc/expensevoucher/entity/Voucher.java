package com.abc.expensevoucher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vouchers")
@EntityListeners(AuditingEntityListener.class)
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voucher_number", nullable = false, unique = true, length = 32)
    private String voucherNumber;

    @Column(name = "voucher_date", nullable = false)
    private LocalDate voucherDate;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    @Column(name = "expense_title", nullable = false, length = 200)
    private String expenseTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_category", nullable = false, length = 40)
    private ExpenseCategory expenseCategory;

    @Column(name = "expense_description", columnDefinition = "text")
    private String expenseDescription;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "employee_name", nullable = false, length = 150)
    private String employeeName;

    @Column(name = "employee_code", length = 50)
    private String employeeCode;

    @Column(name = "employee_signature_path", length = 255)
    private String employeeSignaturePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VoucherStatus status;

    @Column(name = "director_signature_path", length = 255)
    private String directorSignaturePath;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "rejection_reason", columnDefinition = "text")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
