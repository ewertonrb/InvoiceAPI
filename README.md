# Invoice API

Backend do Invoice Platform, construído com Spring Boot, Java 21, PostgreSQL, Flyway e JWT.

O serviço fornece autenticação, empresas, memberships, workers, worklogs, aprovações, invoices, shifts, notificações, imagens e geração de PDFs.

## Requisitos

- Java 21
- PostgreSQL 17 ou compatível
- Maven Wrapper incluído no repositório
- Docker e Docker Compose, opcionalmente

## Configuração local

Configure as variáveis no arquivo `.env.local` ou no ambiente do processo:

```env
SPRING_PROFILES_ACTIVE=local
DB_URL=jdbc:postgresql://localhost:5432/invoice_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=replace-with-a-long-random-secret
FRONTEND_BASE_URL=http://localhost:3000
EMAIL_NOTIFICATIONS_ENABLED=false
```

O arquivo `.env.local` é ignorado pelo Git e nunca deve conter valores de produção versionados.

## Execução com Maven

```bash
./mvnw spring-boot:run
```

A aplicação inicia na porta `8080` por padrão. As migrations do Flyway são executadas automaticamente na inicialização.

## Execução com Docker Compose

O `compose.yaml` local sobe PostgreSQL e a API:

```bash
docker compose up --build
```

Para parar os serviços:

```bash
docker compose down
```

O volume `invoice_postgres_data` mantém os dados do banco entre reinicializações.

## Profiles e produção

Use `SPRING_PROFILES_ACTIVE=prod` em produção. O profile produtivo exige configuração externa para:

```env
DB_URL=jdbc:postgresql://host:5432/invoice_db
DB_USERNAME=invoice_user
DB_PASSWORD=strong-database-password
JWT_SECRET=long-random-secret
FRONTEND_BASE_URL=https://app.example.com
EMAIL_NOTIFICATIONS_ENABLED=false
MAIL_HOST=
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_SMTP_AUTH=true
MAIL_STARTTLS_ENABLED=true
MAIL_FROM=
```

Em produção, `spring.jpa.hibernate.ddl-auto=validate`; alterações de schema devem ser feitas por migrations Flyway em `src/main/resources/db/migration/`.

Não exponha `DB_PASSWORD`, `JWT_SECRET`, credenciais SMTP ou arquivos `.env` no GitHub.

## Testes e build

```bash
./mvnw test
./mvnw clean package
```

O build gera o JAR em `target/`. Para construir a imagem Docker:

```bash
docker build -t invoice-api:latest .
```

## API

Principais grupos de endpoints:

- `/auth`: login, seleção de empresa, sessão atual e recuperação de password.
- `/users`: usuários e troca autenticada da própria password em `/users/me/password`.
- `/companies`: empresas e operações administrativas por empresa.
- `/work-logs`: criação, edição, aprovação, rejeição e reenvio de worklogs.
- `/invoices`: preview, drafts, emissão, pagamento, cancelamento e PDF.
- `/companies/{companyId}/shifts`: criação e gestão de shifts.
- `/companies/{companyId}/notifications`: notificações e leitura.
- `/platform/companies`: provisionamento global para `PLATFORM_ADMIN`.

As permissões são aplicadas por papel e por empresa. Os papéis de empresa são `OWNER`, `ADMIN`, `MANAGER`, `FINANCE` e `WORKER`.

## Repositório relacionado

- Frontend: [Invoice-Web](https://github.com/ewertonrb/Invoice-Web)
- Branch principal: `main`
- Branch de desenvolvimento: `develop`
- Release atual: `v1.0.0`

O frontend deve apontar `API_BASE_URL` para a instância correspondente desta API.
