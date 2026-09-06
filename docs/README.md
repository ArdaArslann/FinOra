# FinOra

> AI-powered Personal Finance Platform — Java Spring Boot backend + Kotlin Jetpack Compose Android client.

## Overview

FinOra is a modern personal finance platform that helps users manage income, expenses, and budgets while providing AI-powered receipt processing and financial insights to improve spending habits and financial decision-making.

The project is designed as a portfolio-quality, production-oriented application following modern software engineering practices.

## Features

### ✅ Backend

- JWT Authentication (Access + Refresh Token)
- User Registration & Login
- Default Category Seeding on Registration
- Category Management (CRUD)
- Transaction Management (CRUD)
- Budget Management (CRUD, usage tracking)
- Dashboard Summary (income, expenses, balance, category spending, budget overview, recent transactions)
- Statistics & Reporting (date-range, daily/monthly trends, category breakdown, budget performance)
- Receipt Upload (JPG, JPEG, PNG, PDF — max 5 MB)
- Gemini Vision Receipt Processing
- Receipt Extraction & Persistence
- Receipt Confirmation → Transaction Creation
- Receipt-to-Transaction Linking
- AI Financial Insights (structured summary, budget insights, recommendations)
- Gemini AI Provider
- OpenAI AI Provider
- AI Provider Abstraction
- Redis-backed AI Rate Limiting (per-user, 60s TTL)
- Global Exception Handling
- Request Validation (Jakarta Bean Validation)
- Swagger / OpenAPI Documentation
- PostgreSQL Persistence
- Flyway Database Migrations (V1–V9)
- Dockerized Environment (Docker Compose)

### ✅ Android

- Onboarding screen
- Register & Login flows with backend error display
- Token Manager (DataStore — access + refresh tokens)
- Automatic token refresh on 401 (OkHttp Authenticator)
- Dynamic navigation based on authentication state
- Dashboard screen (live API integration)
- Transactions screen
- Budget screen
- Category screen
- Statistics screen (custom Canvas charts — daily/monthly/category)
- Receipt screen (Android image picker + multipart upload to backend)
- Profile screen
- Bottom navigation via MainScreen Scaffold with central FAB
- Hilt dependency injection
- Retrofit + OkHttp networking

## Tech Stack

### Android

- Kotlin
- Jetpack Compose
- MVVM
- Hilt
- Retrofit + OkHttp
- DataStore

### Backend

- Java 21
- Spring Boot
- Spring Security
- JWT (Access + Refresh Tokens)
- PostgreSQL
- Redis
- Docker + Docker Compose
- Flyway
- JPA / Hibernate

### AI

- Google Gemini (Vision + Text)
- OpenAI

## Architecture

- Clean Architecture (layered)
- DDD Lite
- RESTful API
- DTO-based API Design
- Service / Repository Pattern
- AI Provider Abstraction
- MVVM (Android)

## Getting Started

### Prerequisites

- Docker & Docker Compose
- Android Studio (for Android client)

### Backend

```bash
# 1. Copy and fill environment variables
cp .env.example .env

# 2. Start all services
docker compose up --build
```

Required environment variables (see `.env.example`):

| Variable          | Description              |
|-------------------|--------------------------|
| `DB_PASSWORD`     | PostgreSQL password      |
| `JWT_SECRET`      | JWT signing secret       |
| `OPENAI_API_KEY`  | OpenAI API key           |
| `GEMINI_API_KEY`  | Google Gemini API key    |

Backend runs on `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Android

Open the `android/` directory in Android Studio and run on an emulator or physical device.
The default `BASE_URL` points to `http://10.0.2.2:8080` (Android emulator → host machine).

## Project Status

🚀 Active Development

### Backend — ✅ Complete

- ✅ Authentication (access + refresh tokens)
- ✅ Categories (CRUD + default seeding on registration)
- ✅ Transactions
- ✅ Budgets
- ✅ Dashboard
- ✅ Statistics & Reporting
- ✅ Receipt Upload & Processing (Gemini Vision)
- ✅ Receipt Confirmation → Transaction
- ✅ AI Financial Insights (Gemini + OpenAI)
- ✅ Redis Rate Limiting
- ✅ Swagger / OpenAPI
- ✅ Flyway Migrations (V1–V9)
- ✅ Docker Compose

### Android — Live API Integration Complete

- ✅ Onboarding
- ✅ Register & Login (with backend error messages)
- ✅ Token refresh & auth state navigation
- ✅ Dashboard (live data)
- ✅ Transactions
- ✅ Budgets
- ✅ Categories
- ✅ Statistics (custom Canvas charts)
- ✅ Receipt upload (image picker + backend processing)
- ✅ Profile
- ⏳ AI Insights screen (backend ready, Android UI pending)
- ⏳ Room local caching
- ⏳ Testing & UX polish

## Roadmap

- [x] Backend core modules
- [x] AI financial insights
- [x] Redis rate limiting
- [x] Statistics & Reports
- [x] Android live API integration
- [x] Token refresh flow
- [x] Statistics charts (Canvas)
- [x] Receipt image picker & upload
- [ ] AI Insights Android screen
- [ ] Room local caching
- [ ] Testing & UX polish
