package com.abc.expensevoucher.service;

import com.abc.expensevoucher.dto.auth.LoginRequest;
import com.abc.expensevoucher.dto.auth.LoginResponse;
import com.abc.expensevoucher.dto.auth.RegisterRequest;
import com.abc.expensevoucher.dto.auth.UserResponse;
import com.abc.expensevoucher.entity.Department;
import com.abc.expensevoucher.entity.Role;
import com.abc.expensevoucher.entity.User;
import com.abc.expensevoucher.exception.AccountDisabledException;
import com.abc.expensevoucher.exception.DuplicateEmailException;
import com.abc.expensevoucher.exception.InvalidCredentialsException;
import com.abc.expensevoucher.mapper.UserMapper;
import com.abc.expensevoucher.repository.UserRepository;
import com.abc.expensevoucher.security.JwtService;
import com.abc.expensevoucher.security.UserPrincipal;
import com.abc.expensevoucher.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (DisabledException ex) {
            throw new AccountDisabledException("Account is disabled");
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();
        }
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);
        log.info("User {} logged in", user.getEmail());
        return new LoginResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationSeconds(), userMapper.toResponse(user));
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException();
        }
        Instant now = Instant.now(clock);
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.EMPLOYEE);
        user.setActive(true);
        String employeeId = request.getEmployeeId();
        if (employeeId != null && !employeeId.isBlank()) {
            user.setEmployeeCode(employeeId.trim());
        }
        user.setDepartment(Department.canonical(request.getDepartment()));
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser() {
        UserPrincipal principal = SecurityUtils.currentUser();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(InvalidCredentialsException::new);
        return userMapper.toResponse(user);
    }
}
