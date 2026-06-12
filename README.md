# TuCargo

Motorcycle-logistics marketplace built with Kotlin Multiplatform + Compose Multiplatform (Android & iOS). Clients create shipments, verified drivers accept and deliver them with live location tracking, and an in-app admin role reviews driver KYC documents.

| Role | What they can do |
|------|------------------|
| `CLIENT` | Create shipments (price quoted from operator config), track the driver live, cancel before pickup, view history. |
| `DRIVER` | Register a vehicle, upload 6 KYC documents, go online, accept available trips, advance the trip lifecycle, complete with the recipient's delivery code. |
| `ADMIN` | Review pending drivers, approve/reject each document (with a reason), verify drivers. Assigned **only** via the Firebase console (see runbook). |

## Project layout

- `composeApp/` — shared code (UI + domain + data).
  - `commonMain/.../features/*` — presentation, one package per feature with `State` / `Action` / `Error` / `ViewModel` / `Screen` files (the `features/auth/presentation/login` package is the reference pattern).
  - `commonMain/.../domain` — models, use cases, `domain/trip/TripTransitions.kt` (the trip status machine — **keep in sync with `firestore.rules`**).
  - `commonMain/.../data` — repositories. Convention: repositories may talk to Firestore/Storage directly (via the GitLive SDK); only `auth`/`user` keep dedicated remote data sources. Firestore field names are snake_case via `@SerialName`.
  - `androidMain` / `iosMain` — `expect`/`actual` implementations (maps, GPS, permissions, logging).
- `androidApp/` — Android entry point.
- `iosApp/` — iOS entry point (Xcode project).
- `firestore.rules`, `storage.rules`, `firestore.indexes.json`, `firebase.json` — server-side authorization (see Security).

## Setup

### 1. Firebase project

1. Create a Firebase project with **Authentication (email/password)**, **Cloud Firestore**, and **Storage** enabled.
2. Android: download `google-services.json` into `androidApp/` (gitignored).
3. iOS: add `GoogleService-Info.plist` to `iosApp/iosApp` in Xcode (gitignored) and add the [Firebase iOS SDK](https://github.com/firebase/firebase-ios-sdk) Swift package (FirebaseAuth, FirebaseFirestore, FirebaseStorage) to the `iosApp` target. Firebase + Koin are initialized from Kotlin in `MainViewController` — no Swift code changes needed.

### 2. Deploy the security rules and indexes (required)

Without these, the database denies nothing — never run the app against a project without them:

```shell
npm install -g firebase-tools
firebase login
firebase deploy --only firestore:rules,firestore:indexes,storage
```

### 3. Seed the pricing config (required)

Trip prices are quoted from `config/system`; trip creation fails loudly if it is missing. Create the document in the Firestore console:

```
config/system {
  base_price: 35000          // COP, charged on every trip
  base_km_included: 1.0      // km covered by the base price
  price_per_km: 5000         // per km beyond the included distance
  commission_percentage: 0.15
  min_wallet_balance: 5000
  android_version_min: "1.0.0"
  maintenance_mode: false
}
```

### 4. Google Maps key (Android)

Create `secrets.properties` at the repo root (gitignored):

```properties
GOOGLE_MAPS_API_KEY=your-key
```

Restrict the key in Google Cloud Console to the Android app's package name + SHA-1. Debug builds warn if it is missing; **release builds fail**.

### 5. Build & run

```shell
./gradlew :androidApp:assembleDebug     # Android
./gradlew :composeApp:allTests          # unit tests
```

iOS: open `iosApp/` in Xcode and run (maps and GPS are not implemented on iOS yet — the UI degrades gracefully).

## Admin runbook

1. Register a normal account in the app.
2. In the Firestore console, edit that user's document: set `role` to `"ADMIN"`.
3. Sign in again — the app routes to the driver-verification dashboard.
4. Open a pending driver, view each document, approve or reject (rejections require a reason shown to the driver; the driver re-uploads and the document returns to `PENDING`).
5. When all six documents are `APPROVED`, tap **Verify driver**. The driver's `KycPending` screen reacts immediately and unlocks the driver home.

The rules forbid creating `ADMIN` accounts from the app and forbid non-admin writes to `is_verified`, KYC statuses, `role`, `wallet_balance`, account `status`, and ratings.

## Security model

- All authorization is enforced in `firestore.rules` / `storage.rules`; client-side checks are UX only.
- Trip lifecycle: `SEARCHING → ASSIGNED → ON_WAY → ARRIVED_PICKUP → IN_PROGRESS → COMPLETED`, plus client cancellation before pickup. Accepting is atomic — two racing drivers can't both win (transaction + rules).
- Drivers can only write their own location, and only on their own active trip.
- KYC documents live in `users/{uid}/kyc_documents/{type}`; status changes are admin-only; uploads are owner-only, image/*, < 5 MB.
- The trip's `price_total` must equal `price_base + price_distance` and be positive at creation; the formula inputs come from `config/system` (read-only to clients).

## Testing

```shell
./gradlew :composeApp:allTests
```

Manual end-to-end check against the Firebase emulator: `firebase emulators:start`, then exercise two drivers accepting the same trip (exactly one must win), a non-admin attempting to write `is_verified` (must be denied), and a >5 MB or non-image KYC upload (must be rejected).

## Known limitations / backlog

- **iOS**: GPS and maps are stubs (`IosLocationProvider`, iOS `MapComponent`); the permission requester reports denied so the UI shows its unavailable states. Real CoreLocation/MapKit integration is pending.
- **Distance** is straight-line (haversine); a routing API would price by road distance.
- **Delivery code** is verified in the driver's UI only — the driver can read the trip document, so true enforcement needs a Cloud Function.
- **Wallet**: balance is read-only; top-ups/withdrawals need a payment provider integration.
- **Email verification** at registration (Firebase `sendEmailVerification` + an `email_verified` gate in the rules) is not implemented yet.
- Rules unit tests via `@firebase/rules-unit-testing`; push notifications; ratings/reviews; profile editing.
