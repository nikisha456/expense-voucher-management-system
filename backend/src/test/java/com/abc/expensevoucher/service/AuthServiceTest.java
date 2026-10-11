package com.abc.expensevoucher.service;

import com.abc.expensevoucher.dto.auth.RegisterRequest;
import com.abc.expensevoucher.dto.auth.UserResponse;
import com.abc.expensevoucher.entity.Role;
import com.abc.expensevoucher.entity.User;
import com.abc.expensevoucher.exception.DuplicateEmailException;
import com.abc.expensevoucher.mapper.UserMapper;
import com.abc.expensevoucher.repository.UserRepository;
import com.abc.expensevoucher.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-10T04:30:00Z"), ZoneId.of("Asia/Kolkata"));
        service = new AuthService(
                authenticationManager,
                userRepository,
                jwtService,
                new UserMapper(),
                passwordEncoder,
                clock
        );
    }

    @Test
    void registerCreatesActiveEmployeeAndDoesNotReturnPassword() {
        when(userRepository.existsByEmailIgnoreCase("neha.sharma@abc.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2b$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            return user;
        });

        UserResponse response = service.register(request("Neha Sharma", "  Neha.Sharma@abc.com  ", "EMP-2001"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getEmail()).isEqualTo("neha.sharma@abc.com");
        assertThat(saved.getPassword()).isEqualTo("$2b$hashed");
        assertThat(saved.getEmployeeCode()).isEqualTo("EMP-2001");
        assertThat(saved.getDepartment()).isEqualTo("Engineering");
        assertThat(saved.getCreatedAt()).isEqualTo(Instant.parse("2026-10-10T04:30:00Z"));
        assertThat(saved.getUpdatedAt()).isEqualTo(Instant.parse("2026-10-10T04:30:00Z"));
        assertThat(response.role()).isEqualTo(Role.EMPLOYEE);
        assertThat(response.email()).isEqualTo("neha.sharma@abc.com");
        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.name()).isEqualTo("Neha Sharma");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("ananya.iyer@abc.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request("Ananya Iyer", "Ananya.Iyer@abc.com", null)))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("An account with this email already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerForcesEmployeeEvenWhenCallerIntendsAnotherRole() {
        when(userRepository.existsByEmailIgnoreCase("guest@abc.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = service.register(request("Guest User", "guest@abc.com", null));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(response.role()).isEqualTo(Role.EMPLOYEE);
        assertThat(captor.getValue().getRole()).isNotEqualTo(Role.DIRECTOR);
        assertThat(captor.getValue().getRole()).isNotEqualTo(Role.ACCOUNTS);
    }

    private RegisterRequest request(String name, String email, String employeeId) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword("Password@123");
        request.setConfirmPassword("Password@123");
        request.setEmployeeId(employeeId);
        request.setDepartment("engineering");
        return request;
    }
}
