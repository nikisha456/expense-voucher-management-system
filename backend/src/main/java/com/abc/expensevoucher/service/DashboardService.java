package com.abc.expensevoucher.service;

import com.abc.expensevoucher.dto.dashboard.DashboardDtos.AccountsDashboardResponse;
import com.abc.expensevoucher.dto.dashboard.DashboardDtos.ActivityItem;
import com.abc.expensevoucher.dto.dashboard.DashboardDtos.DirectorDashboardResponse;
import com.abc.expensevoucher.dto.dashboard.DashboardDtos.EmployeeDashboardResponse;
import com.abc.expensevoucher.dto.dashboard.DashboardDtos.LabelAmount;
import com.abc.expensevoucher.dto.dashboard.DashboardDtos.MonthAmount;
import com.abc.expensevoucher.dto.dashboard.DashboardDtos.MonthCount;
import com.abc.expensevoucher.dto.dashboard.DashboardDtos.StatusSlice;
import com.abc.expensevoucher.dto.voucher.VoucherSummaryResponse;
import com.abc.expensevoucher.entity.ExpenseCategory;
import com.abc.expensevoucher.entity.Role;
import com.abc.expensevoucher.entity.User;
import com.abc.expensevoucher.entity.VoucherStatus;
import com.abc.expensevoucher.entity.VoucherStatusHistory;
import com.abc.expensevoucher.exception.ResourceNotFoundException;
import com.abc.expensevoucher.mapper.VoucherMapper;
import com.abc.expensevoucher.repository.UserRepository;
import com.abc.expensevoucher.repository.VoucherRepository;
import com.abc.expensevoucher.repository.VoucherStatusHistoryRepository;
import com.abc.expensevoucher.security.UserPrincipal;
import com.abc.expensevoucher.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final String[] MONTHS = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private final VoucherRepository voucherRepository;
    private final VoucherStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final VoucherMapper voucherMapper;
    private final Clock clock;

    @Transactional(readOnly = true)
    public EmployeeDashboardResponse employeeDashboard() {
        User user = requireRole(Role.EMPLOYEE);
        Map<VoucherStatus, Long> counts = countsFor(voucherRepository.countByStatusForUser(user.getId()));
        long pending = counts.getOrDefault(VoucherStatus.PENDING_APPROVAL, 0L)
                + counts.getOrDefault(VoucherStatus.SUBMITTED, 0L);
        LocalDate today = LocalDate.now(clock);
        LocalDate monthStart = today.withDayOfMonth(1);
        BigDecimal current = money(voucherRepository.sumClaimedBetween(user.getId(), monthStart, monthStart.plusMonths(1)));
        BigDecimal previous = money(voucherRepository.sumClaimedBetween(user.getId(), monthStart.minusMonths(1), monthStart));
        BigDecimal change = null;
        if (previous.signum() > 0) {
            change = current.subtract(previous)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(previous, 1, RoundingMode.HALF_UP);
        }
        List<VoucherSummaryResponse> recent = voucherRepository.findTop5ByCreatedByIdOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(voucherMapper::toSummary)
                .toList();
        return new EmployeeDashboardResponse(
                sum(counts),
                counts.getOrDefault(VoucherStatus.DRAFT, 0L),
                pending,
                counts.getOrDefault(VoucherStatus.APPROVED, 0L),
                counts.getOrDefault(VoucherStatus.REJECTED, 0L),
                money(voucherRepository.sumClaimedByUser(user.getId())),
                change,
                monthAmounts(voucherRepository.monthlySpendForUser(user.getId(), today.getYear())),
                slices(counts),
                recent
        );
    }

    @Transactional(readOnly = true)
    public DirectorDashboardResponse directorDashboard() {
        requireRole(Role.DIRECTOR);
        LocalDate today = LocalDate.now(clock);
        Instant start = today.atStartOfDay(clock.getZone()).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        Map<VoucherStatus, Long> counts = countsFor(voucherRepository.countByStatus());
        List<ActivityItem> activity = historyRepository.findTop10ByOrderByChangedAtDesc().stream()
                .map(this::toActivity)
                .toList();
        List<VoucherSummaryResponse> pending = voucherRepository.findTop5ByStatusOrderByCreatedAtAsc(VoucherStatus.PENDING_APPROVAL)
                .stream()
                .map(voucherMapper::toSummary)
                .toList();
        return new DirectorDashboardResponse(
                counts.getOrDefault(VoucherStatus.PENDING_APPROVAL, 0L),
                voucherRepository.countByStatusAndApprovalDate(VoucherStatus.APPROVED, today),
                historyRepository.countByToStatusAndChangedAtGreaterThanEqualAndChangedAtLessThan(VoucherStatus.REJECTED, start, end),
                money(voucherRepository.sumPendingAmount()),
                activity,
                categoryAmounts(voucherRepository.approvedAmountByCategory()),
                monthCounts(voucherRepository.approvalsByMonth(today.getYear())),
                pending
        );
    }

    @Transactional(readOnly = true)
    public AccountsDashboardResponse accountsDashboard() {
        requireRole(Role.ACCOUNTS);
        Map<VoucherStatus, Long> counts = countsFor(voucherRepository.countByStatus());
        long pending = counts.getOrDefault(VoucherStatus.PENDING_APPROVAL, 0L)
                + counts.getOrDefault(VoucherStatus.SUBMITTED, 0L);
        List<VoucherSummaryResponse> recent = voucherRepository.findTop5ByStatusOrderByApprovalDateDesc(VoucherStatus.APPROVED)
                .stream()
                .map(voucherMapper::toSummary)
                .toList();
        return new AccountsDashboardResponse(
                sum(counts),
                pending,
                counts.getOrDefault(VoucherStatus.APPROVED, 0L),
                counts.getOrDefault(VoucherStatus.REJECTED, 0L),
                money(voucherRepository.sumApprovedAmount()),
                recent,
                labelAmounts(voucherRepository.approvedAmountByDepartment()),
                monthAmounts(voucherRepository.monthlyReimbursement(LocalDate.now(clock).getYear()))
        );
    }

    private ActivityItem toActivity(VoucherStatusHistory history) {
        String action = switch (history.getToStatus()) {
            case DRAFT -> "created a draft";
            case SUBMITTED -> "submitted";
            case PENDING_APPROVAL -> "sent for approval";
            case APPROVED -> "approved";
            case REJECTED -> "rejected";
        };
        return new ActivityItem(
                history.getVoucher().getId(),
                history.getVoucher().getVoucherNumber(),
                history.getChangedBy() == null ? "System" : history.getChangedBy().getName(),
                action,
                history.getToStatus(),
                history.getChangedAt()
        );
    }

    private List<LabelAmount> categoryAmounts(List<Object[]> rows) {
        List<LabelAmount> amounts = new ArrayList<>();
        for (Object[] row : rows) {
            ExpenseCategory category = ExpenseCategory.valueOf(row[0].toString());
            amounts.add(new LabelAmount(category.getLabel(), money(row[1])));
        }
        return amounts;
    }

    private List<LabelAmount> labelAmounts(List<Object[]> rows) {
        List<LabelAmount> amounts = new ArrayList<>();
        for (Object[] row : rows) {
            amounts.add(new LabelAmount(String.valueOf(row[0]), money(row[1])));
        }
        return amounts;
    }

    private List<MonthAmount> monthAmounts(List<Object[]> rows) {
        Map<Integer, BigDecimal> values = new HashMap<>();
        for (Object[] row : rows) {
            values.put(toInt(row[0]), money(row[1]));
        }
        List<MonthAmount> months = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            months.add(new MonthAmount(month, MONTHS[month - 1], values.getOrDefault(month, money(BigDecimal.ZERO))));
        }
        return months;
    }

    private List<MonthCount> monthCounts(List<Object[]> rows) {
        Map<Integer, Long> values = new HashMap<>();
        for (Object[] row : rows) {
            values.put(toInt(row[0]), toLong(row[1]));
        }
        List<MonthCount> months = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            months.add(new MonthCount(month, MONTHS[month - 1], values.getOrDefault(month, 0L)));
        }
        return months;
    }

    private List<StatusSlice> slices(Map<VoucherStatus, Long> counts) {
        List<StatusSlice> slices = new ArrayList<>();
        for (VoucherStatus status : VoucherStatus.values()) {
            slices.add(new StatusSlice(status.name(), status.getLabel(), counts.getOrDefault(status, 0L)));
        }
        return slices;
    }

    private Map<VoucherStatus, Long> countsFor(List<Object[]> rows) {
        Map<VoucherStatus, Long> counts = new EnumMap<>(VoucherStatus.class);
        for (Object[] row : rows) {
            counts.put((VoucherStatus) row[0], toLong(row[1]));
        }
        return counts;
    }

    private long sum(Map<VoucherStatus, Long> counts) {
        return counts.values().stream().mapToLong(Long::longValue).sum();
    }

    private User requireRole(Role role) {
        UserPrincipal principal = SecurityUtils.currentUser();
        if (principal.getRole() != role) {
            throw new AccessDeniedException("This action requires the " + role.name().toLowerCase() + " role");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private BigDecimal money(Object value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
    }

    private int toInt(Object value) {
        return value instanceof Number number ? number.intValue() : Integer.parseInt(value.toString());
    }

    private long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString());
    }
}
