# AGENTS.md

Guidance for AI coding agents working in this repository. (Claude Code reads `CLAUDE.md`, which mirrors this file — keep the two in sync when editing either.)

## Project overview

TuCargo is a motorcycle-logistics marketplace. **Clients** create shipments, verified **drivers** accept and deliver them with live location tracking, and an **admin** role reviews driver KYC documents. The mobile app is **Kotlin Multiplatform + Compose Multiplatform** (Android & iOS); the backend is **Firebase** (Auth, Firestore, Storage, Realtime Database, Cloud Functions) accessed from shared Kotlin via the **GitLive** SDK.

## Modules

- **`composeApp/`** — the KMP module containing essentially all app code (UI + domain + data), with `commonMain`, `androidMain`, and `iosMain` source sets. Package root: `com.juanpablo0612.tucargo`.
- **`androidApp/`** — Android application entry point (`MainActivity`, `TuCargoApplication`). Thin; depends on `:composeApp`.
- **`iosApp/`** — Xcode project. Firebase + Koin are initialized from Kotlin in `MainViewController.kt`, so no Swift wiring is needed beyond adding the Firebase SPM packages.
- **`functions/`** — Firebase Cloud Functions (TypeScript, Node 22, `firebase-functions` v7). This is a **separate npm project**, not part of the Gradle build.
- Root `*.rules` / `*.indexes.json` / `firebase.json` / `.firebaserc` — Firebase deployment + server-side authorization. Default project: `tucargo-b6e0e`.

## Common commands

```shell
# Android app (debug APK)
./gradlew :androidApp:assembleDebug

# Shared unit tests (aggregate across all KMP targets) — this is what CI runs
./gradlew :composeApp:allTests

# A single test: run the target's test task with a class/method filter, e.g.
./gradlew :composeApp:iosSimulatorArm64Test --tests "com.juanpablo0612.tucargo.SomeTest"

# Cloud Functions (run inside functions/)
npm --prefix functions install
npm --prefix functions run lint     # eslint (also runs as a deploy predeploy step)
npm --prefix functions run build    # tsc -> functions/lib

# Deploy server-side pieces (requires firebase-tools + firebase login)
firebase deploy --only firestore:rules,firestore:indexes,storage
firebase deploy --only functions

# Local emulators (auth/firestore/functions/storage/database + UI)
firebase emulators:start

# iOS: register the Google Maps SPM package once per checkout (run on a Mac)
XCODEPROJ_PATH='<path>/iosApp/iosApp.xcodeproj' ./gradlew :composeApp:integrateLinkagePackage
```

### Local config required to build (all gitignored)

- **`secrets.properties`** at the repo root — `GOOGLE_MAPS_API_KEY` (Android) and `GOOGLE_MAPS_IOS_API_KEY` (iOS), surfaced to Kotlin via **BuildKonfig**. A blank Android key only *warns* in debug but **fails release builds** (see `androidApp/build.gradle.kts`).
- **`androidApp/google-services.json`** — Firebase Android config (CI injects it from the `GOOGLE_SERVICES_JSON` secret).
- **`iosApp/iosApp/GoogleService-Info.plist`** — Firebase iOS config.
- The Cloud Functions quoting/dispatch uses a server-side `GOOGLE_MAPS_SERVER_KEY` Firebase secret (`firebase functions:secrets:set GOOGLE_MAPS_SERVER_KEY`), distinct from the client map keys.

Toolchain: JVM target **17** (Java 21 in CI), Android `compileSdk`/`targetSdk` **37**, `minSdk` **24**. Versions are centralized in `gradle/libs.versions.toml`; type-safe project accessors are enabled (`projects.composeApp`).

## Architecture

### Layering

Code in `commonMain` is organized into four layers; dependencies point downward only:

`features/` (presentation) → `domain/usecase/` → `data/` (repositories) → Firebase (GitLive SDK)

- **`domain/model/`** — flat package of plain domain models (`Trip`, `User`, `KycDocument`, `AppError`, pricing/quote/offer types). Money is modeled as **`Int`** (Colombian pesos, COP) — never floats.
- **`domain/usecase/`** — one tiny class per action, sub-packaged by concern (`auth`, `user`, `trip`, `quote`, `tracking`, `document`, `admin`). Each exposes `operator fun invoke(...)` and just delegates to a repository (see `domain/usecase/trip/AcceptOfferUseCase.kt`). ViewModels depend on use cases, not repositories.
- **`data/`** — repositories grouped by entity (`auth`, `user`, `trip`, `quote`, `tracking`, `document`, `config`). Each is an `interface` + `…Impl`. Repositories talk to Firestore/Storage/Functions **directly** via the GitLive SDK; only `auth`/`user` keep dedicated `RemoteDataSource` classes.
- **`core/`** — cross-cutting helpers: `validation/`, `ui/` (theme, reusable components, `LocalDimensions`/`ResponsiveContainer` responsive design), `location/`, `service/`, `permissions/`, `fcm/`, `coroutines/AppDispatchers`, `logging/`, `time/`.

### Feature package convention (MVI-ish)

Each screen lives in its own `features/<area>/<feature>` package with five files — **`features/auth/login` is the canonical reference**:

- `XState.kt` — `@Immutable data class` of UI state.
- `XAction.kt` — `sealed interface` of user intents.
- `XError.kt` — feature-local error enum/sealed type (mapped from `AppError`).
- `XViewModel.kt` — `androidx.lifecycle.ViewModel`; holds `MutableStateFlow<XState>` exposed as `StateFlow`, a single `onAction(action)` dispatcher, and raw Compose `TextFieldState`s for inputs. It calls use cases and `fold`s the returned `Result`.
- `XScreen.kt` — the `@Composable`.

Two intentional exceptions: **`features/client/quote`** is a multi-step wizard (`PickLocationScreen` → `CargoScreen` → `QuoteScreen`) sharing a single `TripRequestViewModel` across its screens, and **`features/driver/offer/OfferScreen.kt`** is a stateless dialog component. Neither needs the full file set.

### Dependency injection (Koin)

`di/` splits modules by concern: `DataModule` (Firebase singletons + repositories), `DomainModule` (use cases), `ViewModelModule` (ViewModels). `AppModule.kt` aggregates them and exposes `initKoin(...)`. `PlatformModule` is an **`expect val`** with `actual` implementations per platform (e.g. Android binds Room-backed `LocationBuffer` + foreground-service controller; iOS binds in-memory equivalents). Android starts Koin in `TuCargoApplication`; iOS in `MainViewController`.

### Navigation

`navigation/AppNavigation.kt` is the single nav graph. Routes are **type-safe** `@Serializable` objects/classes nested under a `sealed class Route`. Key behaviors:

- An `AuthViewModel` drives an `authState` flow; the **start destination is chosen by role** (`ADMIN → AdminHome`, `CLIENT → ClientHome`, `DRIVER → DriverHome` or the appropriate onboarding step based on `DriverOnboardingStatus`). Logout pops the whole back stack to `Login`.
- The trip-request wizard is a **nested `navigation<Route.TripRequestGraph>`** graph; its screens share one `TripRequestViewModel` by scoping `koinViewModel(viewModelStoreOwner = parentEntry)` to the graph's back-stack entry.

### Error handling

Repository methods return **`kotlin.Result<T>`**, never throw to callers. The convention (`data/common/Exceptions.kt`):

- Wrap work in `safeCall { … }`, which catches exceptions, **re-throws `CancellationException`**, and maps everything else through `ExceptionMapper` into the **`AppError`** sealed hierarchy (`domain/model/AppError.kt`: `Auth`, `Validation`, `Trip`, `Driver`, `Network`, `DataCorruption`, `Unknown`).
- Inside `safeCall`, throwing a specific `AppError` (e.g. `AppError.Trip.AlreadyTaken`) is how repositories signal typed failures — including parsing Cloud Function error codes out of the response/message (see `TripRepositoryImpl.requestTrip`/`completeTrip`).
- ViewModels `fold` the `Result` and translate `AppError` into their feature-local `XError`.

### Data layer & serialization

Firestore documents use **snake_case** field names. Each entity has a `…Dto` (`@Serializable`, fields mapped with `@SerialName("snake_case")`) plus a `…Mapper` with `toDomain()` / `toDto()` extensions; repositories convert at the boundary and never expose DTOs. Reads use GitLive's `.data<Dto>()`, real-time listeners use `.snapshots.map { … }`. **Monetary fields are stored as integers** (no floats).

### Trip lifecycle & dispatch (the most important cross-file flow)

The trip status machine is defined **once** in `domain/trip/TripTransitions.kt` and **mirrored exactly** by the `/trips` update rules in `firestore.rules` — keep both in sync. Canonical lifecycle:

```
REQUESTED → OFFERED → ACCEPTED → AT_PICKUP → IN_TRANSIT → AT_DROPOFF → COMPLETED
            (+ CANCELLED_NO_DRIVER / CANCELLED_CLIENT / CANCELLED_DRIVER / CANCELLED_ADMIN)
```

Dispatch is **server-authoritative** and runs as an automatic offer loop, *not* a "list of available trips the driver browses":

1. Client gets a price (`createQuote` callable) → quote stored in `quotes`.
2. Client calls **`requestTrip`** (callable) which validates/consumes the quote in a transaction, generates a 4-digit `delivery_code`, and creates the trip as `REQUESTED`.
3. The **`onTripCreate`** Firestore trigger fires **`dispatchTrip`**, which takes a `dispatch_locks/{tripId}` lease, finds the nearest online/available verified driver (haversine), writes a `trip_offers` doc, sets the trip to `OFFERED`, sends an FCM `TRIP_OFFER` data message, and **polls** the offer for up to ~30s. On reject/expire it advances to the next-nearest driver, up to `max_dispatch_attempts`, else `CANCELLED_NO_DRIVER`.
4. The driver app receives the offer via FCM → `OfferEventBus` (or the `observeActiveOffer` Firestore listener) → shows `OfferScreen`. Accept calls **`acceptOffer`** (transactional → `ACCEPTED`); reject calls `rejectOffer`.
5. Driver advances the lifecycle. Intermediate hops (`AT_PICKUP`/`IN_TRANSIT`/`AT_DROPOFF`) go through the **`updateTripStatus`** callable; `COMPLETED` goes through **`completeTrip`**, which validates the `delivery_code` server-side and enforces attempt limits.

Note: `TripRepositoryImpl` still contains an older client-side `acceptTrip`/`createTrip` path, but the rules now set `allow create: if false` on `trips` and `quotes`, so the **Cloud Function path is authoritative**.

### Cloud Functions (`functions/src/`)

`index.ts` initializes admin SDK (`maxInstances: 10`) and re-exports each function. Two kinds:

- **Callable** (`onCall`, client-invoked, often role-gated via `request.auth.token.role` and transactional): `requestTrip`, `acceptOffer`, `rejectOffer`, `completeTrip`, `updateTripStatus`, `approveDriver`, `createQuote`.
- **Triggers / scheduled**: `onTripCreate` (→ dispatch), `onUserDocumentCreated`, `cleanStaleDriverLocations`. `dispatchTrip` and `pricing` are internal helpers, not exported functions.

`firebase.json` runs `npm run lint` and `npm run build` as **predeploy** steps, so functions must lint-clean and compile before they deploy. ESLint config is Google style: **double quotes, 2-space indent** (`functions/.eslintrc.js`).

### Configuration documents (gotcha)

There are **two** Firestore config documents with different consumers — do not conflate them:

- **`config/system`** — read by the **client app** (`ConfigRepositoryImpl`, `SystemConfig`). Reads fail loudly if absent. The README's "Seed the pricing config" step documents this doc's fields.
- **`config/app_config`** — read by the **Cloud Functions** (`createQuote`, `acceptOffer`, `dispatchTrip`) for `base_fare`, `per_km_fare`, `commission_rate`, `commission_floor`, `max_dispatch_attempts`. This is the authoritative pricing/dispatch config for the server flow.

`config/*` is world-readable to signed-in users but **`allow write: if false`** — only editable from the Firebase console / admin SDK.

### Location tracking pipeline

`core/location/LocationProvider` is an `expect`/`actual` GPS source (Android = `AndroidLocationProvider` via fused location; iOS = `IosLocationProvider`, **currently a stub** that reports unavailable). `domain/trip/TripTracker` samples the location flow (~4s), maps to `DriverLocation`, and pushes via `SendLocationUseCase`. Persistence buffering is platform-specific: Android uses a **Room** database (`data/tracking/room/…`, `RoomLocationBuffer`, KSP-generated) drained by a `DriverLocationService` **foreground service**; iOS uses `InMemoryLocationBuffer`. FCM offer/cancel events are bridged into common code through the `core/fcm/OfferEventBus` singleton, fed by `TuCargoFirebaseMessagingService` on Android.

### expect/actual platform code

When adding platform behavior, follow the existing seams: declare an `expect` in `commonMain` and provide `actual` in both `androidMain` and `iosMain`. Existing pairs: `di/PlatformModule`, `core/logging/Logger` (`logError`), `core/permissions/LocationPermission`, `core/location/LocationProvider`, `core/service/LocationServiceController`, `core/time/TimeUtils`, `core/ui/components/MapComponent`, `data/document/StorageDataExt`.

## Security model & invariants to keep in sync

**All authorization is enforced in `firestore.rules` / `storage.rules`; client-side checks are UX only.** When changing related code, update the rules in the same change:

- **`TripTransitions.kt` ↔ `firestore.rules`** trip `update` block — the allowed status hops must match. `COMPLETED` is deliberately *excluded* from the rules (must go through `completeTrip`); drivers may only advance one hop at a time or stream `driver_last_lat/lng`; clients may only cancel before pickup.
- **KYC upload limits** — `storage.rules` enforces owner-only, `image/*`, `< 5 MB`; the client-side checks in `DocumentRepositoryImpl` must match. KYC docs live at `users/{uid}/kyc_documents/{type}`; status changes are admin-only.
- The rules forbid creating `ADMIN` accounts from the app and forbid non-admin writes to `is_verified`, `status`, `role`, `wallet_balance`, ratings, and KYC statuses. Admin is assigned **only** by editing the user doc's `role` to `"ADMIN"` in the Firebase console.
- `quotes`, `trip_offers`, `dispatch_locks`, `audit_log` are **Cloud-Function-only** (no direct client writes); drivers can read `REQUESTED` trips and their own offers only.

Firestore composite indexes live in `firestore.indexes.json` (queries on `trips` by `client_id`/`status`/`driver_id` + `created_at`, and `trip_offers`); add an index there when introducing a new compound query.

## Notes / known gotchas

- The **README's "Security model" section uses outdated status names** (`SEARCHING/ASSIGNED/ON_WAY/...`). The authoritative lifecycle is `TripTransitions.kt` + `firestore.rules` (`REQUESTED/OFFERED/ACCEPTED/AT_PICKUP/IN_TRANSIT/AT_DROPOFF/COMPLETED`).
- **iOS GPS is a stub** — `IosLocationProvider` reports denied, so location-dependent driver UI shows its unavailable states. Maps on iOS use the Google Maps SDK for iOS via Kotlin's direct Swift Package Manager integration (`swiftPMDependencies` in `composeApp/build.gradle.kts`), no CocoaPods.
- There is no `commonTest` source set yet; `:composeApp:allTests` currently has no tests to run but is wired into CI for when they are added.
- UI strings exist in Spanish and English (`composeResources/values` and `values-es`); user-facing copy is Spanish-first.
</content>
