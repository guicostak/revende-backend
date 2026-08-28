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

## `equals` e `hashCode` em entidade

O erro clássico é gerar os dois a partir de todos os campos — o que `@Data` do Lombok faz.

Dois estragos:

1. **ID gerado pelo banco é `null` antes do flush.** Se a entidade entra num `HashSet`
   antes de persistir e ganha ID depois, o hash muda enquanto ela está na coleção, e ela
   fica inalcançável ali dentro.
2. **Acessar relação `LAZY` dentro de `equals`** dispara carregamento, ou estoura
   `LazyInitializationException` fora de transação.

Recomendação da comunidade: **não escreva `equals`/`hashCode` em entidade**, ou escreva à
mão com cuidado. A igualdade por identidade que a JPA dá por padrão costuma ser a mais segura.

E **não use `@Data` do Lombok em entidade** — ele gera `equals`, `hashCode` e `toString`
que passam por relações lazy e por ID mutável.

> No modelo hexagonal isso quase desaparece: o **agregado de domínio** tem igualdade por
> identidade de negócio e a **entidade JPA** é estrutura burra de persistência. Separar as
> duas resolve o problema pela raiz, em vez de administrá-lo.

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
