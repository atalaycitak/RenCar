# Map Filtering

The home map supports vehicle filtering while preserving the existing MVI flow.

## Filter Sources

- Vehicle type is sent to the backend through `GET /vehicles?type=...`.
- Maximum daily price is applied client-side because the backend does not expose a price filter parameter.
- Minimum range is applied client-side because the backend does not expose a range filter parameter.

## UI Behavior

The home screen renders map markers and the vehicle list from `HomeState.filteredVehicles`.
Changing the vehicle type reloads vehicles from the repository. Changing price or range updates the local state only.

When the active filters hide the selected vehicle, the ViewModel selects the first visible vehicle, or clears the selection when there is no visible vehicle.

## Demo Mode

`FakeVehicleRepository` contains several vehicle types, prices, and ranges so filter behavior can be tested without the backend.
