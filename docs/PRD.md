# Product Requirements Document — FinOra

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

- University students
- Employees
- Freelancers
- Individuals who want to improve their financial habits
- Individuals who want to automate receipt-based expense entry

---

## 5. Features

### 5.1 Authentication

| Feature | Status |
|---|---|
| User Registration | ✅ |
| User Login | ✅ |
| JWT Access Token (1 hour) | ✅ |
| Refresh Token (server-stored) | ✅ |
| Automatic token refresh on 401 (Android) | ✅ |
| BCrypt password hashing | ✅ |
| Authenticated current-user resolution | ✅ |
| User data isolation | ✅ |

---

### 5.2 Categories

| Feature | Status |
|---|---|
| Create Category | ✅ |
| Update Category | ✅ |
| Delete Category | ✅ |
| List Categories | ✅ |
| User-specific categories | ✅ |
| Category icon | ✅ |
| Category color | ✅ |
| Default category seeding on registration | ✅ |

**Default Categories:** Food, Transport, Shopping, Bills, Entertainment, Health, Other

---

### 5.3 Transactions

| Feature | Status |
|---|---|
| Create Transaction | ✅ |
| Update Transaction | ✅ |
| Delete Transaction | ✅ |
| Get Transaction by ID | ✅ |
| List Transactions | ✅ |
| Income tracking | ✅ |
| Expense tracking | ✅ |
| Transaction description | ✅ |
| Transaction date | ✅ |
| Category assignment | ✅ |
| Optional receipt association | ✅ |

**Transaction Types:** `INCOME`, `EXPENSE`

---

### 5.4 Budget Management

| Feature | Status |
|---|---|
| Create Budget | ✅ |
| Update Budget | ✅ |
| Delete Budget | ✅ |
| List Budgets | ✅ |
| Category-based budgets | ✅ |
| Daily / Weekly / Monthly / Yearly periods | ✅ |
| Budget start and end dates | ✅ |
| Budget usage tracking | ✅ |
| Remaining budget calculation | ✅ |
| Budget usage percentage | ✅ |
| Budget risk flags (>80%, >100%) | ✅ |

Budget spending is dynamically calculated from transactions within the same user, category, and period.

---

### 5.5 Dashboard

| Feature | Status |
|---|---|
| Overall income / expenses / balance | ✅ |
| Current-month income / expenses / balance | ✅ |
| Current-month category spending | ✅ |
| Budget overview (spent, remaining, %) | ✅ |
| Budget risk information | ✅ |
| Recent transactions | ✅ |

Dashboard statistics are calculated dynamically — no duplicated aggregate storage.

---

### 5.6 Statistics & Reporting

| Feature | Status |
|---|---|
| Date-range report (startDate, endDate) | ✅ |
| Total income and expenses for period | ✅ |
| Net balance | ✅ |
| Daily breakdown | ✅ |
| Monthly breakdown | ✅ |
| Category breakdown | ✅ |
| Budget performance (spent, budget, %, over-budget) | ✅ |

---

### 5.7 Receipt Processing

| Feature | Status |
|---|---|
| Receipt upload (JPG, JPEG, PNG, PDF — max 5 MB) | ✅ |
| Receipt storage (local) | ✅ |
| Gemini Vision extraction | ✅ |
| Receipt extraction persistence | ✅ |
| Receipt status lifecycle (UPLOADED → PROCESSING → PROCESSED / FAILED) | ✅ |
| Receipt confirmation (user review + approve) | ✅ |
| EXPENSE transaction creation on confirmation | ✅ |
| Receipt-to-transaction linking | ✅ |
| One-time confirmation enforcement | ✅ |

**Extracted fields:** `merchantName`, `totalAmount`, `transactionDate`, `currency`, `suggestedCategory`

---

### 5.8 AI Financial Insights

| Feature | Status |
|---|---|
| Structured financial context sent to AI | ✅ |
| Financial summary generation | ✅ |
| Monthly status (income, expenses, balance) | ✅ |
| Budget insights per category | ✅ |
| Practical recommendations | ✅ |
| Gemini AI provider | ✅ |
| OpenAI AI provider | ✅ |
| AI provider abstraction | ✅ |
| Per-user Redis rate limiting (60s TTL) | ✅ |

**AI Response schema:**

```json
{
  "summary": "...",
  "monthlyStatus": { "income": 0, "expenses": 0, "balance": 0 },
  "budgetInsights": [
    { "category": "Food", "spent": 0, "budget": 0, "remaining": 0, "usagePercentage": 0 }
  ],
  "recommendations": ["..."]
}
```

**AI Rules:**
- Uses only supplied financial data — never invents information.
- Does not recommend creating a budget when one already exists.
- Budgets >80% usage → risk. Budgets >100% → exceeded.

---

## 6. API

| Domain | Base Path |
|---|---|
| Authentication | `/auth` |
| Categories | `/categories` |
| Transactions | `/transactions` |
| Budgets | `/budgets` |
| Dashboard | `/dashboard` |
| Statistics | `/statistics` |
| Receipts | `/receipts` |
| User Profile | `/users` |

- All requests/responses use DTOs (entities are not exposed).
- Jakarta Bean Validation on all request bodies.
- Centralized exception handling (`GlobalExceptionHandler`) for consistent `ApiErrorResponse`.
- Swagger / OpenAPI available at `/swagger-ui.html`.

---

## 7. Security & Data Isolation

| Requirement | Implementation |
|---|---|
| Authentication | JWT (access + refresh) |
| Password storage | BCrypt |
| User data isolation | All queries scoped to authenticated user |
| Secret management | Environment variables (never hardcoded) |
| Token storage (Android) | DataStore only — never logs |
| Session termination | Tokens cleared on logout and failed refresh |

Users cannot access another user's data by supplying a foreign entity ID.

---

## 8. Data & Persistence

| Technology | Role |
|---|---|
| PostgreSQL 17 | Primary persistent data store |
| Redis 8 | Temporary state (AI rate limiting) |
| Flyway | Schema migrations (V1–V9) |
| Spring Data JPA + Hibernate | ORM |

- Hibernate schema auto-generation is disabled.
- All entity IDs use UUID.
- Common audit fields: `id`, `createdAt`, `updatedAt`.

---

## 9. Infrastructure

```
Docker Compose
 |
 ├── FinOra Backend (port 8080)
 ├── PostgreSQL 17 (port 5432)
 └── Redis 8 (port 6379)
```

Secrets are injected via environment variables (`.env` file, not committed to the repository).

---

## 10. Non-Functional Requirements

### Security
- JWT authentication (access + refresh tokens)
- BCrypt password hashing
- User-level data isolation
- Input validation
- Centralized exception handling
- No hardcoded secrets

### Reliability
- Explicit transaction boundaries
- Flyway database migrations
- Receipt processing failure handling (`FAILED` status)
- AI provider error handling
- Retry handling for transient Gemini Vision failures
- Atomic Redis rate limiting
- Automatic token refresh on 401 (Android)

### Performance
- All DB queries scoped to authenticated user
- Dashboard calculated dynamically (no stale aggregates)
- Redis for low-latency rate-limit checks
- AI calls protected by per-user rate limiting

### Maintainability
- Layered architecture per module
- Service / repository abstractions
- DTO-based API contracts
- AI provider abstraction
- Clear separation: receipt extraction ≠ transaction creation

---

## 11. Implementation Status

### Backend — ✅ Complete

| Module | Status |
|---|---|
| Authentication (access + refresh tokens) | ✅ |
| Default category seeding | ✅ |
| Category CRUD | ✅ |
| Transaction CRUD | ✅ |
| Budget CRUD + usage | ✅ |
| Dashboard | ✅ |
| Statistics & Reporting | ✅ |
| Receipt upload & storage | ✅ |
| Gemini Vision receipt extraction | ✅ |
| Receipt confirmation → transaction | ✅ |
| AI financial insights (Gemini + OpenAI) | ✅ |
| Redis rate limiting | ✅ |
| Swagger / OpenAPI | ✅ |
| Flyway migrations (V1–V9) | ✅ |
| Docker Compose | ✅ |

### Android — ✅ Complete

| Feature | Status |
|---|---|
| Onboarding | ✅ |
| Register & Login | ✅ |
| Token refresh + auth-state navigation | ✅ |
| Dashboard | ✅ |
| Transactions | ✅ |
| Budgets | ✅ |
| Categories | ✅ |
| Statistics (custom Canvas charts) | ✅ |
| Receipt upload (image picker + backend) | ✅ |
| Profile | ✅ |
