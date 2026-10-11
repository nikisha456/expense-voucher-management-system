package com.abc.expensevoucher.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

public class NotFutureDateValidator implements ConstraintValidator<NotFutureDate, LocalDate> {

    @Autowired(required = false)
    private Clock clock;

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        ZoneId zone = clock == null ? ZoneId.of("Asia/Kolkata") : clock.getZone();
        return !value.isAfter(LocalDate.now(zone));
    }
}
