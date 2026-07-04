# MVI ViewModel & DI Rules

This document outlines the strict rules for implementing ViewModels and Dependency Injection in the RenCar project.

## 1. Base Class
Every ViewModel must inherit from `BaseMviViewModel<State, Intent, Effect>`.
Initial state must be provided via the `createInitialState()` function.

## 2. Threading Rules
- ViewModels must NEVER perform long-running operations or I/O tasks on the Main thread.
- Use `viewModelScope.launch { }` for asynchronous operations.
- Delegate data fetching and business logic to `UseCase` classes.

## 3. Handling Intents
- All intents must be handled within the `handleIntent(intent: Intent)` function.
- Example:
  ```kotlin
  override fun handleIntent(intent: FeatureIntent) {
      when (intent) {
          is FeatureIntent.LoadData -> loadData()
      }
  }
  ```

## 4. Updating State
- State must only be updated using the `setState { copy(...) }` mechanism.
- Direct mutation is strictly prohibited.

## 5. Sending Effects
- Effects must be sent using the `setEffect { Effect }` mechanism.

## 6. Dependency Injection (Koin)
- ViewModels must use standard constructor injection.
- Do not use Hilt or Dagger. Koin is the mandated DI framework.
- Register ViewModels in `di/AppModule.kt` using `viewModelOf(::FeatureViewModel)`.
- Inject ViewModels in Compose screens using `koinViewModel()`.
