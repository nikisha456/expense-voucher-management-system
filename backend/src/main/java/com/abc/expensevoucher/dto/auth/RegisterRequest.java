package com.abc.expensevoucher.dto.auth;

import com.abc.expensevoucher.validation.PasswordMatch;
import com.abc.expensevoucher.validation.ValidDepartment;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PasswordMatch
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(example = "Neha Sharma")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 180, message = "Email must be at most 180 characters")
    @Schema(example = "neha.sharma@abc.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "Password must be at least 8 characters and include an uppercase letter, a lowercase letter, and a number"
    )
    @Schema(example = "Password@123")
    private String password;

    @NotBlank(message = "Confirm your password")
    @Schema(example = "Password@123")
    private String confirmPassword;

    @Size(max = 50, message = "Employee ID must be at most 50 characters")
    @Schema(example = "EMP-2001")
    private String employeeId;

    @NotBlank(message = "Department is required")
    @ValidDepartment
    @Size(max = 100, message = "Department must be at most 100 characters")
    @Schema(example = "Engineering")
    private String department;
}
