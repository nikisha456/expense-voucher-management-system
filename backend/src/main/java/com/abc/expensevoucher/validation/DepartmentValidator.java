package com.abc.expensevoucher.validation;

import com.abc.expensevoucher.entity.Department;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DepartmentValidator implements ConstraintValidator<ValidDepartment, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return Department.isValid(value);
    }
}
