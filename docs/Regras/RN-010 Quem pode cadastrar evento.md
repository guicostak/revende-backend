---
tags: [regra-de-negocio, catalog, seguranca, parcial]
contexto: Catalog
codigo: src/main/java/com/revende/backend/config/SecurityConfig.java
---

# RN-010 — Quem pode cadastrar evento

> **Regra atual:** qualquer usuário autenticado cria evento no catálogo.
> **Regra pretendida:** não decidida.

## ⚠️ Estado do problema

`SecurityConfig` libera `GET /api/events/**` e protege o resto com `anyRequest().authenticated()`.
Como não existe papel algum ([[Contexto Identity]]), *autenticado* = *qualquer pessoa que se
cadastrou 10 segundos atrás*. Além disso:

- `EventController.create` **não usa `Authentication`**: o evento não guarda quem o criou.
  Não há autoria, não há auditoria, não há a quem responsabilizar.
- Sem chave natural, o catálogo aceita duplicatas e lixo ilimitado.
- É vetor de spam: eventos falsos servem de isca para anúncios falsos.

## Opções em aberto

| Opção | Efeito |
|---|---|
| Papel `ADMIN` | Catálogo curado, atrito alto para o vendedor |
| Qualquer um cria, com moderação | Escala melhor, exige fila de revisão |
| Só via fonte oficial (integração) | Catálogo confiável, dependência externa |

Nenhuma foi escolhida — ver [[Perguntas em Aberto]]. Enquanto isso, **não construa
funcionalidade que assuma catálogo confiável**.

## Testes esperados (quando a regra existir)

- usuário sem papel cria evento → 403
- criação registra o autor
- evento duplicado (mesmo nome + data + local) → 409

Aplicada em [[UC-05 Cadastrar evento]] · dívida nº 9 em [[Dívidas Técnicas]].
