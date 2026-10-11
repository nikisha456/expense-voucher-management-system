package com.abc.expensevoucher.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Schema(example = "ananya.iyer@abc.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(example = "Password@123")
    private String password;
}
