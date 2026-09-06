# Architecture

## Overview

FinOra follows a layered architecture based on Clean Architecture and DDD Lite. The domain is organized around core business entities, application services, infrastructure components, and their relationships.

The backend is built with:

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- Flyway
- Docker

The Android client is built with:

- Kotlin
- Jetpack Compose
- MVVM
- Hilt
- Retrofit + OkHttp
- DataStore

The main application domains are:

- Authentication (access + refresh tokens)
- Users
- Categories
- Transactions
- Budgets
- Receipts
- Dashboard
- AI Financial Insights
- Statistics & Reporting

---

## Entities

### User

Represents an application user and the owner of the user's financial data.

#### Relationships

- One User has many Categories.
- One User has many Transactions.
- One User has many Budgets.
- One User has many Receipts.
- One User has many RefreshTokens.

#### Fields

- id
- firstName
- lastName
- email
- password
- createdAt
- updatedAt

---

### RefreshToken

Represents a server-side stored refresh token tied to a user session.

#### Relationships

- Belongs to one User.

#### Fields

- id
- token
- user
- expiresAt
- createdAt

---

### Category

Represents how transactions and budgets are classified.

#### Examples

- Food
- Transport
- Shopping
- Bills
- Entertainment
- Health
- Other

#### Relationships

- Belongs to one User.
- Has many Transactions.
- Has many Budgets.

#### Fields

- id
- name
- icon
- color
- createdAt
- updatedAt

Categories are user-specific. A transaction or budget can only reference a category belonging to the current user.

Default categories are automatically seeded when a new user registers.

---

### Transaction

Represents a financial movement.

#### Relationships

- Belongs to one User.
- Belongs to one Category.
- May be associated with one Receipt.

#### Fields

- id
- amount
- type
- description
- transactionDate
- category
- user
- createdAt
- updatedAt

#### Transaction Types

- Income
- Expense

Transactions are the primary source for calculating financial statistics such as income, expenses, balance, and category spending.

---

### Budget

Represents a spending limit for a category during a specific period.

#### Relationships

- Belongs to one User.
- Belongs to one Category.

#### Fields

- id
- amount
- period
- startDate
- endDate
- category
- user
- createdAt
- updatedAt

#### Budget Periods

Supported budget periods include:

- Daily
- Weekly
- Monthly
- Yearly

Budget spending is calculated from transactions belonging to the same user and category within the budget's active period.

---

### Receipt

Represents an uploaded receipt that can be processed and optionally linked to a transaction.

#### Relationships

- Belongs to one User.
- May be linked to one Transaction.
- Has one Receipt Extraction.

#### Fields

- id
- originalFileName
- storageKey
- contentType
- fileSize
- status
- user
- transaction
- uploadedAt

#### Receipt Status

- UPLOADED
- PROCESSING
- PROCESSED
- FAILED

A receipt is not automatically converted into a transaction.

After the receipt is processed, the user can review and confirm the extracted information. Confirmation creates an expense transaction and links the receipt to that transaction.

A receipt can only be confirmed once.

---

### Receipt Extraction

Represents structured information extracted from a receipt image.

#### Relationships

- Belongs to one Receipt.

#### Fields

- receipt
- merchantName
- totalAmount
- transactionDate
- currency
- suggestedCategory

Receipt extraction is performed using Gemini Vision.

The model analyzes the receipt image directly and returns structured JSON containing:

- Merchant name
- Total amount
- Transaction date
- Currency
- Suggested category

The suggested category is only a recommendation. The final transaction category is selected or confirmed by the user.

---

## Receipt Processing Flow

```
User
 |
 | Upload Receipt (multipart)
 v
ReceiptController
 |
 v
ReceiptService
 |
 v
StorageService (LocalStorageService)
 |
 v
ReceiptEntity (status: UPLOADED → PROCESSING)
 |
 v
ReceiptExtractionService
 |
 v
GeminiVisionReceiptExtractor
 |
 v
ReceiptExtractionEntity
 |
 v
ReceiptEntity (status: PROCESSED)
 |
 | User reviews & confirms
 v
TransactionService
 |
 v
TransactionEntity (EXPENSE)
 |
 v
Receipt linked to Transaction
```

The current receipt extraction flow uses Gemini Vision directly.

Earlier OCR/Tesseract/OpenCV components were explored during development, but Gemini Vision is the active receipt extraction implementation. An OpenAIReceiptExtractor also exists as an alternative provider.

---

## Receipt Confirmation

Receipt extraction and transaction creation are intentionally separated.

The extraction process identifies probable financial information from the receipt:

- Merchant name
- Total amount
- Transaction date
- Currency
- Suggested category

The user must confirm the extracted information before a transaction is created.

### Confirmation Endpoint

`POST /receipts/{id}/confirm`

The confirmation request contains:

- amount
- description
- transactionDate
- categoryId

After confirmation:

1. A new EXPENSE transaction is created.
2. The selected category is assigned to the transaction.
3. The receipt is linked to the created transaction.
4. The receipt remains in PROCESSED status.

A receipt cannot be confirmed more than once.

If a receipt is already linked to a transaction, the API returns a `RECEIPT_ALREADY_CONFIRMED` business error.

---

## Dashboard

The Dashboard provides dynamically calculated financial information based on the user's transactions and budgets.

### Financial Statistics

The dashboard supports:

- Overall income
- Overall expense
- Overall balance
- Current-month income
- Current-month expense
- Current-month balance
- Current-month category spending
- Budget spending
- Budget remaining amount
- Budget usage percentage
- Recent transactions

Dashboard statistics are calculated dynamically from the user's stored financial data rather than being stored as duplicated aggregate values.

---

## Statistics & Reporting

The Statistics module provides date-range financial analysis:

- Total income and expenses for the period
- Net balance
- Daily statistics (amount per day)
- Monthly statistics (amount per month)
- Category breakdown (income/expense per category)
- Budget performance (spent vs. budget, usage percentage, over-budget flag)

---

## AI Financial Insights

FinOra includes an AI-powered financial insight system.

### Financial Insight Flow

```
Dashboard API
 |
 v
FinancialInsightService
 |
 v
FinancialInsightRateLimiter (Redis — 60s per user)
 |
 v
FinancialInsightContextBuilder
 |
 v
FinancialInsightPromptBuilder
 |
 v
FinancialInsightGenerator (abstraction)
 |
 +----> GeminiFinancialInsightGenerator
 |
 +----> OpenAIFinancialInsightGenerator
 |
 v
FinancialInsightResponse
```

### Financial Insight Context

The AI receives structured financial context containing:

- Overall income
- Overall expense
- Overall balance
- Current-month income
- Current-month expense
- Current-month balance
- Current-month category spending
- Existing budgets
- Budget amount
- Budget spending
- Budget remaining amount
- Budget usage percentage

The AI is instructed to use only the provided financial data and must not invent financial information.

### AI Financial Insight Rules

- Existing budgets are treated as already defined.
- The AI must not recommend creating a budget for a category that already has a budget.
- The AI must not claim that a budget is missing when one exists.
- Budget spending must come from the calculated budget context.
- Budget spending must not be inferred from unrelated category spending.
- Budgets below 80% usage are not treated as a budget problem.
- Budgets above 80% usage may be reported as a risk.
- Budgets above 100% usage are reported as exceeded.
- Recommendations must be based only on the supplied financial data.

### AI Financial Insight Response

```json
{
  "summary": "short overall financial summary",
  "monthlyStatus": {
    "income": 30000,
    "expenses": 2500,
    "balance": 27500
  },
  "budgetInsights": [
    {
      "category": "Food",
      "spent": 2000,
      "budget": 2500,
      "remaining": 500,
      "usagePercentage": 80
    }
  ],
  "recommendations": [
    "Monitor your Food budget closely."
  ]
}
```

### AI Provider Abstraction

AI financial insight generation is implemented behind the `FinancialInsightGenerator` abstraction.

This allows the application to support different AI providers without changing the financial insight business logic.

Currently supported implementations:

- `GeminiFinancialInsightGenerator`
- `OpenAIFinancialInsightGenerator`

The service layer is independent from the specific AI provider.

---

## Authentication

FinOra uses a dual-token authentication scheme.

### Access Token

- Short-lived JWT (1 hour)
- Sent in the `Authorization: Bearer <token>` header on every protected request

### Refresh Token

- Longer-lived opaque token stored server-side in the `refresh_tokens` table
- Used to obtain a new access token when the current one expires
- Endpoint: `POST /auth/refresh`

### Token Refresh Flow (Android)

```
Request fails with 401
 |
 v
OkHttp TokenAuthenticator triggered
 |
 v
POST /auth/refresh (with stored refresh token)
 |
 +----> Success → new access token stored → original request retried
 |
 +----> Failure → tokens cleared → user redirected to Login
```

The refresh is attempted once per failed request. If the refresh fails, the user session is terminated.

---

## Android Client Architecture

The Android application acts as a client of the FinOra REST API. It handles presentation, UI state, networking, session management, and local caching, while the backend remains the source of truth.

### Layers

- **Presentation**: Jetpack Compose screens, ViewModels, UI state, and navigation. ViewModels communicate with the API layer directly; they never expose raw Retrofit responses to the UI.
- **Data**: Retrofit API interfaces, DTOs (matching the exact JSON contract), network utilities, and TokenManager.
- **Core**: Hilt dependency injection, OkHttp (with `AuthInterceptor` and `TokenAuthenticator`), and centralized `ApiResponse` / `NetworkUtils` error handling.

### Navigation

Navigation is managed via a `NavGraph` with two root destinations:

- **Auth graph** (`onboarding`, `login`, `register`) — shown when no valid session exists
- **Main graph** (`MainScreen`) — shown when a valid session exists; hosts bottom navigation (Dashboard, Transactions, Budgets, Categories, Statistics, Receipts, Profile) with a central FAB

On app launch, `MainActivity` checks the stored token via `TokenManager` to determine the initial destination.

### API Interfaces

| Interface          | Domain          |
|--------------------|-----------------|
| `AuthApi`          | Authentication  |
| `CategoryApi`      | Categories      |
| `TransactionApi`   | Transactions    |
| `BudgetApi`        | Budgets         |
| `DashboardApi`     | Dashboard       |
| `StatisticsApi`    | Statistics      |
| `ReceiptApi`       | Receipts        |
| `UserApi`          | User profile    |

### API Contract & Networking

- **Contract**: Swagger/OpenAPI is the authoritative contract for endpoints and JSON fields.
- **Networking**: Retrofit and OkHttp are configured via a single Hilt `NetworkModule`.
- **Authentication**: `AuthInterceptor` attaches the access token to protected calls. `TokenAuthenticator` handles automatic refresh on 401.
- **Error handling**: `ApiResponse<T>` and `NetworkUtils` provide consistent Loading / Success / Error state across all ViewModels.

### Security & Environment

- No hardcoded production secrets.
- JWTs, refresh tokens, and sensitive data are never logged.
- Tokens are cleared on logout and on failed refresh.
- Base URL is configurable (`10.0.2.2:8080` for emulator, real IP for physical device).

### Database Migrations (Flyway)

| Migration | Description                        |
|-----------|------------------------------------|
| V1        | Initial schema (users)             |
| V2        | Refresh tokens table               |
| V3        | Categories table                   |
| V4        | Transactions table                 |
| V5        | Budgets table                      |
| V7        | Receipts table                     |
| V8        | Transaction FK on receipts         |
| V9        | Receipt extractions table          |
