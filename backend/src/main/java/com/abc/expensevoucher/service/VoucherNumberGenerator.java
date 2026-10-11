package com.abc.expensevoucher.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class VoucherNumberGenerator {

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public String next() {
        Long sequence = jdbcTemplate.queryForObject("SELECT nextval('voucher_number_seq')", Long.class);
        if (sequence == null) {
            throw new IllegalStateException("Failed to generate a voucher number");
        }
        int year = LocalDate.now(clock).getYear();
        return String.format("EV-%d-%06d", year, sequence);
    }
}
