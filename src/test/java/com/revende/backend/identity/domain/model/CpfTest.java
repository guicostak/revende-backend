package com.revende.backend.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.revende.backend.identity.domain.exception.InvalidCpfException;
import org.junit.jupiter.api.Test;

class CpfTest {

    private static final String VALID = "52998224725";

    @Test
    void shouldStripFormattingCharacters() {
        assertThat(new Cpf("529.982.247-25").value()).isEqualTo(VALID);
    }

    @Test
    void shouldRejectWrongCheckDigit() {
        assertThatThrownBy(() -> new Cpf("52998224726")).isInstanceOf(InvalidCpfException.class);
    }

    @Test
    void shouldRejectRepeatedDigitsEvenWhenCheckDigitMatches() {
        assertThatThrownBy(() -> new Cpf("11111111111")).isInstanceOf(InvalidCpfException.class);
    }

    @Test
    void shouldRejectWrongLength() {
        assertThatThrownBy(() -> new Cpf("529982247")).isInstanceOf(InvalidCpfException.class);
    }

    @Test
    void shouldNotExposeFullNumberInToString() {
        assertThat(new Cpf(VALID).toString()).doesNotContain(VALID).contains("***");
    }
}
