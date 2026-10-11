package com.abc.expensevoucher.dto.auth;

import com.abc.expensevoucher.entity.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        String employeeCode,
        String department,
        String signatureFileName,
        boolean active
) {
}
