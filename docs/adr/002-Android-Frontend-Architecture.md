# ADR 002: Android Frontend Architecture

**Date:** 2026-08-20
**Status:** Accepted

## Context
The FinOra Android application required a scalable, modern, and highly performant architecture to consume the existing Spring Boot backend APIs.

## Decision
We implemented the following technology stack and architectural patterns:
1. **Jetpack Compose:** For all UI rendering, enforcing Material Design 3 and custom `ui-ux-designer` Glassmorphism rules.
2. **Hilt:** For Dependency Injection (DI) to manage ViewModels, Network modules, and Token management.
3. **Retrofit & OkHttp:** For networking. Added an `AuthInterceptor` to automatically attach JWT Bearer tokens to requests.
4. **DataStore:** Replaced SharedPreferences for secure and asynchronous token storage.
5. **StateFlow / Resource Wrapper:** Used a sealed `Resource` class (Success, Error, Loading) to handle API responses and map them directly to Compose UI States.

## Consequences
- **Positive:** The UI is completely reactive. Errors (like 401 Unauthorized or wrong password) are caught by the `Resource.Error` state and displayed directly in the UI without crashing.
- **Negative:** Steeper learning curve for developers unfamiliar with Jetpack Compose or Hilt.
