package com.abc.expensevoucher.mapper;

import com.abc.expensevoucher.dto.auth.UserResponse;
import com.abc.expensevoucher.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getEmployeeCode(),
                user.getDepartment(),
                user.getSignaturePath(),
                user.isActive()
        );
    }
}
