# Transaction
- id
- amount
- currency
- type
- description
- merchant
- category 
- wallet
- createdAt
- updatedAt
- transactionDate

User
 └── Wallet
      └── Transaction
            ├── fromWallet (nullable)
            ├── toWallet (nullable)
            └── Categor

## Design Decision

Transactions support three types:

- Income
- Expense
- Transfer

Transfers use both `fromWallet` and `toWallet`.

Income uses only `toWallet`.

Expense uses only `fromWallet`.
 
