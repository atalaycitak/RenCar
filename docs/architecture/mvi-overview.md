# MVI Architecture Overview

This document outlines the standard Model-View-Intent (MVI) architecture used in the RenCar project. All developers and AI agents must strictly adhere to this structure.

## 1. Core Principles
The MVI architecture ensures a unidirectional data flow. The components interact as follows:
- **View (Screen):** Renders the UI based on the `State`. Emits `Intent` based on user actions. Never holds business logic.
- **Intent:** Represents user actions or system events (e.g., `LoadData`, `OnSubmitClicked`).
- **ViewModel:** Subscribes to `Intent`s, executes business logic via `UseCase`s, and produces a new `State` or `Effect`.
- **State:** A single, immutable data class representing the entire UI state.
- **Effect:** One-time UI events (e.g., Navigation, SnackBar) that should not be persisted in the State.

## 2. Layer Definitions
- **Presentation:** Contains Jetpack Compose UI (`Screen`), `Contract` (State/Intent/Effect), and `ViewModel`.
- **Domain:** Contains pure Kotlin logic. Includes `Model`, `UseCase`, and `Repository` interfaces.
- **Data:** Contains implementations of `Repository`, Network calls (Retrofit), and Local Storage (DataStore).

## 3. Data Flow
1. User interacts with the UI (e.g., clicks a button).
2. UI fires an `Intent` to the `ViewModel`.
3. `ViewModel` processes the `Intent` and calls a `UseCase` in the Domain layer.
4. `UseCase` fetches/mutates data via `Repository`.
5. `ViewModel` updates the `State` using `.copy()` and emits it via `StateFlow`.
6. UI observes the `StateFlow` and recomposes automatically.
