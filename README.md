# Visão Geral do Projeto

Este projeto possui dois módulos principais:

- **Subscription**: Gerencia assinaturas pré-pagas, com renovação automática e controle de pagamentos.
- **Payment Simulator**: Simula um provedor externo de pagamentos via RabbitMQ.

## Regras de Negócio

- Renovação automática no dia do vencimento.
- Um usuário pode ter apenas uma assinatura ATIVA por vez.
- Cancelamento mantém acesso até o fim do ciclo.
- Upgrades e downgrades criam novas assinaturas com cobrança proporcional.
- Cron diário solicita pagamento 3 vezes (uma por dia). Se não houver confirmação após essas 3 solicitações, a assinatura é suspensa.

## Robustez e Concorrência

- @Transactional nos serviços garante atomicidade e rollback.
- Lock pessimista na assinatura ATIVA (SELECT FOR UPDATE) para evitar corridas em upgrade/renovação.
- Índices únicos parciais: 1 `ATIVA` e 1 `PRE_PROCESSADA` por usuário; unique em `payments.subscription_id`.
- Ordenação de escrita: `flush()` antes de criar nova ATIVA para respeitar o índice parcial.
- Publicação only-after-commit: mensagens enviadas após commit (TransactionSynchronization.afterCommit).
- Retry no RabbitMQ: cada solicitação publicada tem até 3 tentativas internas antes de ser roteada para a DLQ.
- Idempotência no consumo: ignora eventos se Payment já estiver SUCCESS.
- Global Exception Handler: respostas padronizadas (400/404/500) e validações.
- Flyway: migração versionada do schema (V1), evitando ddl-auto em produção.

## Decisões de Design

- Pré-pago por ser o cenário mais comum de assinaturas.
- Renovação cria novo ciclo para manter histórico claro.

## Testes e Desenvolvimento

- Crons agendados (scheduler) com endpoint para acionamento manual em testes.
- Endpoints para confirmar sucesso/falha de pagamento manualmente.
- Simulador pode desabilitar consumo para testar falhas.
- Endpoints administrativos expostos apenas para desenvolvimento.

## Configuração (.env)

Crie um arquivo `.env` na raiz (mesma pasta do `compose.yaml`) com as variáveis:

```
RABBITMQ_DEFAULT_USER=myuser
RABBITMQ_DEFAULT_PASS=secret
POSTGRES_DB=mydatabase
POSTGRES_USER=myuser
POSTGRES_PASSWORD=secret
```

Esses valores serão injetados nos serviços via Docker Compose.

## Primeira execução (setup)

Pré-requisitos: Docker, Docker Compose e JDK 17 (para build local com Gradle).

```
gradle --no-daemon wrapper --gradle-version 9.0 --distribution-type bin
chmod +x ./gradlew
./gradlew clean build -x test && docker compose up --build
```

## Como Rodar

No diretório raiz (execuções seguintes):

```
./gradlew clean build -x test && docker compose up --build
```

## URLs de Acesso

- Subscription API: http://localhost:8080
- Payment Simulator API: http://localhost:8081
- RabbitMQ Management UI: http://localhost:15672
- Postgres: localhost:5431
