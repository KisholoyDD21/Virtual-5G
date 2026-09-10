# Architecture

## Android: Clean Architecture, five Gradle modules

```
        app  (application module, DI composition root, MainActivity)
       /  |  \
      /   |   \
presentation data  (both depend on domain + core)
      \   |   /
       \  |  /
       domain + core  (leaf modules, no dependency on each other)
```

- **`:domain`** - pure Kotlin/JVM, zero Android dependency. Entities, repository
  *interfaces*, use cases, and the two engines (`NetworkQualityEngine`,
  `OperatingModeSelector`) that decide the score and the mode. Because this module
  can't touch `Context` or any Android framework class, its logic is trivially unit
  testable without Robolectric or a device - see `domain/src/test/`.
- **`:core`** - Android library. Cross-cutting utilities with no feature knowledge:
  permission checks, a coroutine dispatcher provider (for testability), and a
  logger that redacts known-sensitive field names before anything reaches Logcat.
- **`:data`** - Android library. Implements every `:domain` repository interface
  using real Android APIs (`TelephonyManager`, `ConnectivityManager`, OkHttp, Room,
  WorkManager, Keystore-backed prefs). This is the only module that imports
  `android.telephony.*`.
- **`:presentation`** - Android library. Jetpack Compose screens and ViewModels.
  Depends only on `:domain` (interfaces/use cases) and `:core` (utils) - it has
  never heard of `:data`, enforced by the `NavGraphDependencies` interface it
  defines and `:app` implements.
- **`:app`** - the application module. The only place that knows about every other
  module. Contains `AppContainer`, the composition root.

## Why manual DI instead of Hilt

`AppContainer` (`app/src/main/java/com/virtual5g/app/di/AppContainer.kt`) is a plain
Kotlin class that constructs every repository, use case, and `ViewModelProvider.Factory`
in dependency order, then implements `NavGraphDependencies` so `:presentation` can
reach them without knowing their concrete types. This is a deliberate choice for
this codebase, not a simplification of the architecture itself: everything is still
built against interfaces, so introducing Hilt or Koin later is a mechanical change
confined to this one file plus a handful of `@HiltViewModel` annotations - nothing
in `:domain`, `:data`, or `:presentation` would need to change.

## The Virtual5GEngine, concretely

The spec's `Virtual5GEngine` maps to `GetVirtual5GStateUseCase`
(`domain/usecase/GetVirtual5GStateUseCase.kt`), which:

1. Reads device capability once (`DeviceRepository`).
2. Collects a live `Flow<NetworkMetrics>` (`NetworkRepository`, backed by
   `ConnectivityObserver` + `CellularTelephonyObserver` + `LightweightLatencyProbe`
   combined in `:data`).
3. Keeps a rolling 10-sample window so `NetworkQualityEngine` can score stability.
4. Asks `OperatingModeSelector` for the mode and its human-readable explanation.
5. Emits one `Virtual5GState` per change - this is the single object every screen
   renders from.

## Backend

FastAPI + SQLAlchemy, five routers (`auth`, `plan`, `speedtest`, `telemetry`,
`health`), each a thin layer over the models. `CarrierProvider` is an abstract base
class mirroring the exact same interface shape as the Android side, so both
platforms would swap in a real carrier integration the same way. SQLite is the local
dev default (zero setup); `docker-compose.yml` runs it against Postgres + Redis for
anything closer to production.
