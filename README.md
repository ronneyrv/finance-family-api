# Finance Family API

## Personal & Household Finance Management Platform

Backend REST API for a full-stack financial management platform designed
for personal and household finance.

Finance Family was built as a production-oriented portfolio project,
focusing on real-world software engineering practices such as domain
modeling, secure API design, automated testing, database versioning,
CI/CD, containerization, observability, deployment safety, and disaster
recovery.

> This repository contains the backend of the Finance Family platform.
> The frontend is maintained separately in
> [`finance-family-web`](https://github.com/ronneyrv/finance-family-web).

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![GitHub
Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)](https://github.com/features/actions)

Português: [`README.pt-BR.md`](README.pt-BR.md)

------------------------------------------------------------------------

## Project Links

-   [Frontend
    Repository](https://github.com/ronneyrv/finance-family-web)
-   [Architecture](#architecture)
-   [Testing](#testing)
-   [Production Infrastructure](#production-infrastructure)
-   [Database Migrations](#database-migrations)

------------------------------------------------------------------------

## Overview

Finance Family allows users to manage personal and household finances
through a domain-oriented REST API.

The platform covers financial transactions, financial accounts,
categories, credit cards, installment purchases, invoices, recurring
transactions, internal transfers, and consolidated financial analytics.

The backend is designed around a clear separation between API,
application services, domain entities, persistence, security, and
infrastructure.

------------------------------------------------------------------------

## Key Features

### Financial Management

-   Income and expense management
-   Financial account management
-   Current account balance calculation
-   Financial categories and subcategories
-   Internal transfers between financial accounts
-   Recurring transactions

### Categories

-   System default categories
-   Personal expense categories
-   Personal subcategories
-   Purchase category and subcategory management

### Credit Cards

-   Credit card management
-   Purchase registration
-   Installment purchases
-   Credit card installments
-   Invoice generation and tracking
-   Invoice payment
-   Invoice payment linked to financial accounts
-   Separation between economic expenses and credit card payment
    movements
-   Purchase category and subcategory association

### Household Finance

-   User and household organization
-   User profile and avatar
-   Household-level financial aggregation
-   Individual and consolidated financial views
-   Data ownership and access isolation

### Financial Analytics

-   Monthly financial summaries
-   Category expense analysis
-   Monthly projections
-   Cash flow
-   Cumulative financial results
-   Credit card expense trends
-   Income commitment analysis
-   Financial health indicators

### Platform

-   JWT authentication
-   Refresh token support
-   Request validation
-   Centralized exception handling
-   Database versioning with Flyway
-   OpenAPI documentation
-   Spring Boot Actuator
-   Dockerized development environment
-   Automated integration tests
-   Production health checks
-   Immutable Docker image deployments
-   Automated deployment rollback
-   PostgreSQL backup and restore procedures

------------------------------------------------------------------------

## Engineering Highlights

Finance Family was developed incrementally using a feature-oriented
workflow and production-oriented engineering practices.

### Domain Modeling

The financial domain includes relationships between:

``` text
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

The model explicitly distinguishes economic events from settlement
movements.

For example, paying a credit card invoice decreases the selected
financial account balance without recording the original purchase as a
second expense.

### Security

The API uses Spring Security and JWT-based authentication.

Security-related responsibilities include:

-   authentication
-   authorization
-   password hashing
-   JWT validation
-   refresh token management
-   protected endpoints
-   resource ownership
-   household-level data isolation
-   production secret management

### Database Integrity

Database structure is controlled by Flyway migrations.

Hibernate runs with:

``` properties
spring.jpa.hibernate.ddl-auto=validate
```

This means Hibernate validates the entity mapping against the existing
schema without automatically modifying the database structure.

The same migration strategy is also used by integration tests.

### Automated Testing

Integration tests use:

-   JUnit
-   Spring Boot Test
-   PostgreSQL
-   Testcontainers
-   Flyway

The test environment provisions a real PostgreSQL database through
Testcontainers and applies the same database migration strategy used by
the application.

This avoids relying on an in-memory database whose behavior could differ
from PostgreSQL in production.

### CI/CD

The project uses GitHub Actions to automate validation and deployment.

The production pipeline follows the general flow:

``` text
Code Change
    ↓
Automated Tests
    ↓
Application Build
    ↓
Docker Image Build
    ↓
Immutable SHA-based Image
    ↓
Container Registry
    ↓
Production Deployment
    ↓
Health Check
```

Production images are associated with specific Git commits through
SHA-based tags.

### Deployment Safety

Production deployments validate the candidate version before considering
it healthy.

If the new application fails its health check, the deployment process
can automatically restore the previous application version and Docker
Compose configuration.

This provides a controlled rollback mechanism instead of leaving the
production environment running an unhealthy release.

### Observability

Spring Boot Actuator provides application health information.

Health checks are also used as part of the deployment process to
determine whether a new version is operational.

------------------------------------------------------------------------

## Architecture

The backend follows a layered architecture:

``` text
HTTP Request
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

The main application structure is organized around:

``` text
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

------------------------------------------------------------------------

## Technology Stack

### Backend

-   Java 21
-   Spring Boot 3.5.3
-   Spring Web
-   Spring Data JPA
-   Hibernate
-   Spring Security
-   Bean Validation
-   Spring Boot Actuator
-   JWT
-   Lombok
-   Gradle

### Database

-   PostgreSQL 17
-   Flyway
-   Testcontainers

### API Documentation

-   OpenAPI
-   Swagger UI

### Infrastructure

-   Docker
-   Docker Compose
-   GitHub Actions
-   GitHub Container Registry
-   Oracle Cloud Infrastructure
-   Nginx
-   Let's Encrypt
-   Certbot

------------------------------------------------------------------------

## Domain Model

The main domain entities include:

``` text
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

Users belong to a `Household`, allowing the platform to provide both
individual and consolidated financial views.

Financial transactions are associated with financial accounts and
categorized according to the application's financial domain.

Credit card purchases are modeled separately from their installments and
invoices, allowing the system to represent the lifecycle of a credit
card expense.

------------------------------------------------------------------------

## Environment Profiles

The application separates environment-specific behavior through Spring
Boot profiles.

Profile   Purpose             Database                    Development Data
  --------- ------------------- --------------------------- ------------------
`dev`     Local development   PostgreSQL                  Enabled
`test`    Automated tests     PostgreSQL Testcontainers   Test fixtures
`prod`    Production          PostgreSQL                  Disabled

### Development

The `dev` profile provides a convenient local environment with
PostgreSQL and development fixtures.

### Test

The `test` profile uses PostgreSQL Testcontainers and isolated fixtures.

Flyway migrations are applied before the integration tests, keeping the
database schema aligned with the application's migration history.

### Production

The `prod` profile disables development data initialization and keeps
internal development tooling unavailable.

Swagger/OpenAPI and detailed health information are not exposed in the
production environment.

------------------------------------------------------------------------

## Local Development

### Requirements

-   Git
-   Docker
-   Docker Compose
-   Java 21

The project uses the Gradle Wrapper, so a global Gradle installation is
not required.

### Environment Variables

Create a local `.env` file from the provided example:

``` bash
cp .env.example .env
```

Configure the required values for:

``` text
DB_NAME
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
CORS_ALLOWED_ORIGINS
```

Never commit local secrets or production credentials.

### Run with Docker Compose

Start the complete development environment:

``` bash
docker compose up --build
```

Or run it in the background:

``` bash
docker compose up --build -d
```

Check container status:

``` bash
docker compose ps
```

Follow API logs:

``` bash
docker compose logs -f finance-api
```

Stop the environment:

``` bash
docker compose down
```

To remove the local PostgreSQL volume as well:

``` bash
docker compose down -v
```

> The `-v` option permanently removes the local database volume.

### Run PostgreSQL in Docker and the API with Gradle

Start PostgreSQL:

``` bash
docker compose up -d postgres
```

Load local environment variables:

``` bash
set -a
source .env
set +a
```

Run the API:

``` bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

This development workflow allows the API to be restarted without
rebuilding its Docker image after every code change.

------------------------------------------------------------------------

## API Validation

Once the application is running, validate the health endpoint:

``` bash
curl http://localhost:8080/actuator/health
```

A healthy application should return:

``` json
{
  "status": "UP"
}
```

In development, the interactive API documentation is available through
Swagger UI.

The OpenAPI specification is also exposed by the development
environment.

------------------------------------------------------------------------

## Testing

Run the test suite:

``` bash
./gradlew test
```

Run the complete test suite from a clean state:

``` bash
./gradlew clean test
```

Compile the application without running tests:

``` bash
./gradlew compileJava
```

The integration test environment uses PostgreSQL Testcontainers and
Flyway to validate the application against a database environment that
closely matches production.

------------------------------------------------------------------------

## Database Migrations

Database schema evolution is managed by Flyway.

Migration files are located at:

``` text
src/main/resources/db/migration
```

The migration history currently includes:

Migration   Purpose
  ----------- -------------------------------------------
`V1`        Create households
`V2`        Create users
`V3`        Create categories
`V4`        Create subcategories
`V5`        Create financial accounts
`V6`        Create transactions
`V7`        Create goals table
`V8`        Create credit cards
`V9`        Create purchases
`V10`       Create credit card installments
`V11`       Create recurring transactions
`V12`       Add transaction kind
`V13`       Create refresh tokens
`V14`       Add avatar columns to users
`V15`       Seed default categories
`V16`       Drop goals table
`V17`       Add personal categories and subcategories
`V18`       Add category and subcategory to purchases
`V19`       Add internal transfer support

Goals are no longer part of the current application domain. The
historical `V7` migration remains in the Flyway history because it
created the table, while `V16` explicitly removes it.

Flyway applies pending migrations during application startup.

Hibernate validates the resulting schema rather than modifying it
automatically.

------------------------------------------------------------------------

## Production Infrastructure

The production environment runs the API in a containerized deployment.

The general architecture is:

``` text
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
    ├── HTTP → HTTPS redirect
    │
    └── HTTPS / TLS
           │
           ▼
     Spring Boot API
           │
           ▼
      PostgreSQL
```

The Spring Boot application is not exposed directly to the public
internet.

Nginx acts as the public reverse proxy and handles HTTPS termination.

------------------------------------------------------------------------

## Production Deployment

The deployment workflow is based on:

``` text
Git Commit
    +
Docker Image SHA
    +
Versioned Docker Compose
```

The deployment process validates:

1.  the candidate Docker Compose configuration
2.  the candidate Docker image
3.  the application startup
4.  the application health endpoint

Only after the health check succeeds is the deployment considered
healthy.

------------------------------------------------------------------------

## Automatic Rollback

If a new release fails its health check, the deployment process can
restore the previous version.

The rollback restores:

``` text
Previous Docker Image
        +
Previous Docker Compose Configuration
```

The application is then restarted and validated through the health
endpoint.

This strategy reduces the risk of leaving production in an unhealthy or
partially deployed state.

------------------------------------------------------------------------

## Backup & Disaster Recovery

Production PostgreSQL data is protected through an automated backup and
restore process.

The backup flow is:

``` text
Cron
  ↓
Backup Script
  ↓
PostgreSQL Container
  ↓
pg_dump
  ↓
gzip
  ↓
Backup Storage
```

Backups are validated after creation and follow a retention policy.

The restore process validates the backup before rebuilding the database
and starting the application again.

The recovery flow includes:

``` text
Backup
  ↓
Validation
  ↓
Application Stop
  ↓
Database Recreation
  ↓
Restore
  ↓
Data Validation
  ↓
Application Start
  ↓
Health Check
```

The current infrastructure does not define a formal numerical RTO and
does not currently use external backup storage or PostgreSQL
Point-in-Time Recovery.

------------------------------------------------------------------------

## Development Workflow

Development follows a feature-oriented Git workflow:

``` text
Issue
  ↓
Feature Branch
  ↓
Implementation
  ↓
Tests
  ↓
Formatting / Linting
  ↓
Build Validation
  ↓
Conventional Commit
  ↓
Pull Request
  ↓
Review
  ↓
Merge
```

This workflow is used throughout the project's feature development and
maintenance history.

------------------------------------------------------------------------

## Project Evolution

Finance Family has evolved incrementally from its initial backend
foundation into a production-oriented full-stack platform.

Major engineering milestones include:

-   backend and database foundation
-   authentication and authorization
-   financial transaction domain
-   financial account management
-   credit card and installment management
-   invoice lifecycle and payments
-   recurring transactions
-   financial analytics and dashboard
-   CI/CD automation
-   immutable Docker deployments
-   automated rollback
-   PostgreSQL Testcontainers integration
-   financial health analysis
-   internal account transfers
-   production backup and disaster recovery procedures

The project is continuously refined through feature branches, pull
requests, testing, and incremental improvements.

------------------------------------------------------------------------

## Related Repository

### Finance Family Web

The frontend is maintained separately:

[Finance Family Web](https://github.com/ronneyrv/finance-family-web)

The web application is built with React, TypeScript, Vite, Tailwind CSS,
Axios, Recharts, Vitest, and React Testing Library.

------------------------------------------------------------------------

## Project Status

Finance Family is an actively evolving portfolio project focused on
demonstrating full-stack development and production-oriented software
engineering practices.

The application is designed to showcase not only feature implementation,
but also:

-   architecture
-   domain modeling
-   security
-   testing
-   database management
-   CI/CD
-   infrastructure
-   deployment safety
-   observability
-   operational reliability

------------------------------------------------------------------------

## Author

**Ronney Rocha**

Full Stack Developer focused on Java, Spring Boot, React, TypeScript,
and software engineering practices.

------------------------------------------------------------------------

## License

This project is maintained as a personal portfolio project.