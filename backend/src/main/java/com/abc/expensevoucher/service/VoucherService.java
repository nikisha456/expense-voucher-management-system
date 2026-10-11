package com.abc.expensevoucher.service;

import com.abc.expensevoucher.dto.voucher.VoucherFilter;
import com.abc.expensevoucher.dto.voucher.VoucherRequest;
import com.abc.expensevoucher.dto.voucher.VoucherResponse;
import com.abc.expensevoucher.dto.voucher.VoucherSummaryResponse;
import com.abc.expensevoucher.entity.Department;
import com.abc.expensevoucher.entity.Role;
import com.abc.expensevoucher.entity.User;
import com.abc.expensevoucher.entity.Voucher;
import com.abc.expensevoucher.entity.VoucherStatus;
import com.abc.expensevoucher.entity.VoucherStatusHistory;
import com.abc.expensevoucher.exception.BusinessRuleException;
import com.abc.expensevoucher.exception.InvalidStateTransitionException;
import com.abc.expensevoucher.exception.ResourceNotFoundException;
import com.abc.expensevoucher.mapper.VoucherMapper;
import com.abc.expensevoucher.repository.UserRepository;
import com.abc.expensevoucher.repository.VoucherRepository;
import com.abc.expensevoucher.repository.VoucherStatusHistoryRepository;
import com.abc.expensevoucher.security.UserPrincipal;
import com.abc.expensevoucher.specification.VoucherSpecifications;
import com.abc.expensevoucher.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherService {

    private static final Map<String, String> SORT_FIELDS = Map.ofEntries(
            Map.entry("voucherNumber", "voucherNumber"),
            Map.entry("voucherDate", "voucherDate"),
            Map.entry("expenseDate", "expenseDate"),
            Map.entry("amount", "amount"),
            Map.entry("status", "status"),
            Map.entry("department", "departmentName"),
            Map.entry("departmentName", "departmentName"),
            Map.entry("category", "expenseCategory"),
            Map.entry("expenseCategory", "expenseCategory"),
            Map.entry("createdAt", "createdAt"),
            Map.entry("updatedAt", "updatedAt"),
            Map.entry("employeeName", "employeeName"),
            Map.entry("expenseTitle", "expenseTitle")
    );

    private final VoucherRepository voucherRepository;
    private final VoucherStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final VoucherNumberGenerator numberGenerator;
    private final FileStorageService fileStorageService;
    private final VoucherMapper voucherMapper;
    private final Clock clock;

    @Transactional
    public VoucherResponse create(VoucherRequest request) {
        User employee = requireRole(Role.EMPLOYEE);
        Voucher voucher = new Voucher();
        voucher.setVoucherNumber(numberGenerator.next());
        applyFields(voucher, request);
        snapshotEmployee(voucher, employee);
        voucher.setStatus(VoucherStatus.DRAFT);
        voucher.setCreatedBy(employee);
        voucher = voucherRepository.save(voucher);
        recordHistory(voucher, null, VoucherStatus.DRAFT, employee, "Voucher created as draft");
        log.info("Draft voucher {} created by {}", voucher.getVoucherNumber(), employee.getEmail());
        return toDetail(voucher);
    }

    @Transactional
    public VoucherResponse update(Long id, VoucherRequest request) {
        User employee = requireRole(Role.EMPLOYEE);
        Voucher voucher = loadOwnedDraft(id);
        applyFields(voucher, request);
        snapshotEmployee(voucher, employee);
        voucherRepository.save(voucher);
        log.info("Draft voucher {} updated by {}", voucher.getVoucherNumber(), employee.getEmail());
        return toDetail(voucher);
    }

    @Transactional
    public void delete(Long id) {
        User employee = requireRole(Role.EMPLOYEE);
        Voucher voucher = loadOwnedDraft(id);
        historyRepository.deleteByVoucherId(voucher.getId());
        voucherRepository.delete(voucher);
        log.info("Draft voucher {} deleted by {}", voucher.getVoucherNumber(), employee.getEmail());
    }

    @Transactional
    public VoucherResponse submit(Long id) {
        User employee = requireRole(Role.EMPLOYEE);
        Voucher voucher = loadOwnedDraft(id);
        if (voucher.getEmployeeSignaturePath() == null || voucher.getEmployeeSignaturePath().isBlank()) {
            throw new BusinessRuleException("Employee signature is required before submission");
        }
        transition(voucher, VoucherStatus.SUBMITTED, employee, "Submitted for approval");
        transition(voucher, VoucherStatus.PENDING_APPROVAL, employee, "Awaiting director approval");
        voucherRepository.save(voucher);
        log.info("Voucher {} submitted by {}", voucher.getVoucherNumber(), employee.getEmail());
        return toDetail(voucher);
    }

    @Transactional
    public VoucherResponse uploadEmployeeSignature(Long id, MultipartFile file) {
        User employee = requireRole(Role.EMPLOYEE);
        Voucher voucher = loadOwnedDraft(id);
        voucher.setEmployeeSignaturePath(fileStorageService.store(file));
        voucherRepository.save(voucher);
        log.info("Employee signature attached to voucher {}", voucher.getVoucherNumber());
        return toDetail(voucher);
    }

    @Transactional
    public VoucherResponse useProfileSignature(Long id) {
        User employee = requireRole(Role.EMPLOYEE);
        Voucher voucher = loadOwnedDraft(id);
        if (employee.getSignaturePath() == null || employee.getSignaturePath().isBlank()) {
            throw new BusinessRuleException("Save a signature on your profile before using it");
        }
        voucher.setEmployeeSignaturePath(fileStorageService.copy(employee.getSignaturePath()));
        voucherRepository.save(voucher);
        log.info("Profile signature copied onto voucher {}", voucher.getVoucherNumber());
        return toDetail(voucher);
    }

    @Transactional(readOnly = true)
    public VoucherResponse getById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        assertCanView(voucher);
        return toDetail(voucher);
    }

    @Transactional(readOnly = true)
    public Page<VoucherSummaryResponse> listMine(VoucherFilter filter) {
        User employee = requireRole(Role.EMPLOYEE);
        return search(filter, employee.getId());
    }

    @Transactional(readOnly = true)
    public Page<VoucherSummaryResponse> listAll(VoucherFilter filter) {
        UserPrincipal principal = SecurityUtils.currentUser();
        if (principal.getRole() != Role.DIRECTOR && principal.getRole() != Role.ACCOUNTS) {
            throw new AccessDeniedException("You do not have permission to view all vouchers");
        }
        return search(filter, null);
    }

    @Transactional(readOnly = true)
    public Page<VoucherSummaryResponse> listPending(VoucherFilter filter) {
        requireRole(Role.DIRECTOR);
        filter.setStatus(VoucherStatus.PENDING_APPROVAL);
        return search(filter, null);
    }

    @Transactional
    public VoucherResponse approve(Long id, MultipartFile signature, boolean useSaved) {
        User director = requireRole(Role.DIRECTOR);
        Voucher voucher = loadForUpdate(id);
        if (voucher.getStatus() != VoucherStatus.PENDING_APPROVAL) {
            throw new InvalidStateTransitionException(voucher.getStatus(), VoucherStatus.APPROVED);
        }
        String stored = resolveDirectorSignature(director, signature, useSaved);
        voucher.setDirectorSignaturePath(stored);
        voucher.setApprovalDate(LocalDate.now(clock));
        voucher.setRejectionReason(null);
        transition(voucher, VoucherStatus.APPROVED, director, "Approved for reimbursement");
        voucherRepository.save(voucher);
        log.info("Voucher {} approved by {}", voucher.getVoucherNumber(), director.getEmail());
        return toDetail(voucher);
    }

    @Transactional
    public VoucherResponse reject(Long id, String reason) {
        User director = requireRole(Role.DIRECTOR);
        Voucher voucher = loadForUpdate(id);
        String cleaned = reason == null ? "" : reason.trim();
        if (cleaned.length() < 10) {
            throw new BusinessRuleException("Rejection reason must be at least 10 characters");
        }
        if (voucher.getStatus() != VoucherStatus.PENDING_APPROVAL) {
            throw new InvalidStateTransitionException(voucher.getStatus(), VoucherStatus.REJECTED);
        }
        voucher.setRejectionReason(cleaned);
        voucher.setApprovalDate(null);
        transition(voucher, VoucherStatus.REJECTED, director, cleaned);
        voucherRepository.save(voucher);
        log.info("Voucher {} rejected by {}", voucher.getVoucherNumber(), director.getEmail());
        return toDetail(voucher);
    }

    private Page<VoucherSummaryResponse> search(VoucherFilter filter, Long ownerId) {
        Page<Voucher> page = voucherRepository.findAll(VoucherSpecifications.withFilter(filter, ownerId), pageable(filter));
        return page.map(voucherMapper::toSummary);
    }

    private Pageable pageable(VoucherFilter filter) {
        int page = filter.getPage() == null || filter.getPage() < 0 ? 0 : filter.getPage();
        int size = filter.getSize() == null || filter.getSize() < 1 ? 10 : Math.min(filter.getSize(), 100);
        String requested = filter.getSortBy() == null ? "" : filter.getSortBy();
        String sortBy = SORT_FIELDS.getOrDefault(requested, "createdAt");
        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    private void applyFields(Voucher voucher, VoucherRequest request) {
        if (request.getExpenseTitle() == null || request.getExpenseTitle().isBlank()) {
            throw new BusinessRuleException("Expense title is required");
        }
        if (request.getExpenseDate() == null) {
            throw new BusinessRuleException("Expense date is required");
        }
        if (request.getExpenseDate().isAfter(LocalDate.now(clock))) {
            throw new BusinessRuleException("Expense date cannot be in the future");
        }
        if (request.getVoucherDate() == null) {
            throw new BusinessRuleException("Voucher date is required");
        }
        if (request.getExpenseCategory() == null) {
            throw new BusinessRuleException("Expense category is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Amount must be greater than 0");
        }
        if (request.getAmount().scale() > 2) {
            throw new BusinessRuleException("Amount cannot have more than 2 decimal places");
        }
        voucher.setVoucherDate(request.getVoucherDate());
        voucher.setExpenseDate(request.getExpenseDate());
        voucher.setDepartmentName(Department.canonical(request.getDepartmentName()));
        voucher.setExpenseTitle(request.getExpenseTitle().trim());
        voucher.setExpenseCategory(request.getExpenseCategory());
        String description = request.getExpenseDescription();
        voucher.setExpenseDescription(description == null || description.isBlank() ? null : description.trim());
        voucher.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
    }

    private void snapshotEmployee(Voucher voucher, User employee) {
        voucher.setEmployeeName(employee.getName());
        voucher.setEmployeeCode(employee.getEmployeeCode());
    }

    private String resolveDirectorSignature(User director, MultipartFile signature, boolean useSaved) {
        if (signature != null && !signature.isEmpty()) {
            return fileStorageService.store(signature);
        }
        if (useSaved && director.getSignaturePath() != null && !director.getSignaturePath().isBlank()) {
            return fileStorageService.copy(director.getSignaturePath());
        }
        throw new BusinessRuleException("Director signature is required before approval");
    }

    private Voucher loadOwnedDraft(Long id) {
        Voucher voucher = loadForUpdate(id);
        if (!isOwner(voucher)) {
            throw new ResourceNotFoundException("Voucher not found");
        }
        if (voucher.getStatus() != VoucherStatus.DRAFT) {
            throw new InvalidStateTransitionException("Only draft vouchers can be modified");
        }
        return voucher;
    }

    private Voucher loadForUpdate(Long id) {
        Voucher voucher = voucherRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        assertCanView(voucher);
        return voucher;
    }

    private void assertCanView(Voucher voucher) {
        UserPrincipal principal = SecurityUtils.currentUser();
        if (principal.getRole() == Role.EMPLOYEE && !isOwner(voucher, principal.getId())) {
            throw new ResourceNotFoundException("Voucher not found");
        }
        if (principal.getRole() != Role.EMPLOYEE
                && principal.getRole() != Role.DIRECTOR
                && principal.getRole() != Role.ACCOUNTS) {
            throw new AccessDeniedException("You do not have access to this voucher");
        }
    }

    private boolean isOwner(Voucher voucher) {
        return isOwner(voucher, SecurityUtils.currentUser().getId());
    }

    private boolean isOwner(Voucher voucher, Long userId) {
        return voucher.getCreatedBy() != null && voucher.getCreatedBy().getId().equals(userId);
    }

    private User requireRole(Role role) {
        UserPrincipal principal = SecurityUtils.currentUser();
        if (principal.getRole() != role) {
            throw new AccessDeniedException("This action requires the " + role.name().toLowerCase() + " role");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void transition(Voucher voucher, VoucherStatus to, User actor, String remarks) {
        VoucherStatus from = voucher.getStatus();
        if (!isAllowed(from, to)) {
            throw new InvalidStateTransitionException(from, to);
        }
        voucher.setStatus(to);
        recordHistory(voucher, from, to, actor, remarks);
    }

    private boolean isAllowed(VoucherStatus from, VoucherStatus to) {
        return switch (to) {
            case SUBMITTED -> from == VoucherStatus.DRAFT;
            case PENDING_APPROVAL -> from == VoucherStatus.SUBMITTED;
            case APPROVED, REJECTED -> from == VoucherStatus.PENDING_APPROVAL;
            case DRAFT -> false;
        };
    }

    private void recordHistory(Voucher voucher, VoucherStatus from, VoucherStatus to, User actor, String remarks) {
        VoucherStatusHistory history = new VoucherStatusHistory();
        history.setVoucher(voucher);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedBy(actor);
        history.setRemarks(remarks);
        history.setChangedAt(Instant.now(clock));
        historyRepository.save(history);
    }

    private VoucherResponse toDetail(Voucher voucher) {
        var history = historyRepository.findByVoucherIdOrderByChangedAtAscIdAsc(voucher.getId());
        boolean owner = isOwner(voucher);
        return voucherMapper.toDetail(voucher, history, owner);
    }
}
