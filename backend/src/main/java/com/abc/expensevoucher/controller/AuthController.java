package com.abc.expensevoucher.controller;

import com.abc.expensevoucher.dto.auth.LoginRequest;
import com.abc.expensevoucher.dto.auth.LoginResponse;
import com.abc.expensevoucher.dto.auth.RegisterRequest;
import com.abc.expensevoucher.dto.auth.UserResponse;
import com.abc.expensevoucher.dto.common.ApiResponse;
import com.abc.expensevoucher.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(
            summary = "Register an employee account",
            description = "Public. Creates an active EMPLOYEE only. A role sent by the client is ignored. Does not sign the user in."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Account created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Account created successfully", authService.register(request));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Login with email and password")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successful", authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Current authenticated user")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(authService.currentUser());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout acknowledgement for stateless JWT clients")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok("Logged out", null);
    }
}
