package com.abc.expensevoucher.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AmountInWords {

    private static final String[] ONES = {
            "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public String toWords(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        long rupees = scaled.longValue();
        int paise = scaled.remainder(BigDecimal.ONE).movePointRight(2).abs().intValue();
        StringBuilder words = new StringBuilder("Rupees ");
        words.append(rupees == 0 ? "Zero" : convert(rupees));
        if (paise > 0) {
            words.append(" and ").append(convert(paise)).append(" Paise");
        }
        words.append(" Only");
        return words.toString();
    }

    private String convert(long number) {
        if (number < 20) {
            return ONES[(int) number];
        }
        if (number < 100) {
            return TENS[(int) number / 10] + (number % 10 == 0 ? "" : " " + ONES[(int) number % 10]);
        }
        if (number < 1_000) {
            return convert(number / 100) + " Hundred" + remainder(number, 100);
        }
        if (number < 100_000) {
            return convert(number / 1_000) + " Thousand" + remainder(number, 1_000);
        }
        if (number < 10_000_000) {
            return convert(number / 100_000) + " Lakh" + remainder(number, 100_000);
        }
        return convert(number / 10_000_000) + " Crore" + remainder(number, 10_000_000);
    }

    private String remainder(long number, long divisor) {
        long remaining = number % divisor;
        return remaining == 0 ? "" : " " + convert(remaining);
    }
}
