---
tags: [dados, referencia, jpa]
---

# Armadilhas do Spring Data JPA

Onde o Hibernate castiga quem não presta atenção. Complementa [[Modelagem PostgreSQL]].

## N+1

Uma consulta traz N registros, e o Hibernate dispara mais N para carregar as relações.
Aparece tanto por `LAZY` acessado em laço quanto por `EAGER`, que carrega sem ninguém pedir.

**`@ManyToOne` é `EAGER` por padrão em JPA.** Este é o detalhe que pega quase todo mundo:
`@OneToMany` é `LAZY`, `@ManyToOne` não é. A v1 do Revende tinha `EAGER` explícito em
`event` e `seller`, mas teria o mesmo problema se tivesse omitido.

Solução: `LAZY` sempre, e consulta de listagem com `JOIN FETCH` ou projeção.
Verificação honesta: **contar queries em teste**, não ler o código.

## `@Data` do Lombok: o padrão do projeto

Entidades usam `@Data`, `@Builder`, `@NoArgsConstructor` e `@AllArgsConstructor`. Getters,
setters, `equals`, `hashCode` e `toString` vêm do Lombok em vez de escritos à mão.

Dois efeitos conhecidos, para ter em mente ao usar:

**`equals` e `hashCode` cobrem todos os campos.** Com ID gerado pelo banco, o valor é `null`
antes do flush — então entidade que entra num `HashSet` antes de persistir muda de hash
depois e fica inalcançável naquela coleção. Na prática: não guarde entidade não persistida
em coleção baseada em hash.

**`toString` percorre as relações.** Em associação `LAZY` isso dispara carregamento, ou
estoura `LazyInitializationException` fora de transação. Quando houver relação mapeada,
exclua do `toString`:

```java
@ToString(exclude = "listings")
```

E campo sensível também sai:

```java
@ToString.Exclude
private String passwordHash;
```

## `open-in-view`

Ligado por padrão no Spring Boot. Mantém a sessão do Hibernate aberta durante a renderização
da resposta, o que esconde N+1 e faz carregamento acontecer fora da camada que deveria
controlá-lo. **Desligue** — já está `false` no `application.yml`.

O efeito colateral é bom: o que estava escondido vira `LazyInitializationException`, e o
erro aparece em teste em vez de virar lentidão em produção.

## Fontes

- [JPA Entity Equality — Baeldung](https://www.baeldung.com/jpa-entity-equality)
- [The final article about equals and hashCode for JPA entities — JPA Buddy](https://jpa-buddy.com/blog/hopefully-the-final-article-about-equals-and-hashcode-for-jpa-entities-with-db-generated-ids/)
- [Spring Data JPA Best Practices: Entity Design Guide](https://protsenko.dev/spring-data-jpa-best-practices-entity-design-guide/)
- [Understanding and Solving the N+1 Problem in Spring Data JPA — DEV](https://dev.to/sadiul_hakim/understanding-and-solving-the-n1-problem-in-spring-data-jpa-2b6f)
