# FinOra

> AI-powered Personal Finance Platform — Java Spring Boot backend + Kotlin Jetpack Compose Android client.

## Overview

FinOra is a modern personal finance platform that helps users manage income, expenses, and budgets while providing AI-powered receipt processing and financial insights to improve spending habits and financial decision-making.

The project is a portfolio-quality, full-stack mobile application following modern software engineering practices — from a production-grade REST API to a native Android client.

## Tech Stack

### Android

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM |
| DI | Hilt |
| Networking | Retrofit + OkHttp |
| Session | DataStore (TokenManager) |

### Backend

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| Security | Spring Security + JWT |
| Database | PostgreSQL 17 |
| Cache | Redis 8 |
| Migrations | Flyway |
| ORM | JPA / Hibernate |
| Container | Docker + Docker Compose |

### AI

| Provider | Usage |
|---|---|
| Google Gemini | Vision receipt extraction + financial insights |
| OpenAI | Financial insights (alternative provider) |

## Features

### Backend ✅

- JWT Authentication (Access + Refresh Token)
- User Registration & Login
- Default Category Seeding on Registration
- Category Management (CRUD)
- Transaction Management (CRUD)
- Budget Management (CRUD + usage tracking)
- Dashboard (income, expenses, balance, category spending, budget overview, recent transactions)
- Statistics & Reporting (date-range, daily/monthly trends, category breakdown, budget performance)
- Receipt Upload (JPG, JPEG, PNG, PDF — max 5 MB)
- Gemini Vision Receipt Processing
- Receipt Extraction & Persistence
- Receipt Confirmation → Transaction Creation
- AI Financial Insights (Gemini + OpenAI with provider abstraction)
- Redis-backed AI Rate Limiting (per-user, 60s TTL)
- Global Exception Handling
- Jakarta Bean Validation
- Swagger / OpenAPI Documentation
- Flyway Migrations (V1–V9)
- Dockerized (Docker Compose)

### Android ✅

- Onboarding screen
- Register & Login with backend error display
- TokenManager (DataStore — access + refresh tokens)
- Automatic token refresh on 401 (OkHttp `TokenAuthenticator`)
- Dynamic auth-state navigation
- Dashboard screen (live API)
- Transactions screen
- Budget screen
- Category screen
- Statistics screen (custom Canvas charts — daily / monthly / category)
- Receipt screen (Android image picker + multipart upload)
- Profile screen
- MainScreen Scaffold with bottom navigation + central FAB
- Hilt dependency injection
- Retrofit + OkHttp networking with centralized error handling

## Architecture

- Clean Architecture (layered)
- DDD Lite
- RESTful API
- DTO-based API Design
- Service / Repository Pattern
- AI Provider Abstraction (`FinancialInsightGenerator`)
- MVVM + ViewModels (Android)

## Getting Started

### Prerequisites

- Docker & Docker Compose
- Android Studio (Hedgehog or newer)

### Backend

```bash
# 1. Copy and configure environment variables
cp .env.example .env
# Edit .env with your values

# 2. Start all services
docker compose up --build
```

Required environment variables:

| Variable | Description |
|---|---|
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | JWT signing secret |
| `OPENAI_API_KEY` | OpenAI API key |
| `GEMINI_API_KEY` | Google Gemini API key |

- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### Android

Open the `android/` directory in Android Studio and run on an emulator or physical device.

> The default `BASE_URL` is `http://10.0.2.2:8080` — this routes from the Android emulator to the host machine running the Docker backend.

## Project Status

**✅ Complete — Both backend and Android client are fully implemented.**

### Backend Modules

| Module | Status |
|---|---|
| Authentication (access + refresh tokens) | ✅ |
| Categories (CRUD + default seeding) | ✅ |
| Transactions | ✅ |
| Budgets | ✅ |
| Dashboard | ✅ |
| Statistics & Reporting | ✅ |
| Receipt Upload & Processing (Gemini Vision) | ✅ |
| Receipt Confirmation → Transaction | ✅ |
| AI Financial Insights (Gemini + OpenAI) | ✅ |
| Redis Rate Limiting | ✅ |
| Swagger / OpenAPI | ✅ |
| Flyway Migrations (V1–V9) | ✅ |
| Docker Compose | ✅ |

### Android Screens & Features

| Feature | Status |
|---|---|
| Onboarding | ✅ |
| Register & Login | ✅ |
| Token refresh & auth-state navigation | ✅ |
| Dashboard (live data) | ✅ |
| Transactions | ✅ |
| Budgets | ✅ |
| Categories | ✅ |
| Statistics (Canvas charts) | ✅ |
| Receipt upload (image picker) | ✅ |
| Profile | ✅ |
