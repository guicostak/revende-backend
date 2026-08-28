package com.revende.backend.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.revende.backend.shared.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

class EmailAddressTest {

    @Test
    void shouldNormalizeCaseAndSurroundingWhitespace() {
        assertThat(new EmailAddress("  Maria@Revende.COM  ").value()).isEqualTo("maria@revende.com");
    }

    @Test
    void shouldTreatCaseVariationsAsTheSameAddress() {
        assertThat(new EmailAddress("Maria@revende.com")).isEqualTo(new EmailAddress("maria@revende.com"));
    }

    @Test
    void shouldRejectAddressWithoutDomain() {
        assertThatThrownBy(() -> new EmailAddress("maria@revende")).isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectBlankAddress() {
        assertThatThrownBy(() -> new EmailAddress("   ")).isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectAddressLongerThanRfcLimit() {
        String tooLong = "a".repeat(320) + "@revende.com";
        assertThatThrownBy(() -> new EmailAddress(tooLong)).isInstanceOf(ValidationException.class);
    }
}
