package com.revende.backend.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class OneTimeTokenTest {

    private static final Instant NOW = Instant.parse("2026-08-28T10:00:00Z");

    @Test
    void shouldAcceptTheTokenItIssued() {
        var issued = OneTimeToken.issue(Duration.ofHours(1), NOW);
        assertThat(issued.token().isValid(issued.plainText(), NOW)).isTrue();
    }

    @Test
    void shouldRejectTokenAfterExpiry() {
        var issued = OneTimeToken.issue(Duration.ofHours(1), NOW);
        assertThat(issued.token().isValid(issued.plainText(), NOW.plus(Duration.ofHours(2))))
                .isFalse();
    }

    @Test
    void shouldRejectTokenAlreadyUsed() {
        var issued = OneTimeToken.issue(Duration.ofHours(1), NOW);
        var used = issued.token().markUsed(NOW);
        assertThat(used.isValid(issued.plainText(), NOW)).isFalse();
    }

    @Test
    void shouldRejectDifferentToken() {
        var issued = OneTimeToken.issue(Duration.ofHours(1), NOW);
        var other = OneTimeToken.issue(Duration.ofHours(1), NOW);
        assertThat(issued.token().isValid(other.plainText(), NOW)).isFalse();
    }

    @Test
    void shouldNeverStoreThePlainTextValue() {
        var issued = OneTimeToken.issue(Duration.ofHours(1), NOW);
        assertThat(issued.token().hash()).isNotEqualTo(issued.plainText());
    }
}
