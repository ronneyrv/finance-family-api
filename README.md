# Finance Family API

API REST para gestão financeira pessoal e familiar, desenvolvida com Java e Spring Boot.

A aplicação permite gerenciar transações financeiras, contas financeiras, cartões de crédito, compras parceladas, pagamentos de faturas, transações recorrentes, metas financeiras, categorias de despesas e indicadores consolidados em dashboard.

O projeto também possui uma infraestrutura de produção com imagens Docker imutáveis, configuração versionada com Docker Compose, validação automática de saúde da aplicação e rollback automático em caso de falha no deploy.

## Funcionalidades

- autenticação baseada em JWT
- organização dos dados financeiros por família (`Household`)
- gerenciamento de receitas e despesas
- gerenciamento de contas financeiras
- cálculo de saldo atual das contas
- categorias e subcategorias financeiras
- gerenciamento de cartões de crédito
- registro de compras parceladas
- acompanhamento de faturas e parcelas
- pagamento de faturas vinculado a contas financeiras
- separação entre despesas econômicas e movimentações de pagamento de cartão
- transações recorrentes
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
- Gradle

### Banco de Dados

- PostgreSQL 17
- PostgreSQL Testcontainers para testes automatizados
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
- `FinancialAccount`
- `Category`
- `SubCategory`
- `CreditCard`
- `Purchase`
- `CreditCardInstallment`
- `RecurringTransaction`

As transações financeiras pertencem a usuários e são vinculadas a contas financeiras.

Os usuários pertencem a um `Household`, permitindo a construção de visões financeiras individuais e consolidadas no contexto familiar.

O pagamento de uma fatura de cartão de crédito gera uma movimentação financeira específica, permitindo reduzir o saldo da conta utilizada no pagamento sem contabilizar novamente a compra como uma nova despesa econômica.

## Ambiente de Desenvolvimento

O projeto pode ser executado localmente de duas formas:

1. ambiente completo com Docker Compose, executando PostgreSQL e API em containers;
2. PostgreSQL executado em container e API executada diretamente na máquina com Gradle.

A segunda opção é útil durante o desenvolvimento do backend, pois permite executar e reiniciar a aplicação sem reconstruir a imagem Docker a cada alteração.

### Pré-requisitos

Para executar o ambiente completo com Docker Compose:

- Git
- Docker
- Docker Compose

Para executar a API diretamente na máquina utilizando o PostgreSQL em container:

- Git
- Docker
- Docker Compose
- Java 21

O projeto utiliza o Gradle Wrapper, portanto não é necessário instalar o Gradle globalmente.

## Clonando o Repositório

```bash
git clone https://github.com/ronneyrv/finance-family-api
cd finance-family-api
```

## Variáveis de Ambiente

O projeto utiliza variáveis de ambiente para configurar o banco de dados, autenticação JWT e outras configurações dependentes do ambiente.

Crie o arquivo `.env` na raiz do projeto a partir do arquivo de exemplo:

```bash
cp .env.example .env
```

O arquivo `.env.example` contém a estrutura esperada:

```env
DB_NAME=finance
DB_URL=jdbc:postgresql://localhost:5432/finance
DB_USERNAME=finance
DB_PASSWORD=finance

JWT_SECRET=replace-with-a-secret-of-at-least-32-bytes
JWT_EXPIRATION=86400000

CORS_ALLOWED_ORIGINS=https://your-frontend-domain.example
```

Antes de iniciar a aplicação, substitua os valores de exemplo conforme necessário.

O arquivo `.env` contém configurações locais e informações sensíveis e não deve ser enviado para o repositório.

### Uso das Variáveis no Ambiente Local

No Docker Compose de desenvolvimento:

- `DB_NAME` define o nome do banco criado pelo PostgreSQL;
- `DB_USERNAME` define o usuário do banco;
- `DB_PASSWORD` define a senha do banco;
- `JWT_SECRET` define a chave utilizada para assinatura dos tokens JWT;
- `JWT_EXPIRATION` define o tempo de expiração dos tokens.

O container da API recebe uma URL JDBC construída pelo próprio `docker-compose.yml` utilizando o hostname interno do serviço PostgreSQL:

```text
jdbc:postgresql://postgres:5432/${DB_NAME}
```

Quando a API é executada diretamente na máquina com o profile `dev`, a conexão utiliza:

```text
jdbc:postgresql://localhost:5432/${DB_NAME}
```

A variável `DB_URL` não é utilizada pelo profile `dev`. Ela é mantida para configurações de ambientes que recebem a URL completa de conexão, como o profile de produção.

## Profiles da Aplicação

A aplicação utiliza profiles do Spring Boot para separar as configurações e os dados de cada ambiente.

| Profile | Ambiente | Banco de dados | Inicialização de dados |
|---|---|---|---|
| `dev` | desenvolvimento local | PostgreSQL | dados fictícios de desenvolvimento |
| `test` | testes automatizados | PostgreSQL Testcontainers | fixtures próprias e isoladas |
| `prod` | produção | PostgreSQL | sem inicialização automática de dados de desenvolvimento |

### Profile `dev`

O profile `dev` é utilizado no ambiente local.

Ao iniciar a aplicação com um banco vazio, os initializers de desenvolvimento criam dados fictícios para facilitar o desenvolvimento, os testes manuais da API e a integração com clientes locais.

Esses dados são exclusivos do ambiente de desenvolvimento e não são utilizados pelos testes automatizados nem pelo ambiente de produção.

O `docker-compose.yml` ativa automaticamente o profile por meio da variável:

```text
SPRING_PROFILES_ACTIVE=dev
```

Nesse profile:

- a aplicação conecta ao PostgreSQL local;
- o Flyway executa as migrations pendentes;
- o Hibernate valida o schema existente;
- o Swagger UI permanece habilitado;
- os endpoints `health` e `info` do Actuator ficam expostos;
- o health check apresenta detalhes úteis para desenvolvimento.

### Profile `test`

Os testes automatizados utilizam o profile `test` e um banco PostgreSQL provisionado automaticamente pelo Testcontainers.

A infraestrutura de testes utiliza o mesmo mecanismo de migrations do ambiente de produção, executando o Flyway antes da inicialização dos testes.

Os dados necessários para os testes são criados por fixtures próprias, independentes dos initializers de desenvolvimento. Essa separação mantém os testes reproduzíveis e evita dependência dos dados utilizados no ambiente local.

A suíte de testes pode ser executada com:

```bash
./gradlew test
```

Para executar a suíte a partir de um estado limpo:

```bash
./gradlew clean test
```

### Profile `prod`

O ambiente de produção utiliza exclusivamente o profile `prod`.

Nesse ambiente:

- os initializers de desenvolvimento não são carregados;
- a aplicação não cria automaticamente usuários ou dados financeiros;
- o Hibernate valida o schema com `ddl-auto=validate`;
- o Flyway executa as migrations pendentes;
- Swagger UI e OpenAPI ficam desabilitados;
- detalhes internos do health check não são expostos.

A estrutura do banco de dados é controlada pelas migrations versionadas do Flyway, enquanto os dados de produção permanecem independentes dos ambientes de desenvolvimento e teste.

## Executando o Ambiente Local

### Opção 1 — API e PostgreSQL com Docker Compose

O ambiente Docker de desenvolvimento utiliza o profile `dev` e inicia:

- PostgreSQL;
- Finance Family API.

Na primeira inicialização de um banco vazio, a aplicação executa as migrations do Flyway e carrega os dados fictícios configurados para o ambiente de desenvolvimento.

Para construir a imagem da API e iniciar os containers:

```bash
docker compose up --build
```

Para executar em segundo plano:

```bash
docker compose up --build -d
```

Para verificar o estado dos containers:

```bash
docker compose ps
```

Para acompanhar os logs da API:

```bash
docker compose logs -f finance-api
```

Para acompanhar os logs do PostgreSQL:

```bash
docker compose logs -f postgres
```

Após a inicialização, a API estará disponível em:

```text
http://localhost:8080
```

Para encerrar o ambiente:

```bash
docker compose down
```

Esse comando remove os containers e a rede criada pelo Compose, mas preserva o volume com os dados do PostgreSQL.

Para encerrar o ambiente e remover também o volume do banco de dados:

```bash
docker compose down -v
```

> Atenção: a opção `-v` remove os dados armazenados no volume local do PostgreSQL. Na próxima inicialização, o banco será criado novamente, as migrations do Flyway serão executadas desde a primeira versão e os dados fictícios do profile `dev` serão recriados.

### Opção 2 — PostgreSQL com Docker e API com Gradle

Durante o desenvolvimento do backend, é possível executar apenas o PostgreSQL no Docker e iniciar a API diretamente na máquina.

Primeiro, inicie o banco de dados:

```bash
docker compose up -d postgres
```

Verifique se o container está saudável:

```bash
docker compose ps
```

Como o arquivo `.env` não é carregado automaticamente pelo processo Java iniciado no terminal, carregue as variáveis na sessão atual:

```bash
set -a
source .env
set +a
```

Em seguida, execute a aplicação:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Nesse fluxo:

```text
PostgreSQL
    ↓
Docker Container
    ↓
localhost:5432
    ↓
Spring Boot API
    ↓
Gradle / JVM local
    ↓
localhost:8080
```

O profile `dev` utiliza a seguinte conexão:

```text
jdbc:postgresql://localhost:5432/${DB_NAME}
```

Por isso, a execução local da API utiliza `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD`. Não é necessário exportar `DB_URL` para esse fluxo.

Para interromper a API executada pelo Gradle, utilize `Ctrl+C`.

Para interromper o PostgreSQL:

```bash
docker compose stop postgres
```

Para remover o container e a rede do ambiente local, preservando o volume do banco:

```bash
docker compose down
```

### Validando a Aplicação

Com a API em execução, valide o health check:

```bash
curl http://localhost:8080/actuator/health
```

Uma aplicação saudável deve responder com status `UP`.

A documentação interativa da API pode ser acessada em:

```text
http://localhost:8080/swagger-ui/index.html
```

A especificação OpenAPI está disponível em:

```text
http://localhost:8080/v3/api-docs
```

Para validar a compilação do projeto sem executar os testes:

```bash
./gradlew compileJava
```

Para executar os testes:

```bash
./gradlew test
```

Para executar a suíte completa a partir de um estado limpo:

```bash
./gradlew clean test
```

### Docker Engine 29+

Em alguns ambientes com Docker Engine 29+, o Testcontainers pode exigir a criação do arquivo local:

```text
~/.docker-java.properties
```

Com o conteúdo:

```properties
api.version=1.44
```

> Esse arquivo faz parte apenas da configuração local do ambiente de desenvolvimento e **não deve ser versionado**.

## Migrations do Banco de Dados

A evolução do schema do banco de dados é gerenciada pelo Flyway.

Os arquivos de migration estão localizados em:

```text
src/main/resources/db/migration
```

As migrations atuais são:

| Migration | Descrição |
|---|---|
| `V1` | criação de households |
| `V2` | criação de usuários |
| `V3` | criação de categorias |
| `V4` | criação de subcategorias |
| `V5` | criação de contas financeiras |
| `V6` | criação de transações |
| `V7` | criação de metas financeiras |
| `V8` | criação de cartões de crédito |
| `V9` | criação de compras |
| `V10` | criação de parcelas de cartão de crédito |
| `V11` | criação de transações recorrentes |
| `V12` | adição do tipo de transação e suporte a pagamentos de fatura |
| `V13` | criação da tabela de refresh tokens |

O Flyway executa automaticamente as migrations pendentes durante a inicialização da aplicação.

A mesma estratégia também é utilizada durante a execução dos testes de integração, garantindo que o schema validado seja equivalente ao ambiente de produção.

O Hibernate utiliza:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Dessa forma, o Hibernate valida se o mapeamento das entidades corresponde ao schema existente, mas não cria nem altera automaticamente a estrutura do banco.

### Recriando o Banco Local

Como o ambiente de desenvolvimento utiliza dados fictícios, o banco local pode ser recriado quando necessário.

Para remover os containers e o volume do PostgreSQL:

```bash
docker compose down -v
```

Depois, inicie novamente o ambiente:

```bash
docker compose up --build -d
```

Nesse processo:

1. um novo volume do PostgreSQL é criado;
2. o banco configurado em `DB_NAME` é criado;
3. o Flyway executa todas as migrations em ordem;
4. o Hibernate valida o schema resultante;
5. os initializers do profile `dev` carregam os dados fictícios de desenvolvimento.

> Esse procedimento deve ser utilizado apenas no ambiente local. A remoção do volume apaga permanentemente os dados armazenados no PostgreSQL de desenvolvimento.

### Verificando os Logs das Migrations

Para acompanhar a inicialização da API e a execução das migrations:

```bash
docker compose logs -f finance-api
```

Quando a API é executada diretamente com Gradle, as informações do Flyway aparecem no próprio terminal:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
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

## Backup e Disaster Recovery

A infraestrutura de produção possui um fluxo de backup e recuperação do PostgreSQL baseado em `pg_dump`, compressão com gzip, validação de integridade e política de retenção.

Os backups são executados diretamente sobre o container `finance-postgres`, enquanto a aplicação permanece disponível durante a execução do backup.

### Arquitetura de Backup

O fluxo de backup utiliza os seguintes componentes:

```text
Cron
  ↓
/opt/finance-api/app/scripts/backup.sh
  ↓
Docker Container
finance-postgres
  ↓
pg_dump
  ↓
gzip
  ↓
/opt/finance-api/backups/
  ↓
Backup .sql.gz
```

### Agendamento

O backup automático é executado diariamente às 05:00 UTC, correspondendo a 02:00 BRT.

A configuração atual é:

```text
0 5 * * * /opt/finance-api/app/scripts/backup.sh >> /var/log/finance-backup.log 2>&1
```

A VM de produção utiliza UTC como timezone. O horário correspondente em Brasília é 02:00 BRT.

O resultado da execução automática é registrado em:

```text
/var/log/finance-backup.log
```

O agendamento é instalado no crontab do usuário `root`.

Para verificar o agendamento atualmente instalado:

```bash
sudo crontab -l
```

Para verificar o estado do serviço cron:

```bash
systemctl status cron --no-pager
```

### Diretório de Backups

Os arquivos de backup são armazenados em:

```text
/opt/finance-api/backups
```

O nome dos arquivos segue o padrão:

```text
finance-YYYY-MM-DD_HH-MM-SS.sql.gz
```

Exemplo:

```text
finance-2026-08-11_21-52-27.sql.gz
```

A política de retenção mantém os 7 backups mais recentes.

### Executando um Backup Manual

Para executar um backup manualmente na VM de produção:

```bash
sudo /opt/finance-api/app/scripts/backup.sh
```

O script utiliza as configurações armazenadas em:

```text
/opt/finance-api/compose/.env
```

O script verifica previamente se o arquivo `.env` e o container `finance-postgres` estão disponíveis.

### Validação e Retenção

Após executar o `pg_dump`, o backup é compactado utilizando gzip.

O script valida a integridade do arquivo através de:

```bash
gzip -t /opt/finance-api/backups/finance-YYYY-MM-DD_HH-MM-SS.sql.gz
```

Caso a validação falhe, o arquivo inválido é removido e o processo termina com erro.

A política de retenção mantém os 7 backups mais recentes.

Backups mais antigos são removidos automaticamente após a criação e validação do novo backup.

Para verificar os backups disponíveis:

```bash
ls -lh /opt/finance-api/backups/
```

### Procedimento de Restore

A recuperação do banco de dados utiliza:

```text
scripts/restore.sh
```

O script recebe como argumento o caminho do backup que deverá ser restaurado.

Exemplo:

```bash
sudo /opt/finance-api/app/scripts/restore.sh \
  /opt/finance-api/backups/finance-YYYY-MM-DD_HH-MM-SS.sql.gz
```

O restore é um procedimento destrutivo e deve ser executado somente quando a recuperação do banco de dados for necessária.

### Pré-requisitos do Restore

Antes de iniciar o procedimento:

1. confirmar que o backup escolhido existe;
2. validar a integridade do arquivo;
3. confirmar que o arquivo de ambiente de produção está disponível;
4. confirmar que o container `finance-postgres` está disponível;
5. confirmar que existe uma janela operacional adequada para interromper temporariamente a API.

Para validar o backup antes do restore:

```bash
gzip -t /opt/finance-api/backups/finance-YYYY-MM-DD_HH-MM-SS.sql.gz
```

### Fluxo de Recuperação

O `restore.sh` executa o seguinte fluxo:

```text
Backup selecionado
        ↓
Validação dos pré-requisitos
        ↓
Parada da Finance API
        ↓
Encerramento das conexões PostgreSQL
        ↓
Remoção do banco existente
        ↓
Criação de um novo banco
        ↓
Descompressão do backup
        ↓
Restauração via psql
        ↓
Validação das tabelas restauradas
        ↓
Inicialização da Finance API
        ↓
Health Check
        ↓
Sistema recuperado
```

Durante o restore, a Finance API permanece parada para evitar novas conexões ao banco enquanto o estado do PostgreSQL é reconstruído.

As conexões ativas com o banco de produção são encerradas antes da recriação do banco.

O banco existente é então removido e recriado.

O conteúdo do backup é descompactado e enviado ao PostgreSQL através do `psql`.

Após a restauração, o script verifica as tabelas existentes no banco restaurado.

Somente depois de uma restauração bem-sucedida a Finance API é iniciada novamente.

O processo aguarda o endpoint:

```text
http://127.0.0.1:8080/actuator/health
```

até que a aplicação retorne:

```json
{
  "status": "UP"
}
```

Se a restauração falhar, a Finance API permanece parada.

### Checklist de Disaster Recovery

Em caso de perda ou corrupção dos dados de produção:

1. identificar a necessidade de recuperação;
2. identificar o backup apropriado;
3. validar a integridade do arquivo;
4. confirmar o ambiente de produção;
5. executar o procedimento de restore;
6. acompanhar os logs do processo;
7. confirmar a existência das tabelas restauradas;
8. confirmar que a Finance API iniciou corretamente;
9. validar o health check;
10. validar a autenticação da aplicação;
11. validar operações críticas da aplicação;
12. confirmar a integridade funcional dos dados recuperados.

O processo de recuperação depende do tamanho do backup e do volume de dados restaurado. O projeto não define atualmente um RTO numérico formal.

### Considerações Operacionais

Os backups são realizados online através do `pg_dump`. O PostgreSQL e a Finance API permanecem disponíveis durante a execução normal do backup.

A execução do backup pode gerar consumo adicional temporário de CPU e I/O no PostgreSQL.

Por esse motivo, o backup está programado para uma janela de menor utilização:

```text
05:00 UTC / 02:00 BRT
```

A infraestrutura de produção mantém o servidor em UTC para evitar dependência de timezone local.

Os backups permanecem armazenados localmente na VM de produção. A implementação atual não utiliza armazenamento externo, replicação PostgreSQL ou Point-in-Time Recovery.

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

1. faz checkout do commit exato associado à imagem candidata;
2. valida a configuração candidata do Docker Compose;
3. realiza o pull da imagem antes de alterar a configuração de runtime;
4. cria um backup do Compose atualmente utilizado em produção;
5. sincroniza a configuração versionada com o ambiente de runtime;
6. realiza o deploy da imagem candidata;
7. valida a saúde da aplicação.

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

1. identifica a falha da versão candidata;
2. restaura a configuração anterior do Docker Compose;
3. valida a configuração restaurada;
4. recria o container utilizando a imagem imutável anterior;
5. aguarda a recuperação da aplicação;
6. valida novamente o health check.

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

- autenticação JWT para endpoints protegidos;
- comunicação pública através de HTTPS;
- Nginx como reverse proxy público;
- certificados TLS emitidos pela Let's Encrypt;
- renovação automática de certificados com Certbot;
- aplicação vinculada apenas a `127.0.0.1:8080`;
- ausência de acesso público direto à porta TCP `8080`;
- secrets de produção mantidos fora do repositório;
- imagens Docker de produção identificadas por tags SHA imutáveis;
- validação de saúde após novos deploys;
- rollback automático após deploys não saudáveis.

## Endpoint de Produção

A API está disponível em:

```text
https://finance-api.ronneyrocha.com.br
```

Health check:

```text
https://finance-api.ronneyrocha.com.br/actuator/health
```