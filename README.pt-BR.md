# Finance Family API

## Plataforma de Gestão Financeira Pessoal e Familiar

API REST backend de uma plataforma full-stack para gerenciamento financeiro pessoal e familiar.

O Finance Family foi desenvolvido como um projeto de portfólio orientado a práticas reais de engenharia de software, com foco em modelagem de domínio, segurança de APIs, testes automatizados, versionamento de banco de dados, CI/CD, conteinerização, observabilidade, segurança de deploy e recuperação de desastres.

> Este repositório contém o backend da plataforma Finance Family. O frontend é mantido separadamente em [`finance-family-web`](https://github.com/ronneyrv/finance-family-web).

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)](https://github.com/features/actions)

English: [`README.md`](README.md)

---

## Links do Projeto

- [Repositório do Frontend](https://github.com/ronneyrv/finance-family-web)
- [Arquitetura](#arquitetura)
- [Testes](#testes)
- [Infraestrutura de Produção](#infraestrutura-de-produção)
- [Migrations do Banco de Dados](#migrations-do-banco-de-dados)

---

## Visão Geral

O Finance Family permite gerenciar finanças pessoais e familiares por meio de uma API REST orientada ao domínio financeiro.

A plataforma contempla transações financeiras, contas financeiras, categorias, cartões de crédito, compras parceladas, faturas, transações recorrentes, transferências internas e indicadores financeiros consolidados.

O backend é estruturado com uma separação clara entre API, serviços de aplicação, entidades de domínio, persistência, segurança e infraestrutura.

---

## Principais Funcionalidades

### Gestão Financeira

- Gerenciamento de receitas e despesas
- Gerenciamento de contas financeiras
- Cálculo do saldo atual das contas
- Categorias e subcategorias financeiras
- Transferências internas entre contas financeiras
- Transações recorrentes

### Categorias

- Categorias padrão do sistema
- Categorias pessoais de despesas
- Subcategorias pessoais
- Gerenciamento de categorias e subcategorias de compras

### Cartões de Crédito

- Gerenciamento de cartões de crédito
- Registro de compras
- Compras parceladas
- Parcelas de cartão de crédito
- Geração e acompanhamento de faturas
- Pagamento de faturas
- Pagamento de faturas vinculado a contas financeiras
- Separação entre o evento econômico da compra e a movimentação financeira do pagamento da fatura
- Associação de categorias e subcategorias às compras

### Finanças Familiares

- Organização de usuários e famílias
- Perfil do usuário e avatar
- Consolidação financeira por família
- Visões individuais e consolidadas
- Controle de propriedade e isolamento dos dados

### Indicadores Financeiros

- Resumos financeiros mensais
- Análise de despesas por categoria
- Projeções mensais
- Fluxo de caixa
- Resultados financeiros acumulados
- Tendências de gastos com cartões de crédito
- Análise de comprometimento da renda
- Indicadores de saúde financeira

### Plataforma

- Autenticação baseada em JWT
- Suporte a refresh tokens
- Validação de requisições
- Tratamento centralizado de exceções
- Versionamento do banco de dados com Flyway
- Documentação OpenAPI
- Spring Boot Actuator
- Ambiente de desenvolvimento conteinerizado
- Testes de integração automatizados
- Health checks em produção
- Deploys com imagens Docker imutáveis
- Rollback automático de deploy
- Backup e restauração do PostgreSQL

---

## Destaques de Engenharia

O Finance Family foi desenvolvido de forma incremental, utilizando um fluxo orientado a funcionalidades e práticas de engenharia próximas de um ambiente de produção.

### Modelagem de Domínio

O domínio financeiro possui relacionamentos entre:

```text
Household
   │
   ├── Users
   │
   ├── Financial Accounts
   │       └── Transactions
   │
   ├── Credit Cards
   │       ├── Purchases
   │       ├── Installments
   │       └── Invoices
   │
   ├── Categories
   │       └── Subcategories
   │
   └── Recurring Transactions
```

O modelo diferencia explicitamente eventos econômicos de movimentações de liquidação financeira.

Por exemplo, o pagamento de uma fatura de cartão reduz o saldo da conta financeira selecionada sem registrar a compra original novamente como uma segunda despesa.

### Segurança

A API utiliza Spring Security e autenticação baseada em JWT.

As responsabilidades relacionadas à segurança incluem:

- autenticação
- autorização
- hash de senhas
- validação de JWT
- gerenciamento de refresh tokens
- endpoints protegidos
- controle de propriedade dos recursos
- isolamento dos dados por família
- gerenciamento de secrets em produção

### Integridade do Banco de Dados

A estrutura do banco é controlada por migrations do Flyway.

O Hibernate executa com:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Isso significa que o Hibernate valida o mapeamento das entidades em relação ao schema existente sem modificar automaticamente a estrutura do banco.

A mesma estratégia de migrations também é utilizada nos testes de integração.

### Testes Automatizados

Os testes de integração utilizam:

- JUnit
- Spring Boot Test
- PostgreSQL
- Testcontainers
- Flyway

O ambiente de testes provisiona um PostgreSQL real por meio do Testcontainers e aplica a mesma estratégia de migrations utilizada pela aplicação.

Isso evita depender de um banco em memória cujo comportamento poderia ser diferente do PostgreSQL utilizado em produção.

### CI/CD

O projeto utiliza GitHub Actions para automatizar validações e deploys.

O fluxo geral de produção segue:

```text
Alteração no Código
        ↓
Testes Automatizados
        ↓
Build da Aplicação
        ↓
Build da Imagem Docker
        ↓
Imagem Imutável baseada no SHA
        ↓
Container Registry
        ↓
Deploy em Produção
        ↓
Health Check
```

As imagens de produção são associadas a commits específicos do Git por meio de tags baseadas em SHA.

### Segurança de Deploy

Os deploys de produção validam a nova versão antes de considerá-la saudável.

Caso a nova aplicação falhe no health check, o processo de deploy pode restaurar automaticamente a versão anterior da aplicação e a configuração anterior do Docker Compose.

Isso proporciona um mecanismo controlado de rollback, evitando deixar o ambiente de produção executando uma versão não saudável.

### Observabilidade

O Spring Boot Actuator fornece informações sobre a saúde da aplicação.

Os health checks também são utilizados como parte do processo de deploy para determinar se uma nova versão está operacional.

---

## Arquitetura

O backend segue uma arquitetura em camadas:

```text
Requisição HTTP
      │
      ▼
Controller
      │
      ▼
Service
      │
      ▼
Repository
      │
      ▼
PostgreSQL
```

A estrutura principal da aplicação está organizada em:

```text
src/main/java/com/ronney/finance
├── config
├── controller
├── domain
│   ├── entity
│   └── enums
├── dto
│   ├── request
│   └── response
├── exception
├── repository
├── security
└── service
    └── impl
```

---

## Stack Tecnológica

### Backend

- Java 21
- Spring Boot 3.5.3
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- Bean Validation
- Spring Boot Actuator
- JWT
- Lombok
- Gradle

### Banco de Dados

- PostgreSQL 17
- Flyway
- Testcontainers

### Documentação da API

- OpenAPI
- Swagger UI

### Infraestrutura

- Docker
- Docker Compose
- GitHub Actions
- GitHub Container Registry
- Oracle Cloud Infrastructure
- Nginx
- Let's Encrypt
- Certbot

---

## Modelo de Domínio

As principais entidades do domínio incluem:

```text
User
Household
Transaction
FinancialAccount
Category
SubCategory
CreditCard
Purchase
CreditCardInstallment
RecurringTransaction
```

Os usuários pertencem a um `Household`, permitindo que a plataforma forneça tanto visões financeiras individuais quanto consolidadas.

As transações financeiras são associadas a contas financeiras e categorizadas de acordo com o domínio financeiro da aplicação.

As compras de cartão de crédito são modeladas separadamente de suas parcelas e faturas, permitindo representar o ciclo de vida de uma despesa realizada no cartão.

---

## Profiles de Ambiente

A aplicação separa o comportamento específico de cada ambiente por meio de profiles do Spring Boot.

| Profile | Finalidade | Banco de Dados | Dados de Desenvolvimento |
|---|---|---|---|
| `dev` | Desenvolvimento local | PostgreSQL | Habilitados |
| `test` | Testes automatizados | PostgreSQL Testcontainers | Fixtures de teste |
| `prod` | Produção | PostgreSQL | Desabilitados |

### Desenvolvimento

O profile `dev` fornece um ambiente local conveniente com PostgreSQL e dados de desenvolvimento.

### Testes

O profile `test` utiliza PostgreSQL Testcontainers e fixtures isoladas.

As migrations do Flyway são aplicadas antes dos testes de integração, mantendo o schema do banco alinhado ao histórico de migrations da aplicação.

### Produção

O profile `prod` desabilita a inicialização de dados de desenvolvimento e mantém ferramentas internas de desenvolvimento indisponíveis.

Swagger/OpenAPI e informações detalhadas de health não são expostos no ambiente de produção.

---

## Desenvolvimento Local

### Requisitos

- Git
- Docker
- Docker Compose
- Java 21

O projeto utiliza o Gradle Wrapper, portanto não é necessário instalar o Gradle globalmente.

### Variáveis de Ambiente

Crie um arquivo `.env` local a partir do exemplo fornecido:

```bash
cp .env.example .env
```

Configure os valores necessários para:

```text
DB_NAME
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
CORS_ALLOWED_ORIGINS
```

Nunca faça commit de secrets locais ou credenciais de produção.

### Executar com Docker Compose

Inicie o ambiente completo de desenvolvimento:

```bash
docker compose up --build
```

Ou execute em segundo plano:

```bash
docker compose up --build -d
```

Verifique o status dos containers:

```bash
docker compose ps
```

Acompanhe os logs da API:

```bash
docker compose logs -f finance-api
```

Pare o ambiente:

```bash
docker compose down
```

Para remover também o volume local do PostgreSQL:

```bash
docker compose down -v
```

> A opção `-v` remove permanentemente o volume local do banco de dados.

### Executar PostgreSQL em Docker e a API com Gradle

Inicie o PostgreSQL:

```bash
docker compose up -d postgres
```

Carregue as variáveis de ambiente:

```bash
set -a
source .env
set +a
```

Execute a API:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Esse fluxo permite reiniciar a API sem reconstruir sua imagem Docker a cada alteração no código.

---

## Validação da API

Com a aplicação em execução, valide o endpoint de health:

```bash
curl http://localhost:8080/actuator/health
```

Uma aplicação saudável deve retornar:

```json
{
  "status": "UP"
}
```

No ambiente de desenvolvimento, a documentação interativa da API está disponível por meio do Swagger UI.

A especificação OpenAPI também é disponibilizada pelo ambiente de desenvolvimento.

---

## Testes

Execute a suíte de testes:

```bash
./gradlew test
```

Execute a suíte completa a partir de um estado limpo:

```bash
./gradlew clean test
```

Compile a aplicação sem executar os testes:

```bash
./gradlew compileJava
```

O ambiente de testes de integração utiliza PostgreSQL Testcontainers e Flyway para validar a aplicação em um ambiente de banco de dados próximo ao utilizado em produção.

---

## Migrations do Banco de Dados

A evolução do schema do banco é gerenciada pelo Flyway.

Os arquivos de migration estão localizados em:

```text
src/main/resources/db/migration
```

O histórico atual de migrations inclui:

| Migration | Finalidade |
|---|---|
| `V1` | Criação de households |
| `V2` | Criação de usuários |
| `V3` | Criação de categorias |
| `V4` | Criação de subcategorias |
| `V5` | Criação de contas financeiras |
| `V6` | Criação de transações |
| `V7` | Criação da tabela de metas |
| `V8` | Criação de cartões de crédito |
| `V9` | Criação de compras |
| `V10` | Criação de parcelas de cartão de crédito |
| `V11` | Criação de transações recorrentes |
| `V12` | Adição do tipo de transação |
| `V13` | Criação de refresh tokens |
| `V14` | Adição de colunas de avatar aos usuários |
| `V15` | Carga de categorias padrão |
| `V16` | Remoção da tabela de metas |
| `V17` | Adição de categorias e subcategorias pessoais |
| `V18` | Adição de categoria e subcategoria às compras |
| `V19` | Adição de suporte a transferências internas |

As metas financeiras não fazem mais parte do domínio atual da aplicação. A migration histórica `V7` permanece no histórico do Flyway porque criou a tabela, enquanto a `V16` remove explicitamente essa tabela.

O Flyway aplica as migrations pendentes durante a inicialização da aplicação.

O Hibernate valida o schema resultante em vez de modificá-lo automaticamente.

---

## Infraestrutura de Produção

O ambiente de produção executa a API em uma infraestrutura conteinerizada.

A arquitetura geral é:

```text
Internet
    │
    ▼
DNS
    │
    ▼
Oracle Cloud VM
    │
    ▼
Nginx
    │
    ├── HTTP → redirecionamento HTTPS
    │
    └── HTTPS / TLS
           │
           ▼
     Spring Boot API
           │
           ▼
      PostgreSQL
```

A aplicação Spring Boot não fica diretamente exposta à internet pública.

O Nginx atua como reverse proxy público e realiza a terminação HTTPS.

---

## Deploy em Produção

O fluxo de deploy é baseado em:

```text
Commit do Git
      +
SHA da Imagem Docker
      +
Docker Compose Versionado
```

O processo de deploy valida:

1. a configuração candidata do Docker Compose;
2. a imagem Docker candidata;
3. a inicialização da aplicação;
4. o health endpoint da aplicação.

Somente após a aprovação do health check o deploy é considerado saudável.

---

## Rollback Automático

Caso uma nova versão falhe no health check, o processo de deploy pode restaurar a versão anterior.

O rollback restaura:

```text
Imagem Docker Anterior
        +
Configuração Anterior do Docker Compose
```

A aplicação é então reiniciada e validada novamente por meio do health endpoint.

Essa estratégia reduz o risco de deixar a produção executando uma versão não saudável ou parcialmente implantada.

---

## Backup e Disaster Recovery

Os dados do PostgreSQL em produção são protegidos por um processo automatizado de backup e restauração.

O fluxo de backup é:

```text
Cron
  ↓
Script de Backup
  ↓
Container PostgreSQL
  ↓
pg_dump
  ↓
gzip
  ↓
Armazenamento de Backup
```

Os backups são validados após sua criação e seguem uma política de retenção.

O processo de restauração valida o backup antes de reconstruir o banco de dados e iniciar novamente a aplicação.

O fluxo de recuperação inclui:

```text
Backup
  ↓
Validação
  ↓
Parada da Aplicação
  ↓
Recriação do Banco
  ↓
Restore
  ↓
Validação dos Dados
  ↓
Inicialização da Aplicação
  ↓
Health Check
```

A infraestrutura atual não define um RTO numérico formal e não utiliza atualmente armazenamento externo de backups nem PostgreSQL Point-in-Time Recovery.

---

## Fluxo de Desenvolvimento

O desenvolvimento segue um fluxo Git orientado a funcionalidades:

```text
Issue
  ↓
Feature Branch
  ↓
Implementação
  ↓
Testes
  ↓
Formatação / Lint
  ↓
Validação do Build
  ↓
Conventional Commit
  ↓
Pull Request
  ↓
Review
  ↓
Merge
```

Esse fluxo é utilizado ao longo do histórico de desenvolvimento e manutenção do projeto.

---

## Evolução do Projeto

O Finance Family evoluiu de forma incremental, partindo de uma fundação inicial de backend até se tornar uma plataforma full-stack orientada a práticas de engenharia próximas de um ambiente de produção.

Entre os principais marcos de engenharia estão:

- fundação do backend e banco de dados;
- autenticação e autorização;
- implementação do domínio de transações financeiras;
- gerenciamento de contas financeiras;
- gerenciamento de cartões e compras parceladas;
- ciclo de vida de faturas e pagamentos;
- transações recorrentes;
- indicadores financeiros e dashboard;
- automação de CI/CD;
- deploys com imagens Docker imutáveis;
- rollback automático;
- integração de PostgreSQL Testcontainers;
- análise de saúde financeira;
- transferências internas entre contas;
- processos de backup e disaster recovery em produção.

O projeto continua sendo refinado por meio de feature branches, pull requests, testes e melhorias incrementais.

---

## Repositório Relacionado

### Finance Family Web

O frontend é mantido separadamente:

[Finance Family Web](https://github.com/ronneyrv/finance-family-web)

A aplicação web é construída com React, TypeScript, Vite, Tailwind CSS, Axios, Recharts, Vitest e React Testing Library.

---

## Status do Projeto

O Finance Family é um projeto de portfólio em evolução contínua, focado em demonstrar desenvolvimento full-stack e práticas de engenharia de software orientadas a ambientes de produção.

A aplicação busca demonstrar não apenas implementação de funcionalidades, mas também:

- arquitetura;
- modelagem de domínio;
- segurança;
- testes;
- gerenciamento de banco de dados;
- CI/CD;
- infraestrutura;
- segurança de deploy;
- observabilidade;
- confiabilidade operacional.

---

## Autor

**Ronney Rocha**

Full Stack Developer com foco em Java, Spring Boot, React, TypeScript e práticas de engenharia de software.

---

## Licença

Este projeto é mantido como um projeto pessoal de portfólio.