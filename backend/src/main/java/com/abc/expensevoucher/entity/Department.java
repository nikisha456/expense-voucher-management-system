package com.abc.expensevoucher.entity;

import com.abc.expensevoucher.exception.BusinessRuleException;

import java.util.Arrays;

public enum Department {
    ENGINEERING("Engineering"),
    FINANCE("Finance"),
    HR("HR"),
    SALES("Sales"),
    MARKETING("Marketing"),
    OPERATIONS("Operations"),
    IT_SUPPORT("IT Support"),
    LEGAL("Legal"),
    ADMINISTRATION("Administration");

    private final String label;

    Department(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(department -> department.label.equalsIgnoreCase(value.trim()));
    }

    public static String canonical(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("Department is required");
        }
        return Arrays.stream(values())
                .filter(department -> department.label.equalsIgnoreCase(value.trim()))
                .findFirst()
                .map(Department::getLabel)
                .orElseThrow(() -> new BusinessRuleException("Department is invalid"));
    }
}
