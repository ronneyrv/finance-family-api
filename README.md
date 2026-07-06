# Finance Family API

API REST para gestão financeira pessoal e familiar, desenvolvida com Java e Spring Boot.

A aplicação permite gerenciar transações financeiras, cartões de crédito, compras parceladas, metas financeiras, categorias de despesas e indicadores consolidados em dashboard.

O projeto também possui uma infraestrutura de produção com imagens Docker imutáveis, configuração versionada com Docker Compose, validação automática de saúde da aplicação e rollback automático em caso de falha no deploy.

## Funcionalidades

- autenticação baseada em JWT
- organização dos dados financeiros por família (`Household`)
- gerenciamento de receitas e despesas
- categorias e subcategorias financeiras
- gerenciamento de cartões de crédito
- registro de compras parceladas
- acompanhamento de faturas e parcelas
- metas financeiras
- resumos financeiros mensais
- análise de despesas por categoria
- dashboard financeiro
- versionamento do banco de dados com Flyway
- documentação da API com OpenAPI e Swagger UI
- monitoramento da aplicação com Spring Boot Actuator

## Tecnologias

### Backend

- Java 21
- Spring Boot 3.5.3
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- Spring Boot Actuator
- JWT
- Lombok

### Banco de Dados

- PostgreSQL 17
- Flyway

### Documentação

- OpenAPI
- Swagger UI

### Infraestrutura e DevOps

- Docker
- Docker Compose
- GitHub Actions
- GitHub Container Registry
- Oracle Cloud Infrastructure
- Nginx
- Let's Encrypt
- Certbot
- Neon PostgreSQL

## Arquitetura da Aplicação

A aplicação utiliza uma arquitetura em camadas:

```text
Requisição HTTP
       ↓
   Controller
       ↓
    Service
       ↓
  Repository
       ↓
  PostgreSQL
```

A estrutura principal do backend está organizada da seguinte forma:

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

## Visão Geral do Domínio

O domínio principal da aplicação é composto pelas seguintes entidades:

- `User`
- `Household`
- `Transaction`
- `Category`
- `SubCategory`
- `CreditCard`
- `CreditCardInstallment`
- `Goal`

Os dados financeiros são associados ao contexto familiar (`Household`) do usuário autenticado.

Essa estrutura permite que usuários pertencentes à mesma família compartilhem a visão financeira consolidada, mantendo a organização dos dados dentro do mesmo contexto familiar.

## Executando o Projeto

### Pré-requisitos

Para executar o ambiente completo com Docker:

- Git
- Docker
- Docker Compose

Para executar a aplicação diretamente na máquina:

- Java 21
- PostgreSQL
- Git

O projeto utiliza o Gradle Wrapper, portanto não é necessário instalar o Gradle globalmente.

## Clonando o Repositório

```bash
git clone https://github.com/ronneyrv/finance-family-api
cd finance-family-api
```

## Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
DB_NAME=finance
DB_USERNAME=postgres
DB_PASSWORD=sua_senha

JWT_SECRET=sua_chave_jwt_segura
JWT_EXPIRATION=86400000
```

O arquivo `.env` contém informações sensíveis e não deve ser enviado para o repositório.

## Profiles da Aplicação

A aplicação utiliza profiles do Spring Boot para separar as configurações e os dados utilizados em cada ambiente.

| Profile | Ambiente | Banco de dados | Inicialização de dados |
|---|---|---|---|
| `dev` | desenvolvimento local | PostgreSQL | dados fictícios de desenvolvimento |
| `test` | testes automatizados | H2 em memória | fixtures próprias e isoladas |
| `prod` | produção | PostgreSQL | sem inicialização automática de usuários ou dados da aplicação |

### Desenvolvimento

O profile `dev` é utilizado no ambiente local.

Ao iniciar a aplicação com um banco vazio, os initializers de desenvolvimento criam dados fictícios para facilitar o desenvolvimento e os testes manuais da API.

Esses dados são exclusivos do ambiente de desenvolvimento e não são utilizados pelos testes automatizados nem pelo ambiente de produção.

O `docker-compose.yml` ativa automaticamente o profile:

```text
dev
```

### Testes

Os testes automatizados utilizam o profile `test` e um banco H2 em memória.

Os dados necessários para os testes são criados por fixtures próprias, independentes dos initializers de desenvolvimento. Essa separação mantém os testes reproduzíveis e evita dependência dos dados utilizados no ambiente local.
A suíte de testes pode ser executada com:

```bash
./gradlew test
```

### Produção

O ambiente de produção utiliza exclusivamente o profile `prod`.

Nesse ambiente, os initializers de desenvolvimento não são carregados e a aplicação não cria automaticamente usuários ou dados financeiros.

A estrutura do banco de dados é controlada pelas migrations versionadas do Flyway, enquanto os dados de produção permanecem independentes dos ambientes de desenvolvimento e teste.

## Executando com Docker Compose

O ambiente Docker de desenvolvimento utiliza o profile `dev` e inicia:

- PostgreSQL
- Finance Family API

Na primeira inicialização de um banco vazio, a aplicação cria dados fictícios
de desenvolvimento para facilitar testes manuais e a integração com clientes locais.

Para construir as imagens e iniciar os containers:

```bash
docker compose up --build
```

Para executar em segundo plano:

```bash
docker compose up --build -d
```

Para verificar os containers:

```bash
docker compose ps
```

Para encerrar o ambiente:

```bash
docker compose down
```

Para encerrar o ambiente e remover também o volume do PostgreSQL:

```bash
docker compose down -v
```

> Atenção: a opção `-v` remove permanentemente os dados do banco de dados local armazenados no volume Docker.

Após a inicialização, a API estará disponível em:

```text
http://localhost:8080
```

## Executando a API Localmente

Também é possível executar a aplicação diretamente pelo Gradle utilizando o profile `dev`.

Primeiro, configure as variáveis de ambiente:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/finance
export DB_USERNAME=postgres
export DB_PASSWORD=sua_senha
export JWT_SECRET=sua_chave_jwt_segura
export JWT_EXPIRATION=86400000
```

Em seguida, execute:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

O profile `dev`:

- conecta ao PostgreSQL por meio de variáveis de ambiente
- valida o schema do banco com Hibernate
- executa as migrations do Flyway
- habilita o Swagger UI
- expõe os endpoints `health` e `info` do Actuator
- habilita detalhes do health check para desenvolvimento

## Migrations do Banco de Dados

A evolução do schema do banco de dados é gerenciada pelo Flyway.

Os arquivos de migration estão localizados em:

```text
src/main/resources/db/migration
```

As migrations atuais são:

```text
V1  Criação de households
V2  Criação de usuários
V3  Criação de categorias
V4  Criação de subcategorias
V5  Criação de transações
V6  Criação de metas financeiras
V7  Criação de cartões de crédito
V8  Criação de parcelas de cartão
V9  Adição da data de pagamento das parcelas
```

O Flyway executa automaticamente as migrations pendentes durante a inicialização da aplicação.

O Hibernate utiliza:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Dessa forma, o Hibernate valida se o mapeamento das entidades corresponde ao schema existente, mas não cria ou altera automaticamente a estrutura do banco.

## Documentação da API

O Swagger UI está habilitado no ambiente de desenvolvimento.

Após iniciar a aplicação, acesse:

```text
http://localhost:8080/swagger-ui/index.html
```

A especificação OpenAPI está disponível em:

```text
http://localhost:8080/v3/api-docs
```

## Monitoramento da Aplicação

A aplicação utiliza Spring Boot Actuator para monitoramento de saúde.

No ambiente de desenvolvimento:

```text
http://localhost:8080/actuator/health
```

Em produção:

```text
https://finance-api.ronneyrocha.com.br/actuator/health
```

Uma aplicação saudável retorna o status:

```json
{
  "status": "UP"
}
```

O health check também é utilizado pelo processo de deploy para validar novas versões antes de considerá-las operacionais.

## Arquitetura de Produção

A API de produção é executada em uma máquina virtual na Oracle Cloud Infrastructure.

O tráfego público é recebido pelo Nginx, enquanto o container da aplicação permanece acessível apenas pela interface de loopback da VM.

```text
Internet
    ↓
finance-api.ronneyrocha.com.br
    ↓
DNS
    ↓
Oracle Cloud VM
    ↓
Nginx
    ├── HTTP :80
    │      ↓
    │   Redirect 301
    │      ↓
    └── HTTPS :443
           ↓
       Terminação TLS
           ↓
       127.0.0.1:8080
           ↓
       Docker Container
           ↓
       Spring Boot API
           ↓
       Neon PostgreSQL
```

A porta `8080` da aplicação não é acessível publicamente.

Todo o tráfego público destinado à API passa pelo Nginx utilizando HTTPS.

## Pipeline de CI/CD

O pipeline de integração e entrega contínua é executado pelo GitHub Actions.

O fluxo de deploy segue as seguintes etapas:

```text
Push / Merge na main
        ↓
Testes Automatizados
        ↓
Build da Aplicação
        ↓
Build da Imagem Docker
        ↓
Tag com Git SHA
        ↓
Push para o GHCR
        ↓
Conexão SSH com a VM
        ↓
Checkout do Commit Exato
        ↓
Validação do Compose Candidato
        ↓
Pull da Imagem Candidata
        ↓
Backup do Compose Atual
        ↓
Sincronização do Compose Versionado
        ↓
Deploy da Versão Candidata
        ↓
Health Check
```

As imagens Docker utilizam tags imutáveis baseadas no SHA do commit:

```text
sha-<commit-curto>
```

Exemplo:

```text
sha-c4db753
```

Isso permite associar cada versão executada em produção a uma revisão específica do código-fonte.

## Sincronização do Docker Compose em Produção

A configuração versionada de produção está localizada em:

```text
docker-compose.prod.yml
```

Durante o deploy, o pipeline:

1. faz checkout do commit exato associado à imagem candidata
2. valida a configuração candidata do Docker Compose
3. realiza o pull da imagem antes de alterar a configuração de runtime
4. cria um backup do Compose atualmente utilizado em produção
5. sincroniza a configuração versionada com o ambiente de runtime
6. realiza o deploy da imagem candidata
7. valida a saúde da aplicação

As credenciais e configurações sensíveis permanecem fora do repositório Git e são carregadas a partir do arquivo de ambiente armazenado diretamente na VM.

## Estratégia de Deploy

Cada deploy utiliza dois identificadores relacionados:

```text
Git Commit SHA
        +
Docker Image SHA Tag
```

O objetivo é garantir que o código, a imagem Docker e a configuração do Docker Compose pertençam à mesma versão do projeto.

Antes de modificar a configuração ativa, o processo valida o Compose candidato e realiza o pull da imagem correspondente.

Essa ordem evita alterar o estado de produção caso a imagem não esteja disponível ou a configuração candidata seja inválida.

## Rollback Automático

Cada versão candidata é validada através do endpoint de health check do Actuator.

Se a nova versão não atingir o estado `UP` dentro do período configurado, o script de deploy inicia automaticamente o rollback.

O processo:

1. identifica a falha da versão candidata
2. restaura a configuração anterior do Docker Compose
3. valida a configuração restaurada
4. recria o container utilizando a imagem imutável anterior
5. aguarda a recuperação da aplicação
6. valida novamente o health check

O rollback restaura conjuntamente:

```text
Imagem da Aplicação
+
Configuração do Docker Compose
```

Essa estratégia evita que uma imagem anterior seja executada com uma configuração mais recente e potencialmente incompatível.

O comportamento foi validado através de um cenário controlado de falha, confirmando a restauração automática da imagem anterior, da configuração anterior e da saúde da aplicação.

## Segurança

O ambiente de produção utiliza diferentes camadas de proteção:

- autenticação JWT para endpoints protegidos
- comunicação pública através de HTTPS
- Nginx como reverse proxy público
- certificados TLS emitidos pela Let's Encrypt
- renovação automática de certificados com Certbot
- aplicação vinculada apenas a `127.0.0.1:8080`
- ausência de acesso público direto à porta TCP `8080`
- secrets de produção mantidos fora do repositório
- imagens Docker de produção identificadas por tags SHA imutáveis
- validação de saúde após novos deploys
- rollback automático após deploys não saudáveis

## Endpoint de Produção

A API está disponível em:

```text
https://finance-api.ronneyrocha.com.br
```

Health check:

```text
https://finance-api.ronneyrocha.com.br/actuator/health
```

## Status do Projeto

A API backend e sua infraestrutura de produção estão operacionais, com evolução contínua de funcionalidades, testes e integrações.

Atualmente, o projeto possui recursos para autenticação, gerenciamento de transações, categorização financeira, cartões de crédito, compras parceladas, metas financeiras, dashboards, acesso seguro em produção e recuperação automática após falhas de deploy.
