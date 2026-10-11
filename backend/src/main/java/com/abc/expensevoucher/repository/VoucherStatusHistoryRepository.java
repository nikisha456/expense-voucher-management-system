package com.abc.expensevoucher.repository;

import com.abc.expensevoucher.entity.VoucherStatus;
import com.abc.expensevoucher.entity.VoucherStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface VoucherStatusHistoryRepository extends JpaRepository<VoucherStatusHistory, Long> {

    List<VoucherStatusHistory> findByVoucherIdOrderByChangedAtAscIdAsc(Long voucherId);

    @Modifying
    @Query("delete from VoucherStatusHistory h where h.voucher.id = :voucherId")
    void deleteByVoucherId(@Param("voucherId") Long voucherId);

    long countByToStatusAndChangedAtGreaterThanEqualAndChangedAtLessThan(VoucherStatus toStatus, Instant start, Instant end);

    List<VoucherStatusHistory> findTop10ByOrderByChangedAtDesc();
}
