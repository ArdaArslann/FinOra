# Product Requirements Document

## FinOra

---

## 1. Vision

FinOra is an AI-powered personal finance platform that enables users to manage their personal finances, track income and expenses, create category-based budgets, analyze their financial situation, process receipts using AI, and receive actionable financial insights.

FinOra combines traditional personal finance management with AI-powered automation and personalized financial guidance, delivered through a native Android application backed by a production-grade REST API.

---

## 2. Problem

Many people struggle to understand their financial situation because they:

- Don't know where their money goes.
- Cannot easily identify unnecessary spending.
- Have difficulty staying within a budget.
- Manually record financial transactions.
- Have difficulty extracting information from physical receipts.
- Lack personalized financial guidance.
- Cannot easily understand their financial situation from raw transaction data.

---

## 3. Solution

FinOra provides a modern and intelligent finance management experience with:

- Income and expense tracking
- User-specific categories (with automatic defaults on registration)
- Category-based budgets with usage tracking
- Interactive financial dashboard
- Date-range statistics and spending analytics
- AI-powered financial insights
- AI-powered receipt processing (Gemini Vision)
- Receipt-to-transaction confirmation workflow
- Secure user-isolated financial data
- Swagger / OpenAPI documentation
- Redis-backed AI request rate limiting

---

## 4. Target Users

- University Students
- Employees
- Freelancers
- Individuals who want to improve their financial habits
- Individuals who want to automate receipt-based expense entry

---

## 5. MVP Features

### Authentication

- User Registration
- User Login
- JWT Authentication (Access Token — 1 hour)
- Refresh Token (server-stored, used to renew access tokens)
- Automatic token refresh on 401 (Android client)
- Secure password handling (BCrypt)
- Authenticated current-user resolution
- User data isolation

---

### Categories

- Create Category
- Update Category
- Delete Category
- List Categories
- User-specific categories
- Category icon
- Category color
- Default category seeding on registration

**Default Categories:**

- Food
- Transport
- Shopping
- Bills
- Entertainment
- Health
- Other

---

### Transactions

- Create Transaction
- Update Transaction
- Delete Transaction
- Get Transaction by ID
- List Transactions
- Income Tracking
- Expense Tracking
- Transaction descriptions
- Transaction dates
- Category assignment
- Optional receipt association

**Transaction Types:**

- Income
- Expense

Transactions are associated with the authenticated user and one of the user's categories.

---

### Budget Management

- Create Budget
- Update Budget
- Delete Budget
- List Budgets
- Category-based budgets
- Daily budgets
- Weekly budgets
- Monthly budgets
- Yearly budgets
- Budget start date
- Budget end date
- Budget amount
- Budget usage tracking
- Remaining budget calculation
- Budget usage percentage

Budget spending is calculated from transactions belonging to the same user and category within the budget period.

---

### Dashboard

FinOra provides a dynamic financial dashboard based on the user's transactions and budgets.

**Financial Summary:**

- Overall income
- Overall expenses
- Overall balance
- Current-month income
- Current-month expenses
- Current-month balance

**Spending Analytics:**

- Current-month category spending
- Category-based expense analysis
- Recent transactions

**Budget Overview:**

- Budget amount
- Amount spent
- Remaining amount
- Budget usage percentage
- Budget risk information (>80% warning, >100% exceeded)

Dashboard statistics are calculated dynamically from the user's financial data rather than being stored as duplicated aggregate values.

---

### Statistics & Reporting

FinOra provides date-range financial reports beyond the current-month dashboard.

**Report Inputs:**

- Start date
- End date

**Report Outputs:**

- Total income for the period
- Total expenses for the period
- Net balance
- Daily statistics (income/expense per day)
- Monthly statistics (income/expense per month)
- Category breakdown (income/expense per category)
- Budget performance per category (amount, spent, remaining, usage %, over-budget flag)

---

## 6. AI Features

### AI Financial Insights

FinOra provides AI-powered financial analysis based on the user's financial context.

**The AI receives structured information including:**

- Overall income
- Overall expenses
- Overall balance
- Current-month income
- Current-month expenses
- Current-month balance
- Category spending
- Existing budgets
- Budget amount
- Budget spending
- Remaining budget
- Budget usage percentage

**The AI generates:**

- Financial summary
- Monthly financial status
- Budget insights
- Practical financial recommendations

**AI Response:**

```json
{
  "summary": "short financial summary",
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

**AI Financial Rules:**

The AI must:

- Use only the financial data provided by the application.
- Never invent financial information.
- Never assume personal information that was not provided.
- Treat existing budgets as already defined.
- Never recommend creating a new budget for a category that already has a budget.
- Never claim that an existing budget is missing.
- Use calculated budget spending rather than inferring spending from unrelated category data.
- Consider budgets above 80% usage as potential risks.
- Clearly identify budgets above 100% usage as exceeded.
- Provide practical recommendations based only on the available financial data.

---

### AI Provider Abstraction

AI financial insight generation is implemented through a provider abstraction (`FinancialInsightGenerator`).

The application currently supports:

- Gemini (`GeminiFinancialInsightGenerator`)
- OpenAI (`OpenAIFinancialInsightGenerator`)

The financial insight business logic (context building, prompt building, budget rules) is independent from the specific AI provider.

---

### AI Financial Insight Rate Limiting

Financial insight generation is protected by a per-user rate limiter.

Each authenticated user can request a financial insight once every 60 seconds.

Redis is used to store the temporary rate-limit state.

The rate-limit key structure:

```
finora:rate-limit:financial-insight:{userId}
```

The key automatically expires after 60 seconds using Redis TTL.

The rate limiter uses an atomic Redis operation so concurrent requests cannot bypass the limit.

---

## 7. Receipt Features

### Receipt Upload

Users can upload receipt files through the receipt API.

**Supported file types:**

- JPG
- JPEG
- PNG
- PDF

**Maximum file size:** 5 MB

Uploaded receipts are associated with the authenticated user.

Each receipt contains:

- Original file name
- Storage key
- Content type
- File size
- Processing status
- Upload timestamp
- Optional transaction association

---

### AI Receipt Processing

FinOra processes receipt images using Gemini Vision.

**The active receipt processing flow:**

```
Receipt Image
 |
 v
GeminiVisionReceiptExtractor
 |
 v
Structured JSON
 |
 v
ReceiptExtractionEntity
```

**The AI extracts:**

- Merchant name
- Total amount
- Transaction date
- Currency
- Suggested category

The extracted information is stored as a `ReceiptExtraction`.

The receipt extraction result is not automatically converted into a financial transaction.

---

### Receipt Extraction

The receipt extraction response contains:

```json
{
  "merchantName": "VISTA ROYAL OTEL MESKEN",
  "totalAmount": 1500.00,
  "transactionDate": "2023-04-26",
  "currency": "TRY",
  "suggestedCategory": "Food"
}
```

The suggested category is an AI recommendation only.

The user remains responsible for confirming the final transaction category and financial information.

---

### Receipt Confirmation

Receipt extraction and transaction creation are intentionally separated.

After a receipt has been processed, the user can review the extracted information and confirm it.

**Confirmation endpoint:** `POST /receipts/{id}/confirm`

**The confirmation request contains:**

- amount
- description
- transactionDate
- categoryId

**After confirmation:**

1. An EXPENSE transaction is created.
2. The selected category is assigned.
3. The receipt is linked to the transaction.
4. The receipt remains in PROCESSED status.

A receipt cannot be confirmed more than once.

If a receipt has already been linked to a transaction, the API returns `RECEIPT_ALREADY_CONFIRMED`.

---

### Receipt Status

Receipts use the following processing states:

```
UPLOADED → PROCESSING → PROCESSED
                      ↘ FAILED
```

A successfully processed receipt can subsequently be confirmed by the user.

---

## 8. API

FinOra exposes RESTful APIs using Spring Boot.

**The main API domains are:**

- Authentication (`/auth`)
- Categories (`/categories`)
- Transactions (`/transactions`)
- Budgets (`/budgets`)
- Receipts (`/receipts`)
- Dashboard (`/dashboard`)
- Statistics (`/statistics`)
- User Profile (`/users`)

API requests use DTOs rather than exposing persistence entities directly.

Jakarta Bean Validation is used to validate incoming requests.

The application provides centralized exception handling for consistent API error responses (`ApiErrorResponse`, `ValidationErrorResponse`).

Swagger / OpenAPI is used for:

- API documentation
- Endpoint discovery
- Request testing
- Response inspection
- Development integration testing

---

## 9. Security and Data Isolation

FinOra uses JWT-based authentication with a refresh token scheme.

Every financial resource belongs to an authenticated user.

The application enforces user ownership for:

- Categories
- Transactions
- Budgets
- Receipts
- Receipt extractions
- Dashboard data
- Statistics data

Users cannot access another user's financial data by providing another user's entity ID.

All business operations are performed within the context of the authenticated user.

Secrets (JWT signing key, DB password, AI API keys) are loaded from environment variables and never hardcoded in the repository.

---

## 10. Data and Persistence

PostgreSQL is the primary persistent database.

The application uses:

- Spring Data JPA
- Hibernate
- Flyway

Database schema changes are managed through Flyway migrations (V1–V9).

Hibernate automatic schema generation is disabled.

Redis is used for temporary application state (AI rate limiting) and is not used as the primary financial data store.

Entity identifiers use UUID.

Common audit fields:

- id (UUID)
- createdAt
- updatedAt

---

## 11. Infrastructure

FinOra's backend environment is containerized using Docker.

**The main services are:**

- FinOra Backend (Spring Boot)
- PostgreSQL 17
- Redis 8

The services are orchestrated using Docker Compose.

```
Docker Compose
 |
 +---- FinOra Backend (port 8080)
 |
 +---- PostgreSQL (port 5432)
 |
 +---- Redis (port 6379)
```

PostgreSQL provides persistent application data storage.

Redis provides temporary infrastructure functionality (rate limiting).

Environment variables are injected at runtime via `.env` (not committed to the repository).

---

## 12. Non-Functional Requirements

### Security

- JWT-based authentication (access + refresh tokens)
- Password hashing (BCrypt)
- User-level data isolation
- Input validation (Jakarta Bean Validation)
- Centralized exception handling
- No hardcoded secrets in the repository

### Reliability

- Explicit transaction boundaries
- Database migrations through Flyway
- Receipt processing failure handling (FAILED status)
- AI provider error handling
- Retry handling for transient Gemini Vision failures
- Redis-backed rate limiting
- Automatic token refresh on 401 (Android client)

### Performance

- Database queries are scoped to the authenticated user
- Dashboard calculations are performed dynamically from relevant financial data
- Redis is used for low-latency rate-limit checks
- AI requests are protected against excessive repeated calls

### Maintainability

- Layered architecture
- Service abstractions
- DTO-based API contracts
- Repository abstractions
- AI provider abstraction
- Clear separation between receipt extraction and transaction creation
- Centralized exception handling

---

## 13. Current MVP Status

### Backend — Complete

- ✅ User registration & login
- ✅ JWT authentication (access + refresh tokens)
- ✅ Default category seeding on registration
- ✅ Category management
- ✅ Transaction management
- ✅ Budget management
- ✅ Dashboard financial calculations
- ✅ Statistics & Reporting (date-range, daily/monthly/category/budget)
- ✅ Receipt upload & local storage
- ✅ Gemini Vision receipt extraction
- ✅ Receipt extraction persistence
- ✅ Receipt confirmation → transaction creation
- ✅ Receipt-to-transaction linking
- ✅ AI financial insights (Gemini + OpenAI)
- ✅ Structured AI insight responses
- ✅ AI financial insight budget rules
- ✅ Redis-backed AI rate limiting
- ✅ Swagger / OpenAPI API documentation
- ✅ PostgreSQL persistence
- ✅ Flyway database migrations (V1–V9)
- ✅ Dockerized backend environment

### Android — Live API Integration Complete

- ✅ Onboarding screen
- ✅ Register & Login (with backend error messages)
- ✅ Token Manager (DataStore)
- ✅ Automatic token refresh (OkHttp Authenticator)
- ✅ Dynamic auth-state navigation
- ✅ Dashboard screen (live API)
- ✅ Transactions screen
- ✅ Budgets screen
- ✅ Categories screen
- ✅ Statistics screen (custom Canvas charts)
- ✅ Receipt screen (image picker + multipart upload)
- ✅ Profile screen
- ✅ MainScreen Scaffold with bottom nav + central FAB
- ⏳ AI Insights screen
- ⏳ Room local caching
- ⏳ Testing & UX polish

---

## 14. Android Client Integration

### Integration Goals

The Android application is the client for the FinOra REST API. The backend remains the source of truth for authentication, financial data, calculations, and AI insights. The Android app manages presentation, UI state, networking, and local caching.

### Features & Implementation Guidelines

- **Authentication & Session**: Register and Login flows. Store access/refresh tokens securely via `TokenManager` (DataStore). Handle 401 Unauthorized by attempting a token refresh once via `TokenAuthenticator`. Never perform automatic login to recover from 401 — clear tokens and redirect to Login.
- **Categories**: Category listing and CRUD. Server-created default categories must not be duplicated by the Android client.
- **Transactions**: Create, update, delete, detail, and list flows. Refresh relevant UI state from server responses.
- **Budgets**: Budget CRUD, displaying amount, spending, remaining amount, usage percentage, and risk status.
- **Dashboard**: Build using backend-calculated values (overall income, expenses, balance, recent transactions, category spending, budget overview). Avoid recalculating authoritative totals locally.
- **Statistics and Reporting**: Date-range reports. Render custom Canvas charts for daily/monthly distributions and category breakdowns. Handle empty periods gracefully.
- **Receipt Upload & Confirmation**: Multipart receipt upload showing processing states (UPLOADED → PROCESSING → PROCESSED / FAILED). Display extracted receipt data for user review (merchant, amount, date, currency, suggested category). Confirmation screen converts the extraction into a transaction.
- **AI Financial Insights**: Consume the structured AI response to display summary, monthly financial status, budget insights, and recommendations. (Android screen pending.)
- **Error Handling & UI State**: Every screen must explicitly handle Loading, Success, Empty, and Error states. Use `NetworkUtils` and `ApiResponse<T>` to map backend error codes (e.g., `UNAUTHORIZED`, `INVALID_CREDENTIALS`, `RECEIPT_ALREADY_CONFIRMED`) into user-friendly messages.

### Definition of Done

A complete integration allows a new user to:

1. Register and receive default categories automatically
2. Log in and maintain session across app restarts
3. Manage categories, transactions, and budgets
4. View dashboard and statistics with live data
5. Upload and confirm receipts → transaction created
6. View AI financial insights

Errors are mapped consistently and no sensitive information appears in logs.
