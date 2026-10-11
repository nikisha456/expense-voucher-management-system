package com.abc.expensevoucher.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AmountInWordsTest {

    private final AmountInWords amountInWords = new AmountInWords();

    @Test
    void convertsRupeesAndPaise() {
        assertThat(amountInWords.toWords(new BigDecimal("980.00")))
                .isEqualTo("Rupees Nine Hundred Eighty Only");
        assertThat(amountInWords.toWords(new BigDecimal("5600.50")))
                .isEqualTo("Rupees Five Thousand Six Hundred and Fifty Paise Only");
        assertThat(amountInWords.toWords(new BigDecimal("125000")))
                .isEqualTo("Rupees One Lakh Twenty Five Thousand Only");
    }
}
