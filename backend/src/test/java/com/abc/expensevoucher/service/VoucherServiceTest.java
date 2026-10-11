package com.abc.expensevoucher.service;

import com.abc.expensevoucher.dto.voucher.VoucherRequest;
import com.abc.expensevoucher.entity.ExpenseCategory;
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
import com.abc.expensevoucher.util.AmountInWords;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private VoucherStatusHistoryRepository historyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VoucherNumberGenerator numberGenerator;
    @Mock
    private FileStorageService fileStorageService;

    private VoucherService service;
    private User employee;
    private User otherEmployee;
    private User director;
    private User accounts;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-10T04:30:00Z"), ZoneId.of("Asia/Kolkata"));
        service = new VoucherService(
                voucherRepository,
                historyRepository,
                userRepository,
                numberGenerator,
                fileStorageService,
                new VoucherMapper(new AmountInWords()),
                clock
        );
        employee = user(3L, "Ananya Iyer", "ananya.iyer@abc.com", Role.EMPLOYEE, "EMP-1001");
        otherEmployee = user(4L, "Karan Patel", "karan.patel@abc.com", Role.EMPLOYEE, "EMP-1002");
        director = user(1L, "Priya Sharma", "director@abc.com", Role.DIRECTOR, null);
        accounts = user(2L, "Rahul Mehta", "accounts@abc.com", Role.ACCOUNTS, null);
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createStartsAsDraftWithGeneratedNumber() {
        login(employee);
        when(userRepository.findById(3L)).thenReturn(Optional.of(employee));
        when(numberGenerator.next()).thenReturn("EV-2026-000099");
        when(voucherRepository.save(any())).thenAnswer(invocation -> {
            Voucher voucher = invocation.getArgument(0);
            voucher.setId(99L);
            return voucher;
        });
        when(historyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.findByVoucherIdOrderByChangedAtAscIdAsc(99L)).thenReturn(List.of());

        var response = service.create(validRequest());

        assertThat(response.getStatus()).isEqualTo(VoucherStatus.DRAFT);
        assertThat(response.getVoucherNumber()).isEqualTo("EV-2026-000099");
        assertThat(response.getEmployeeName()).isEqualTo("Ananya Iyer");
        ArgumentCaptor<VoucherStatusHistory> captor = ArgumentCaptor.forClass(VoucherStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getFromStatus()).isNull();
        assertThat(captor.getValue().getToStatus()).isEqualTo(VoucherStatus.DRAFT);
    }

    @Test
    void createRejectsFutureExpenseDateAndInvalidAmountAndDepartment() {
        login(employee);
        when(userRepository.findById(3L)).thenReturn(Optional.of(employee));

        VoucherRequest future = validRequest();
        future.setExpenseDate(LocalDate.of(2026, 10, 11));
        assertThatThrownBy(() -> service.create(future)).isInstanceOf(BusinessRuleException.class);

        VoucherRequest zero = validRequest();
        zero.setAmount(BigDecimal.ZERO);
        assertThatThrownBy(() -> service.create(zero)).isInstanceOf(BusinessRuleException.class);

        VoucherRequest department = validRequest();
        department.setDepartmentName("Space");
        assertThatThrownBy(() -> service.create(department)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void employeeCannotEditAnotherEmployeesDraft() {
        login(employee);
        when(userRepository.findById(3L)).thenReturn(Optional.of(employee));
        when(voucherRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(draftOwnedBy(otherEmployee)));

        assertThatThrownBy(() -> service.update(8L, validRequest())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submittedVoucherIsReadOnly() {
        login(employee);
        when(userRepository.findById(3L)).thenReturn(Optional.of(employee));
        Voucher pending = draftOwnedBy(employee);
        pending.setStatus(VoucherStatus.PENDING_APPROVAL);
        when(voucherRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.update(8L, validRequest())).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(() -> service.delete(8L)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void submitRequiresSignatureThenRecordsBothTransitions() {
        login(employee);
        when(userRepository.findById(3L)).thenReturn(Optional.of(employee));
        Voucher draft = draftOwnedBy(employee);
        when(voucherRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.submit(8L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("signature");

        draft.setEmployeeSignaturePath("employee.png");
        when(voucherRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.findByVoucherIdOrderByChangedAtAscIdAsc(8L)).thenReturn(List.of());

        var response = service.submit(8L);

        assertThat(response.getStatus()).isEqualTo(VoucherStatus.PENDING_APPROVAL);
        ArgumentCaptor<VoucherStatusHistory> captor = ArgumentCaptor.forClass(VoucherStatusHistory.class);
        verify(historyRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(VoucherStatusHistory::getToStatus)
                .containsExactly(VoucherStatus.SUBMITTED, VoucherStatus.PENDING_APPROVAL);
    }

    @Test
    void approveRequiresDirectorSignatureAndPendingStatus() {
        login(director);
        when(userRepository.findById(1L)).thenReturn(Optional.of(director));
        Voucher pending = draftOwnedBy(employee);
        pending.setStatus(VoucherStatus.PENDING_APPROVAL);
        pending.setEmployeeSignaturePath("employee.png");
        when(voucherRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.approve(8L, null, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Director signature");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(fileStorageService.store(file)).thenReturn("director.png");
        when(voucherRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.findByVoucherIdOrderByChangedAtAscIdAsc(8L)).thenReturn(List.of());

        var approved = service.approve(8L, file, false);

        assertThat(approved.getStatus()).isEqualTo(VoucherStatus.APPROVED);
        assertThat(approved.getDirectorSignatureFileName()).isEqualTo("director.png");
        assertThat(approved.getApprovalDate()).isEqualTo(LocalDate.of(2026, 10, 10));
    }

    @Test
    void rejectRequiresReasonAndStoresIt() {
        login(director);
        when(userRepository.findById(1L)).thenReturn(Optional.of(director));
        Voucher pending = draftOwnedBy(employee);
        pending.setStatus(VoucherStatus.PENDING_APPROVAL);
        when(voucherRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.reject(8L, "Too short")).isInstanceOf(BusinessRuleException.class);

        when(voucherRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.findByVoucherIdOrderByChangedAtAscIdAsc(8L)).thenReturn(List.of());

        var rejected = service.reject(8L, "Missing boarding passes for this trip.");

        assertThat(rejected.getStatus()).isEqualTo(VoucherStatus.REJECTED);
        assertThat(rejected.getRejectionReason()).contains("boarding");
    }

    @Test
    void visibilityFollowsRole() {
        Voucher voucher = draftOwnedBy(employee);
        when(voucherRepository.findById(8L)).thenReturn(Optional.of(voucher));
        when(historyRepository.findByVoucherIdOrderByChangedAtAscIdAsc(8L)).thenReturn(List.of());

        login(otherEmployee);
        assertThatThrownBy(() -> service.getById(8L)).isInstanceOf(ResourceNotFoundException.class);

        login(director);
        assertThat(service.getById(8L).getVoucherNumber()).isEqualTo("EV-2026-000008");

        login(accounts);
        assertThat(service.getById(8L).getEmployeeName()).isEqualTo("Ananya Iyer");
    }

    @Test
    void onlyDirectorCanApprove() {
        login(employee);
        assertThatThrownBy(() -> service.approve(8L, null, false)).isInstanceOf(AccessDeniedException.class);
    }

    private void login(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private VoucherRequest validRequest() {
        VoucherRequest request = new VoucherRequest();
        request.setVoucherDate(LocalDate.of(2026, 10, 1));
        request.setExpenseDate(LocalDate.of(2026, 10, 1));
        request.setDepartmentName("Engineering");
        request.setExpenseTitle("Client visit");
        request.setExpenseCategory(ExpenseCategory.TRAVEL);
        request.setExpenseDescription("Taxi and meals");
        request.setAmount(new BigDecimal("1500.50"));
        return request;
    }

    private Voucher draftOwnedBy(User owner) {
        Voucher voucher = new Voucher();
        voucher.setId(8L);
        voucher.setVoucherNumber("EV-2026-000008");
        voucher.setVoucherDate(LocalDate.of(2026, 10, 1));
        voucher.setExpenseDate(LocalDate.of(2026, 10, 1));
        voucher.setDepartmentName("Engineering");
        voucher.setExpenseTitle("Client visit");
        voucher.setExpenseCategory(ExpenseCategory.TRAVEL);
        voucher.setAmount(new BigDecimal("1500.50"));
        voucher.setEmployeeName(owner.getName());
        voucher.setEmployeeCode(owner.getEmployeeCode());
        voucher.setStatus(VoucherStatus.DRAFT);
        voucher.setCreatedBy(owner);
        return voucher;
    }

    private User user(Long id, String name, String email, Role role, String code) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("hashed");
        user.setRole(role);
        user.setEmployeeCode(code);
        user.setActive(true);
        return user;
    }
}
