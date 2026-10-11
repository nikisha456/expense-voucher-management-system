package com.abc.expensevoucher.validation;

import com.abc.expensevoucher.dto.auth.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest value, ConstraintValidatorContext context) {
        if (value == null || value.getPassword() == null || value.getConfirmPassword() == null) {
            return true;
        }
        if (value.getPassword().equals(value.getConfirmPassword())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Passwords do not match")
                .addPropertyNode("confirmPassword")
                .addConstraintViolation();
        return false;
    }
}
