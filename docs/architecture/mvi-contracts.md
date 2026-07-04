# MVI Contracts

This document strictly defines the rules for creating `Contract` files in the RenCar project.

## 1. File Structure
Every screen must have a corresponding `<Feature>Contract.kt` file.

## 2. State Rules
- Must be a `data class` implementing `MviState`.
- Must be immutable (`val` properties only).
- Default values must be provided where applicable (e.g., `isLoading = false`).
- Example:
  ```kotlin
  data class FeatureState(
      val isLoading: Boolean = false,
      val errorMessage: String? = null,
      val data: List<String> = emptyList()
  ) : MviState
  ```

## 3. Intent Rules
- Must be a `sealed interface` implementing `MviIntent`.
- Represents actions triggered by the view.
- Example:
  ```kotlin
  sealed interface FeatureIntent : MviIntent {
      data object LoadData : FeatureIntent
      data class ItemClicked(val id: String) : FeatureIntent
  }
  ```

## 4. Effect Rules
- Must be a `sealed interface` implementing `MviEffect`.
- Represents fire-and-forget events (navigation, toasts).
- Example:
  ```kotlin
  sealed interface FeatureEffect : MviEffect {
      data object NavigateBack : FeatureEffect
      data class ShowError(val message: String) : FeatureEffect
  }
  ```
