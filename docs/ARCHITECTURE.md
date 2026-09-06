# Architecture

## Overview

FinOra is a full-stack personal finance application consisting of:

- A **Java 21 / Spring Boot** REST API backend
- A **Kotlin / Jetpack Compose** native Android client

The backend follows a layered architecture based on Clean Architecture and DDD Lite. The Android client follows MVVM with a clear separation between presentation, data, and core layers.

---

## Backend

### Tech Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| Security | Spring Security + JWT (access + refresh) |
| Database | PostgreSQL 17 |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Cache | Redis 8 |
| Containerization | Docker + Docker Compose |
| Documentation | Swagger / OpenAPI |

### Domain Modules

| Package | Responsibility |
|---|---|
| `auth` | Registration, login, JWT generation, refresh tokens |
| `user` | User profile management |
| `category` | User-specific transaction categories |
| `transaction` | Financial transactions (income / expense) |
| `budget` | Category budgets with usage tracking |
| `dashboard` | Aggregated financial overview |
| `statistics` | Date-range financial reports |
| `receipt` | Receipt upload, AI extraction, confirmation |
| `dashboard.ai` | AI financial insight generation |
| `common` | Shared DTOs, exceptions, security, base entity |

### Layered Architecture (per module)

```
Controller  →  Service  →  Repository  →  Entity
                 ↑
              DTO / Mapper
```

- **Controllers**: Handle HTTP requests, delegate to services, return DTOs.
- **Services**: Business logic, enforcing user ownership.
- **Repositories**: Spring Data JPA interfaces.
- **Entities**: JPA-mapped persistence models.
- **DTOs**: Request/Response objects decoupled from entities.
- **Mappers**: Entity ↔ DTO conversions.

---

## Entities

### User

Represents an application user and the owner of all financial data.

**Relationships:**
- One User → many Categories
- One User → many Transactions
- One User → many Budgets
- One User → many Receipts
- One User → many RefreshTokens

**Fields:** `id`, `firstName`, `lastName`, `email`, `password`, `createdAt`, `updatedAt`

---

### RefreshToken

Server-stored refresh token for session renewal.

**Relationships:** Belongs to one User.

**Fields:** `id`, `token`, `user`, `expiresAt`, `createdAt`

---

### Category

Classifies transactions and budgets.

**Relationships:**
- Belongs to one User
- Has many Transactions
- Has many Budgets

**Fields:** `id`, `name`, `icon`, `color`, `createdAt`, `updatedAt`

Default categories are automatically created for every new user on registration:
`Food`, `Transport`, `Shopping`, `Bills`, `Entertainment`, `Health`, `Other`

---

### Transaction

Represents a financial movement (income or expense).

**Relationships:**
- Belongs to one User
- Belongs to one Category
- Optionally associated with one Receipt

**Fields:** `id`, `amount`, `type`, `description`, `transactionDate`, `category`, `user`, `createdAt`, `updatedAt`

**Types:** `INCOME`, `EXPENSE`

---

### Budget

Spending limit for a category within a time period.

**Relationships:**
- Belongs to one User
- Belongs to one Category

**Fields:** `id`, `amount`, `period`, `startDate`, `endDate`, `category`, `user`, `createdAt`, `updatedAt`

**Periods:** `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`

Budget spending is dynamically calculated from matching transactions.

---

### Receipt

Uploaded receipt image associated with a user.

**Relationships:**
- Belongs to one User
- Optionally linked to one Transaction
- Has one ReceiptExtraction

**Fields:** `id`, `originalFileName`, `storageKey`, `contentType`, `fileSize`, `status`, `user`, `transaction`, `uploadedAt`

**Status lifecycle:** `UPLOADED → PROCESSING → PROCESSED | FAILED`

---

### ReceiptExtraction

Structured financial data extracted from a receipt by AI.

**Relationships:** Belongs to one Receipt.

**Fields:** `receipt`, `merchantName`, `totalAmount`, `transactionDate`, `currency`, `suggestedCategory`

---

## Database Migrations (Flyway)

| Migration | Description |
|---|---|
| V1 | Initial schema — users table |
| V2 | Refresh tokens table |
| V3 | Categories table |
| V4 | Transactions table |
| V5 | Budgets table |
| V7 | Receipts table |
| V8 | Transaction FK on receipts |
| V9 | Receipt extractions table |

---

## Authentication Flow

FinOra uses a dual-token scheme:

| Token | Lifetime | Storage |
|---|---|---|
| Access Token (JWT) | 1 hour | Client memory / DataStore |
| Refresh Token | Long-lived | Server DB + Client DataStore |

### Login Flow

```
POST /auth/login
 → Access Token (JWT) + Refresh Token returned
 → Android stores both in DataStore via TokenManager
```

### Request Flow

```
API Request
 → AuthInterceptor attaches "Authorization: Bearer <access_token>"
 → Backend validates JWT
```

### Refresh Flow (Android — OkHttp TokenAuthenticator)

```
Request → 401 Unauthorized
 |
 v
TokenAuthenticator triggered
 |
 v
POST /auth/refresh { refreshToken }
 |
 +── Success → new access token saved → original request retried
 |
 +── Failure → tokens cleared → navigate to Login
```

---

## Receipt Processing Flow

```
Android (image picker)
 |
 | POST /receipts (multipart)
 v
ReceiptController → ReceiptService
 |
 v
LocalStorageService (file saved to disk)
 |
 v
ReceiptEntity saved (status: UPLOADED)
 |
 v
ReceiptExtractionService
 |
 v
GeminiVisionReceiptExtractor
 |
 v
ReceiptExtractionEntity saved
 |
 v
ReceiptEntity (status: PROCESSED)
 |
 | User reviews extraction on Android → confirms
 v
POST /receipts/{id}/confirm
 |
 v
TransactionService → EXPENSE Transaction created
 |
 v
Receipt linked to Transaction
```

**Confirmation request fields:** `amount`, `description`, `transactionDate`, `categoryId`

A receipt can only be confirmed once. Attempting to re-confirm returns `RECEIPT_ALREADY_CONFIRMED`.

---

## AI Financial Insights Flow

```
GET /dashboard/insights
 |
 v
FinancialInsightService
 |
 v
FinancialInsightRateLimiter
(Redis key: finora:rate-limit:financial-insight:{userId} — 60s TTL)
 |
 v
FinancialInsightContextBuilder
(builds income, expenses, balance, budgets, category spending)
 |
 v
FinancialInsightPromptBuilder
 |
 v
FinancialInsightGenerator (abstraction)
 |
 +──> GeminiFinancialInsightGenerator
 +──> OpenAIFinancialInsightGenerator
 |
 v
FinancialInsightResponse
{
  "summary": "...",
  "monthlyStatus": { income, expenses, balance },
  "budgetInsights": [ { category, spent, budget, remaining, usagePercentage } ],
  "recommendations": [ "..." ]
}
```

**AI Rules:**
- Uses only the supplied financial context — never invents data.
- Does not recommend creating a budget when one already exists.
- Budgets >80% usage → flagged as risk. >100% → flagged as exceeded.

---

## Dashboard

Dynamically calculated from user's stored transactions and budgets:

- Overall income / expenses / balance
- Current-month income / expenses / balance
- Current-month category spending breakdown
- Budget usage (amount, spent, remaining, usage %)
- Recent transactions

No pre-computed aggregate values are stored.

---

## Statistics & Reporting

Date-range report (`startDate`, `endDate`):

- Total income and expenses for the period
- Net balance
- Daily breakdown (income/expense per day)
- Monthly breakdown (income/expense per month)
- Category breakdown (income/expense per category)
- Budget performance per category (spent vs. budget, usage %, over-budget flag)

---

## Android Client Architecture

### Layer Overview

| Layer | Responsibility |
|---|---|
| **Presentation** | Jetpack Compose screens, ViewModels, UI state, navigation |
| **Data** | Retrofit API interfaces, DTOs, `TokenManager`, `NetworkUtils` |
| **Core** | Hilt `NetworkModule`, `AuthInterceptor`, `TokenAuthenticator`, `ApiResponse<T>` |

### Navigation

Two root graphs managed by `NavGraph`:

- **Auth graph** (`onboarding` → `login` → `register`) — shown when no valid session exists
- **Main graph** (`MainScreen`) — shown when a valid session exists

`MainActivity` reads the stored token from `TokenManager` to determine the initial destination at launch.

`MainScreen` hosts a bottom navigation bar + central FAB `Scaffold`:

| Screen | Route |
|---|---|
| Dashboard | `dashboard` |
| Transactions | `transactions` |
| Budgets | `budgets` |
| Categories | `categories` |
| Statistics | `statistics` |
| Receipts | `receipts` |
| Profile | `profile` |

### API Interfaces

| Interface | Domain |
|---|---|
| `AuthApi` | Register, Login, Refresh, Logout |
| `CategoryApi` | Category CRUD |
| `TransactionApi` | Transaction CRUD |
| `BudgetApi` | Budget CRUD |
| `DashboardApi` | Dashboard summary + AI insights |
| `StatisticsApi` | Date-range statistics |
| `ReceiptApi` | Upload, list, confirm receipts |
| `UserApi` | User profile |

### Networking

All API interfaces are instantiated via a single Hilt `NetworkModule`:

- `AuthInterceptor` — attaches `Authorization: Bearer <token>` to every protected request
- `TokenAuthenticator` — handles 401 by attempting one token refresh before clearing session
- `ApiResponse<T>` — wraps all responses in `Success / Error / Loading`
- `NetworkUtils` — extracts backend error codes/messages for user-facing display

### Security

- No hardcoded secrets anywhere in the codebase
- JWT and refresh tokens are stored only in DataStore
- Tokens are cleared on logout and on failed token refresh
- Sensitive data (tokens, passwords) are never logged
