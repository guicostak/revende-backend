package com.revende.backend.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.revende.backend.identity.domain.exception.AccountBlockedException;
import com.revende.backend.identity.domain.exception.EmailAlreadyVerifiedException;
import com.revende.backend.identity.domain.exception.InvalidTokenException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserTest {

    private static final Instant NOW = Instant.parse("2026-08-28T10:00:00Z");
    private static final PasswordHash HASH = new PasswordHash("$2a$10$abcdefghijklmnopqrstuv");

    private static User newUser() {
        return User.register(new PersonName("Maria Souza"), new EmailAddress("maria@revende.com"), HASH);
    }

    private static SellerProfile sellerProfile() {
        return new SellerProfile(
                new Cpf("52998224725"),
                new Address("Rua das Flores", "100", null, "Centro", "Belo Horizonte", "mg", "30110-001"),
                new PixKey(PixKeyType.EMAIL, "maria@revende.com"),
                new PhoneNumber("+5531999998888"));
    }

    @Nested
    class Registration {

        @Test
        void shouldStartActiveButUnverified() {
            var user = newUser();
            assertThat(user.status()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(user.isEmailVerified()).isFalse();
        }

        @Test
        void shouldHaveIdentityFromTheStart() {
            assertThat(newUser().id()).isNotNull();
        }

        @Test
        void shouldGiveDistinctIdentityToEachUser() {
            assertThat(newUser().id()).isNotEqualTo(newUser().id());
        }

        @Test
        void shouldNotExposeCredentialsInToString() {
            assertThat(newUser().toString()).doesNotContain("maria@revende.com").doesNotContain("$2a$");
        }
    }

    @Nested
    class EmailVerification {

        @Test
        void shouldVerifyWithTheIssuedToken() {
            var user = newUser();
            var token = user.issueEmailVerificationToken(NOW);

            user.verifyEmail(token, NOW);

            assertThat(user.isEmailVerified()).isTrue();
        }

        @Test
        void shouldRejectExpiredToken() {
            var user = newUser();
            var token = user.issueEmailVerificationToken(NOW);

            assertThatThrownBy(() -> user.verifyEmail(token, NOW.plus(Duration.ofHours(25))))
                    .isInstanceOf(InvalidTokenException.class);
            assertThat(user.isEmailVerified()).isFalse();
        }

        @Test
        void shouldRejectTokenReuse() {
            var user = newUser();
            var token = user.issueEmailVerificationToken(NOW);
            user.verifyEmail(token, NOW);

            assertThatThrownBy(() -> user.verifyEmail(token, NOW)).isInstanceOf(EmailAlreadyVerifiedException.class);
        }

        @Test
        void shouldInvalidatePreviousTokenWhenIssuingANewOne() {
            var user = newUser();
            var first = user.issueEmailVerificationToken(NOW);
            user.issueEmailVerificationToken(NOW);

            assertThatThrownBy(() -> user.verifyEmail(first, NOW)).isInstanceOf(InvalidTokenException.class);
        }
    }

    @Nested
    class PasswordReset {

        @Test
        void shouldReplacePasswordWithValidToken() {
            var user = newUser();
            var token = user.issuePasswordResetToken(NOW);
            var newHash = new PasswordHash("$2a$10$zyxwvutsrqponmlkjihgfe");

            user.resetPassword(token, newHash, NOW);

            assertThat(user.passwordHash()).isEqualTo(newHash);
        }

        @Test
        void shouldRejectResetTokenAfterThirtyMinutes() {
            var user = newUser();
            var token = user.issuePasswordResetToken(NOW);

            assertThatThrownBy(() -> user.resetPassword(token, HASH, NOW.plus(Duration.ofMinutes(31))))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        void shouldDiscardPendingResetWhenPasswordChangesByOtherMeans() {
            var user = newUser();
            var token = user.issuePasswordResetToken(NOW);
            user.changePassword(new PasswordHash("$2a$10$aaaaaaaaaaaaaaaaaaaaaa"));

            assertThatThrownBy(() -> user.resetPassword(token, HASH, NOW)).isInstanceOf(InvalidTokenException.class);
        }
    }

    @Nested
    class Capabilities {

        @Test
        void shouldNotAllowBuyingBeforeEmailVerification() {
            assertThat(newUser().canBuy()).isFalse();
        }

        @Test
        void shouldAllowBuyingOnceEmailIsVerified() {
            var user = newUser();
            user.verifyEmail(user.issueEmailVerificationToken(NOW), NOW);

            assertThat(user.canBuy()).isTrue();
        }

        @Test
        void shouldNotBeEligibleToSellWithoutSellerProfile() {
            var user = newUser();
            user.verifyEmail(user.issueEmailVerificationToken(NOW), NOW);

            assertThat(user.isEligibleToSell()).isFalse();
        }

        @Test
        void shouldBeEligibleToSellWithVerifiedEmailAndCompleteProfile() {
            var user = newUser();
            user.verifyEmail(user.issueEmailVerificationToken(NOW), NOW);
            user.completeSellerProfile(sellerProfile());

            assertThat(user.isEligibleToSell()).isTrue();
        }

        @Test
        void shouldNotBeEligibleToSellWithProfileButUnverifiedEmail() {
            var user = newUser();
            user.completeSellerProfile(sellerProfile());

            assertThat(user.isEligibleToSell()).isFalse();
        }
    }

    @Nested
    class Moderation {

        @Test
        void shouldDenyEverythingWhileBlocked() {
            var user = newUser();
            user.verifyEmail(user.issueEmailVerificationToken(NOW), NOW);
            user.completeSellerProfile(sellerProfile());

            user.block();

            assertThat(user.canAuthenticate()).isFalse();
            assertThat(user.canBuy()).isFalse();
            assertThat(user.isEligibleToSell()).isFalse();
        }

        @Test
        void shouldKeepEmailVerificationAfterReactivation() {
            var user = newUser();
            user.verifyEmail(user.issueEmailVerificationToken(NOW), NOW);

            user.block();
            user.reactivate();

            assertThat(user.isEmailVerified()).isTrue();
            assertThat(user.canBuy()).isTrue();
        }

        @Test
        void shouldRefuseSellerProfileWhileBlocked() {
            var user = newUser();
            user.block();

            assertThatThrownBy(() -> user.completeSellerProfile(sellerProfile()))
                    .isInstanceOf(AccountBlockedException.class);
        }
    }
}
