package com.revende.backend.identity.domain.model;

import java.util.Objects;

/**
 * Dados exigidos para o cadastro do vendedor como recebedor no gateway de pagamento.
 *
 * <p>É tudo-ou-nada de propósito: se o objeto existe, está completo. Não há como ter meio
 * vendedor — com CPF e sem chave Pix — porque a invariante é garantida na construção em vez
 * de depender de verificação espalhada pelos casos de uso.
 */
public record SellerProfile(Cpf cpf, Address address, PixKey pixKey, PhoneNumber phone) {

    public SellerProfile {
        Objects.requireNonNull(cpf, "CPF é obrigatório para vender");
        Objects.requireNonNull(address, "Endereço é obrigatório para vender");
        Objects.requireNonNull(pixKey, "Chave Pix é obrigatória para vender");
        Objects.requireNonNull(phone, "Telefone é obrigatório para vender");
    }
}
