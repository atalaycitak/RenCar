# Repository Mode and Return Flow

## Repository Mode

The app supports two repository modes through the Gradle project property `rencar.repositoryMode`.

- `real` is the default mode and wires Koin to the default repositories.
- `fake` wires Koin to fake repositories for demo and offline development flows.

Examples:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assembleDebug --project-prop=rencar.repositoryMode=fake
```

The selected mode is exposed through `BuildConfig.REPOSITORY_MODE` and `BuildConfig.USE_FAKE_REPOSITORIES`.
The profile screen displays the active mode so testers can verify which data source is active.

## Vehicle Return Flow

The return flow is now the required path after an active rental.

1. Active rental screen opens the return checklist instead of completing the rental directly.
2. The customer captures four vehicle photos: front, back, left, and right.
3. The customer can add an optional damage note.
4. The app asks for final confirmation.
5. In real repository mode, the app calls `POST /rentals/{id}/return`.
6. On success, the app navigates to the trip summary screen.

The current backend API does not accept return photos or a damage note on the return endpoint.
Therefore, photos and the note are validated and retained client-side for the return flow, while the actual backend completion uses the documented return endpoint.
