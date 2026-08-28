package com.revende.backend.identity.domain.model;

import com.revende.backend.identity.domain.exception.AccountBlockedException;
import com.revende.backend.identity.domain.exception.EmailAlreadyVerifiedException;
import com.revende.backend.identity.domain.exception.InvalidTokenException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Pessoa com conta no Revende. Não há distinção entre comprador e vendedor: a mesma pessoa
 * compra e vende, e "vendedor" é uma capacidade destravada por dados completos, não um tipo.
 *
 * <p>A identidade é gerada no domínio, então o agregado nasce completo e nunca existe num
 * estado sem identidade.
 */
public final class User {

    private static final Duration EMAIL_VERIFICATION_VALIDITY = Duration.ofHours(24);
    private static final Duration PASSWORD_RESET_VALIDITY = Duration.ofMinutes(30);

    private final UserId id;
    private final EmailAddress email;

    private PersonName name;
    private PasswordHash passwordHash;
    private PhoneNumber phone;
    private ProfilePicture profilePicture;
    private AccountStatus status;
    private boolean emailVerified;
    private SellerProfile sellerProfile;
    private OneTimeToken emailVerificationToken;
    private OneTimeToken passwordResetToken;

    private User(UserId id, EmailAddress email, PersonName name, PasswordHash passwordHash) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
        this.status = AccountStatus.ACTIVE;
        this.emailVerified = false;
    }

    public static User register(PersonName name, EmailAddress email, PasswordHash passwordHash) {
        Objects.requireNonNull(name, "Nome é obrigatório");
        Objects.requireNonNull(email, "E-mail é obrigatório");
        Objects.requireNonNull(passwordHash, "Senha é obrigatória");
        return new User(UserId.newId(), email, name, passwordHash);
    }

    // ---------------------------------------------------------------- verificação de e-mail

    /** Emite um token novo, substituindo qualquer anterior: só um fica válido por vez. */
    public String issueEmailVerificationToken(Instant now) {
        if (emailVerified) {
            throw new EmailAlreadyVerifiedException("E-mail já foi verificado");
        }
        OneTimeToken.Issued issued = OneTimeToken.issue(EMAIL_VERIFICATION_VALIDITY, now);
        this.emailVerificationToken = issued.token();
        return issued.plainText();
    }

    public void verifyEmail(String token, Instant now) {
        if (emailVerified) {
            throw new EmailAlreadyVerifiedException("E-mail já foi verificado");
        }
        if (emailVerificationToken == null || !emailVerificationToken.isValid(token, now)) {
            throw new InvalidTokenException("Token de verificação inválido ou expirado");
        }
        this.emailVerificationToken = emailVerificationToken.markUsed(now);
        this.emailVerified = true;
    }

    // ---------------------------------------------------------------- senha

    public String issuePasswordResetToken(Instant now) {
        OneTimeToken.Issued issued = OneTimeToken.issue(PASSWORD_RESET_VALIDITY, now);
        this.passwordResetToken = issued.token();
        return issued.plainText();
    }

    public void resetPassword(String token, PasswordHash newPasswordHash, Instant now) {
        if (passwordResetToken == null || !passwordResetToken.isValid(token, now)) {
            throw new InvalidTokenException("Token de redefinição inválido ou expirado");
        }
        this.passwordResetToken = passwordResetToken.markUsed(now);
        this.passwordHash = Objects.requireNonNull(newPasswordHash);
    }

    /** Trocar a senha invalida qualquer redefinição pendente. */
    public void changePassword(PasswordHash newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash);
        this.passwordResetToken = null;
    }

    // ---------------------------------------------------------------- perfil

    public void rename(PersonName newName) {
        this.name = Objects.requireNonNull(newName);
    }

    public void updateProfilePicture(ProfilePicture picture) {
        this.profilePicture = Objects.requireNonNull(picture);
    }

    public void removeProfilePicture() {
        this.profilePicture = null;
    }

    public void updatePhone(PhoneNumber newPhone) {
        this.phone = Objects.requireNonNull(newPhone);
    }

    public void completeSellerProfile(SellerProfile profile) {
        if (status == AccountStatus.BLOCKED) {
            throw new AccountBlockedException("Conta bloqueada não pode se cadastrar como vendedora");
        }
        this.sellerProfile = Objects.requireNonNull(profile);
    }

    // ---------------------------------------------------------------- moderação

    public void block() {
        this.status = AccountStatus.BLOCKED;
    }

    /** Reativar não devolve verificação de e-mail: ela nunca foi perdida. */
    public void reactivate() {
        this.status = AccountStatus.ACTIVE;
    }

    // ---------------------------------------------------------------- capacidades

    public boolean canAuthenticate() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean canBuy() {
        return canAuthenticate() && emailVerified;
    }

    /**
     * O que o agregado sabe sozinho. Publicar um anúncio também exige conexão ativa com o
     * parceiro de ingressos e recebedor aprovado no gateway — ambos em outros agregados,
     * então essa composição é responsabilidade do caso de uso, não desta classe.
     */
    public boolean isEligibleToSell() {
        return canBuy() && sellerProfile != null;
    }

    // ---------------------------------------------------------------- acesso

    public UserId id() {
        return id;
    }

    public EmailAddress email() {
        return email;
    }

    public PersonName name() {
        return name;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }

    public AccountStatus status() {
        return status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Optional<PhoneNumber> phone() {
        return Optional.ofNullable(phone);
    }

    public Optional<ProfilePicture> profilePicture() {
        return Optional.ofNullable(profilePicture);
    }

    public Optional<SellerProfile> sellerProfile() {
        return Optional.ofNullable(sellerProfile);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof User user && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "User[" + id + "]";
    }
}
