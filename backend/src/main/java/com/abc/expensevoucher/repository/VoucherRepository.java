package com.abc.expensevoucher.repository;

import com.abc.expensevoucher.entity.Voucher;
import com.abc.expensevoucher.entity.VoucherStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Voucher v where v.id = :id")
    Optional<Voucher> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select count(v) from Voucher v
            where (v.employeeSignaturePath = :path or v.directorSignaturePath = :path)
              and (:ownerId is null or v.createdBy.id = :ownerId)
            """)
    long countVisibleSignatures(@Param("path") String path, @Param("ownerId") Long ownerId);

    @Query("select v.status, count(v) from Voucher v where v.createdBy.id = :userId group by v.status")
    List<Object[]> countByStatusForUser(@Param("userId") Long userId);

    @Query("select v.status, count(v) from Voucher v group by v.status")
    List<Object[]> countByStatus();

    @Query("""
            select coalesce(sum(v.amount), 0) from Voucher v
            where v.createdBy.id = :userId and v.status <> com.abc.expensevoucher.entity.VoucherStatus.DRAFT
            """)
    BigDecimal sumClaimedByUser(@Param("userId") Long userId);

    @Query("""
            select coalesce(sum(v.amount), 0) from Voucher v
            where v.status = com.abc.expensevoucher.entity.VoucherStatus.PENDING_APPROVAL
            """)
    BigDecimal sumPendingAmount();

    @Query("""
            select coalesce(sum(v.amount), 0) from Voucher v
            where v.status = com.abc.expensevoucher.entity.VoucherStatus.APPROVED
            """)
    BigDecimal sumApprovedAmount();

    long countByStatusAndApprovalDate(VoucherStatus status, LocalDate approvalDate);

    List<Voucher> findTop5ByCreatedByIdOrderByUpdatedAtDesc(Long userId);

    List<Voucher> findTop5ByStatusOrderByCreatedAtAsc(VoucherStatus status);

    List<Voucher> findTop5ByStatusOrderByApprovalDateDesc(VoucherStatus status);

    @Query(value = """
            SELECT EXTRACT(MONTH FROM expense_date)::int AS month, COALESCE(SUM(amount), 0) AS total
            FROM vouchers
            WHERE created_by = :userId
              AND EXTRACT(YEAR FROM expense_date) = :year
              AND status <> 'DRAFT'
            GROUP BY 1
            ORDER BY 1
            """, nativeQuery = true)
    List<Object[]> monthlySpendForUser(@Param("userId") Long userId, @Param("year") int year);

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM vouchers
            WHERE created_by = :userId
              AND status <> 'DRAFT'
              AND expense_date >= :startDate
              AND expense_date < :endDate
            """, nativeQuery = true)
    BigDecimal sumClaimedBetween(@Param("userId") Long userId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    @Query(value = """
            SELECT expense_category, COALESCE(SUM(amount), 0)
            FROM vouchers
            WHERE status = 'APPROVED'
            GROUP BY expense_category
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> approvedAmountByCategory();

    @Query(value = """
            SELECT EXTRACT(MONTH FROM approval_date)::int, COUNT(*)
            FROM vouchers
            WHERE status = 'APPROVED'
              AND approval_date IS NOT NULL
              AND EXTRACT(YEAR FROM approval_date) = :year
            GROUP BY 1
            ORDER BY 1
            """, nativeQuery = true)
    List<Object[]> approvalsByMonth(@Param("year") int year);

    @Query(value = """
            SELECT department_name, COALESCE(SUM(amount), 0)
            FROM vouchers
            WHERE status = 'APPROVED'
            GROUP BY department_name
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> approvedAmountByDepartment();

    @Query(value = """
            SELECT EXTRACT(MONTH FROM approval_date)::int, COALESCE(SUM(amount), 0)
            FROM vouchers
            WHERE status = 'APPROVED'
              AND approval_date IS NOT NULL
              AND EXTRACT(YEAR FROM approval_date) = :year
            GROUP BY 1
            ORDER BY 1
            """, nativeQuery = true)
    List<Object[]> monthlyReimbursement(@Param("year") int year);
}
