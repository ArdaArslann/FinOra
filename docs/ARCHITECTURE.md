# Architecture

## Entities

### User

Represents an application user.

Relationships

- One User has many Wallets.
- One User has many Categories.

Fields

- id
- firstName
- lastName
- email
- password
- createdAt
- updatedAt

---

### Wallet

Represents where the user's money is stored.

Examples

- Cash
- Enpara
- Garanti Bonus

Relationships

- Belongs to one User.
- Has many outgoing Transactions.
- Has many incoming Transactions.

Wallet

Fields

- id
- name
- currency
- icon
- archived
- createdAt
- updatedAt

Balance is derived from transactions.

---

### Category

Represents how transactions are classified.

Examples

- Food
- Shopping
- Bills
- Transport

Relationships

- Belongs to one User.
- Has many Transactions.

Fields

- id
- name
- createdAt
- updatedAt

---

### Transaction

Represents a financial movement.

Relationships

- Belongs to one Category (nullable).
- References one source Wallet (nullable).
- References one destination Wallet (nullable).


Fields

- id
- amount
- currency
- type
- description
- merchant(nullable)
- category (nullable)
- fromWallet(nullable)
- toWallet(nullable)
- createdAt
- updatedAt
- transactionDate

      
User
├── Wallet
├── Category
│
└── Transaction
     ├── fromWallet
     ├── toWallet
     └── category

## Design Decision

Transactions support three types:

- Income
- Expense
- Transfer

Transfers use both `fromWallet` and `toWallet`.

Income uses only `toWallet`.

Expense uses only `fromWallet`.
 
