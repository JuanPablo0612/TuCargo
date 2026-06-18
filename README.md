# TuCargo

Motorcycle-logistics marketplace built with Kotlin Multiplatform + Compose Multiplatform (Android & iOS). Clients create shipments, verified drivers accept and deliver them with live location tracking, and an in-app admin role reviews driver KYC documents.

| Role | What they can do |
|------|------------------|
| `CLIENT` | Create shipments (price quoted from operator config), track the driver live, cancel before pickup, view history. |
| `DRIVER` | Register a vehicle, upload 6 KYC documents, go online, receive automatic trip offers (nearest-driver dispatch), accept/reject them, advance the trip lifecycle, complete delivery with the recipient's 4-digit code. |
| `ADMIN` | Review pending drivers, approve/reject each document (with a reason), verify drivers. Assigned **only** via the Firebase console (see runbook). |

## Project layout

- `composeApp/` — shared code (UI + domain + data).
  - `commonMain/.../features/*` — presentation, one package per feature with `State` / `Action` / `Error` / `ViewModel` / `Screen` files (the `features/auth/login` package is the reference pattern). `features/client/quote` is an intentional exception — a multi-step wizard sharing one `TripRequestViewModel` across its screens — and `features/driver/offer/OfferScreen.kt` is a stateless dialog/component, not an independent screen; neither needs the full file set.
  - `commonMain/.../domain` — `model/` (flat), `usecase/` (subpackaged by domain concern: `auth`, `user`, `trip`, `quote`, `tracking`, `document`, `admin`), `domain/trip/TripTransitions.kt` (the trip status machine — **keep in sync with `firestore.rules`**).
  - `commonMain/.../data` — repositories, organized by entity (`auth`, `user`, `trip`, `quote`, `tracking`, `document`, `config`). Convention: repositories may talk to Firestore/Storage directly (via the GitLive SDK); only `auth`/`user` keep dedicated remote data sources. Firestore field names are snake_case via `@SerialName`.
  - `commonMain/.../di` — Koin modules split by concern: `DataModule.kt`, `DomainModule.kt`, `ViewModelModule.kt`, `AppModule.kt` (aggregates the others + `initKoin`).
  - `androidMain` / `iosMain` — `expect`/`actual` implementations (maps, GPS, permissions, logging).
- `androidApp/` — Android entry point.
- `iosApp/` — iOS entry point (Xcode project).
- `functions/` — Firebase Cloud Functions (TypeScript, Node 22, firebase-functions v7) — a **separate npm project**, not part of the Gradle build. The server-authoritative trip flow lives here: `createQuote`, `requestTrip`, `dispatchTrip`/`onTripCreate`, `acceptOffer`/`rejectOffer`, `updateTripStatus`, `completeTrip`, `approveDriver`.
- `firestore.rules`, `storage.rules`, `database.rules.json`, `firestore.indexes.json`, `firebase.json` — server-side authorization and deploy config (see Security).

## Setup

### 1. Firebase project

1. Create a Firebase project with **Authentication (email/password)**, **Cloud Firestore**, **Storage**, **Realtime Database** (live location), and **Cloud Functions** (Blaze plan) enabled.
2. Android: download `google-services.json` into `androidApp/` (gitignored).
3. iOS: add `GoogleService-Info.plist` to `iosApp/iosApp` in Xcode (gitignored) and add the [Firebase iOS SDK](https://github.com/firebase/firebase-ios-sdk) Swift package (FirebaseAuth, FirebaseFirestore, FirebaseStorage, FirebaseDatabase) to the `iosApp` target. Firebase + Koin are initialized from Kotlin in `MainViewController` — no Swift code changes needed.

### 2. Deploy the server-side pieces (required)

Authorization **and the entire trip flow** live on the server — never run the app against a project without them. The lifecycle is server-authoritative: quoting, dispatch, offer accept/reject, status hops, and delivery-code verification all run as Cloud Functions, so the app cannot create or accept trips until they are deployed.

```shell
npm install -g firebase-tools
firebase login

# Security rules + indexes (Firestore, Storage, Realtime Database)
firebase deploy --only firestore:rules,firestore:indexes,storage,database

# Cloud Functions (separate npm project; predeploy lints + compiles)
npm --prefix functions install
firebase deploy --only functions
```

The functions also need the `GOOGLE_MAPS_SERVER_KEY` secret set (see Google Maps keys below) before `createQuote` will work.

### 3. Seed the system config (required)

`config/system` is the single source of truth for pricing and dispatch, read by **both the app and the Cloud Functions** (quoting, the accept-offer wallet gate, and dispatch retries). Reads fail loudly if it is missing. Create the document in the Firestore console:

```
config/system {
  base_price: 35000           // COP, charged on every trip
  base_km_included: 1.0       // km covered by the base price
  price_per_km: 5000          // per km beyond the included distance
  commission_percentage: 0.15 // driver commission, as a fraction
  min_wallet_balance: 5000    // COP a driver needs to accept an offer
  max_dispatch_attempts: 5    // nearest-driver offers tried before CANCELLED_NO_DRIVER
  android_version_min: "1.0.0"
  maintenance_mode: false
}
```

The doc is world-readable to signed-in users but write-locked in the rules — edit it only from the Firebase console / admin SDK.

### 4. Google Maps keys

Create `secrets.properties` at the repo root (gitignored):

```properties
GOOGLE_MAPS_API_KEY=your-android-key
GOOGLE_MAPS_IOS_API_KEY=your-ios-key
```

`GOOGLE_MAPS_API_KEY` is consumed by the Android app (Google Maps Compose); restrict it in Google Cloud Console to the Android app's package name + SHA-1. Debug builds warn if it is missing; **release builds fail**.

`GOOGLE_MAPS_IOS_API_KEY` is consumed by the iOS app (Google Maps SDK for iOS, via `BuildKonfig`); restrict it to the iOS app's bundle ID instead — the Android key won't work there.

The backend quoting function uses a separate, server-side `GOOGLE_MAPS_SERVER_KEY` Firebase secret (Google Routes API) — set it once per project:

```shell
firebase functions:secrets:set GOOGLE_MAPS_SERVER_KEY
```

### 5. Build & run

```shell
./gradlew :androidApp:assembleDebug     # Android
./gradlew :composeApp:allTests          # unit tests
```

iOS: the Google Maps SDK for iOS is added via Kotlin's direct Swift Package Manager integration (`swiftPMDependencies` in `composeApp/build.gradle.kts`). On a Mac, once per checkout (or whenever that dependency changes), run:

```shell
XCODEPROJ_PATH='<path-to>/iosApp/iosApp.xcodeproj' ./gradlew :composeApp:integrateLinkagePackage
```

then open `iosApp/` in Xcode and run. GPS is still a stub on iOS (see Known limitations).

## Admin runbook

1. Register a normal account in the app.
2. In the Firestore console, edit that user's document: set `role` to `"ADMIN"`.
3. Sign in again — the app routes to the driver-verification dashboard.
4. Open a pending driver, view each document, approve or reject (rejections require a reason shown to the driver; the driver re-uploads and the document returns to `PENDING`).
5. When all six documents are `APPROVED`, tap **Verify driver**. The driver's `KycPending` screen reacts immediately and unlocks the driver home.

The rules forbid creating `ADMIN` accounts from the app and forbid non-admin writes to `is_verified`, KYC statuses, `role`, `wallet_balance`, account `status`, and ratings.

## Security model

- All authorization is enforced in `firestore.rules` / `storage.rules` / `database.rules.json`; client-side checks are UX only. Direct client `create` on `trips` and `quotes` is denied — those go through Cloud Functions.
- The trip flow is **server-authoritative**: `createQuote` → `requestTrip` (creates the trip `REQUESTED` + a 4-digit delivery code) → `dispatchTrip` (offers it to the nearest online verified driver, retrying on reject/timeout up to `max_dispatch_attempts`) → `acceptOffer` (transactional, so an offer can't be double-accepted) → `updateTripStatus` per hop → `completeTrip` (verifies the delivery code **server-side** with attempt limits).
- Lifecycle: `REQUESTED → OFFERED → ACCEPTED → AT_PICKUP → IN_TRANSIT → AT_DROPOFF → COMPLETED`, plus `CANCELLED_*` (no-driver / client / driver / admin). The allowed hops live once in `domain/trip/TripTransitions.kt` and are mirrored by the `/trips` update rules — keep both in sync. Clients may only cancel before pickup; drivers advance one hop at a time; `COMPLETED` is reachable only via `completeTrip`.
- Live location streams to **Realtime Database** at `driver_locations/{driverId}` for the client map; a throttled `last_lat`/`last_lng` on the driver's user doc separately feeds dispatch matching.
- KYC documents live in `users/{uid}/kyc_documents/{type}`; status changes are admin-only; uploads are owner-only, image/*, < 5 MB.
- `quotes`, `trip_offers`, and `dispatch_locks` are Cloud-Function-only (no direct client writes). The rules forbid creating `ADMIN` accounts from the app and forbid non-admin writes to `is_verified`, `status`, `role`, `wallet_balance`, ratings, and KYC statuses.

## Testing

```shell
./gradlew :composeApp:allTests
```

This aggregates shared unit tests across the KMP targets and is what CI runs. There are currently no `commonTest` cases — the task is wired up for when they are added.

Manual end-to-end check against the Firebase emulator (`firebase emulators:start`): request a trip as a client and confirm the dispatch offer reaches a driver and can be accepted; that rejecting or letting an offer time out re-dispatches to the next driver; that completing with a wrong delivery code is rejected; that a non-admin write to `is_verified` is denied; and that a >5 MB or non-image KYC upload is rejected.

## Known limitations / backlog

- **iOS**: GPS is still a stub (`IosLocationProvider`); the permission requester reports denied so the UI shows its unavailable states. Real CoreLocation integration is pending. Maps are implemented via the Google Maps SDK for iOS (see Setup step 4/5).
- **Driver matching** for dispatch uses straight-line (haversine) distance; trip *pricing* already uses road distance from the Google Routes API.
- **Wallet**: balance is read-only; top-ups/withdrawals need a payment provider integration.
- **Email verification** at registration (Firebase `sendEmailVerification` + an `email_verified` gate in the rules) is not implemented yet.
- Rules unit tests via `@firebase/rules-unit-testing`; ratings/reviews; profile editing.
