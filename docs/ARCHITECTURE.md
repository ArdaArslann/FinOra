# Architecture

## Overview

FinOra follows a layered architecture based on Clean Architecture and DDD Lite. The domain is organized around core business entities and their relationships.

---

## Entities

### User

Represents an application user.

#### Relationships

- One User has many Categories.
- One User has many Transactions.
- One User has many Budgets.

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

Represents how transactions are classified.

#### Examples

- Food
- Shopping
- Bills
- Transportation
- Entertainment

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

---

### Transaction

Represents a financial movement.

#### Relationships

- Belongs to one User.
- Belongs to one Category.

#### Fields

- id
- amount
- type
- description
- transactionDate
- category
- createdAt
- updatedAt

#### Transaction Types

- Income
- Expense

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
- createdAt
- updatedAt

---

## Entity Relationships

```text
User
├── Category
│   ├── Transaction
│   └── Budget
│
├── Transaction
│
└── Budget
```

---

## Design Decisions

- Every entity belongs to a single user.
- Categories are user-specific.
- Budgets are defined per category and time period.
- Transactions are classified by category.
- Dashboard statistics are calculated dynamically from transactions and budgets.
- Entity IDs use UUID.
- Common audit fields (`id`, `createdAt`, `updatedAt`) are inherited from `BaseEntity`.
