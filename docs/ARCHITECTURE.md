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

The main application domains are:

- Authentication
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

#### Fields

- id
- firstName
- lastName
- email
- password
- createdAt
- updatedAt

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


User
 |
 | Upload Receipt
 v
ReceiptController
 |
 v
ReceiptService
 |
 v
StorageService
 |
 v
ReceiptEntity
 |
 v
ReceiptExtractionService
 |
 v
Gemini Vision
 |
 v
ReceiptExtractionEntity
 |
 v
Processed Receipt
 |
 | User confirms
 v
TransactionService
 |
 v
TransactionEntity
 |
 v
Receipt linked to Transaction

The current receipt extraction flow uses Gemini Vision directly.

Earlier OCR/Tesseract/OpenCV components were explored during development, but Gemini Vision is the active receipt extraction implementation.

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

POST /receipts/{id}/confirm
The confirmation request contains:

* amount
* description
* transactionDate
* categoryId

After confirmation:

1. A new EXPENSE transaction is created.
2. The selected category is assigned to the transaction.
3. The receipt is linked to the created transaction.
4. The receipt remains in PROCESSED status.

A receipt cannot be confirmed more than once.

If a receipt is already linked to a transaction, the API returns a RECEIPT_ALREADY_CONFIRMED business error.

Dashboard

The Dashboard provides dynamically calculated financial information based on the user’s transactions and budgets.

Financial Statistics

The dashboard supports:

* Overall income
* Overall expense
* Overall balance
* Current-month income
* Current-month expense
* Current-month balance
* Current-month category spending
* Budget spending
* Budget remaining amount
* Budget usage percentage
* Recent transactions
  Dashboard statistics are calculated dynamically from the user’s stored financial data rather than being stored as duplicated aggregate values.
--------------------

AI Financial Insights
FinOra includes an AI-powered financial insight system.
The financial insight flow is:

Dashboard API
|
v
FinancialInsightService
|
v
FinancialInsightContextBuilder
|
v
FinancialInsightPromptBuilder
|
v
FinancialInsightGenerator
|
+----> Gemini
|
+----> OpenAI
|
v
FinancialInsightResponse
-------------------
Financial Insight Context

The AI receives structured financial context containing:

* Overall income
* Overall expense
* Overall balance
* Current-month income
* Current-month expense
* Current-month balance
* Current-month category spending
* Existing budgets
* Budget amount
* Budget spending
* Budget remaining amount
* Budget usage percentage

The AI is instructed to use only the provided financial data and must not invent financial information.
------------------

AI Financial Insight Rules

The financial insight system follows explicit budget rules:

* Existing budgets are treated as already defined.
* The AI must not recommend creating a budget for a category that already has a budget.
* The AI must not claim that a budget is missing when one exists.
* Budget spending must come from the calculated budget context.
* Budget spending must not be inferred from unrelated category spending.
* Budgets below 80% usage are not treated as a budget problem.
* Budgets above 80% usage may be reported as a risk.
* Budgets above 100% usage are reported as exceeded.
* Recommendations must be based only on the supplied financial data.
-----------------

AI Financial Insight Response

The AI returns a structured response containing:
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

The response is represented by FinancialInsightResponse and contains:

* Summary
* Monthly financial status
* Budget insights
* Practical recommendations
----------------

AI Provider Abstraction

AI financial insight generation is implemented behind the FinancialInsightGenerator abstraction.

This allows the application to support different AI providers without changing the financial insight business logic.

Currently supported implementations include:

* Gemini
* OpenAI

The service layer is therefore independent from the specific AI provider.

