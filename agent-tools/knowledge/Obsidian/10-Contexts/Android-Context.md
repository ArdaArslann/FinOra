# Android App Context

**Description:** Context file for the FinOra Android Application.
**Stack:** Kotlin, Jetpack Compose, Hilt, Retrofit, DataStore.

## Key Mechanisms
- **Authentication:** Managed by `TokenManager.kt` (DataStore) and `AuthInterceptor.kt` (OkHttp).
- **Navigation:** Jetpack Navigation Compose with a centralized `NavGraph.kt`.
- **UI System:** Custom theme (`SpaceDark`, `PrimaryNeon`) and components (`AnimatedPrimaryButton`, `GlassCard`).

## Rules (DO NOT BREAK)
1. Always handle `Resource.Error` in ViewModels and reflect it in the UI (e.g., Snackbar).
2. Never bypass Hilt for dependency injection.
