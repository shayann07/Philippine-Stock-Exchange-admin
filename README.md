# Philippine-Stock-Exchange-admin

A native Kotlin/Java Android admin panel for a Firebase-backed investment / MLM platform — `applicationId = com.codingempire.adminpse`. Despite the repository name, this is **not** a stock-exchange tool: there is no order book, listings, or trade oversight. Admins sign in with Firebase Auth (gated against an `Admin` Firestore collection), then create users, set deposits, edit investment plans, approve/reject withdrawal requests, broadcast announcements, and chat 1:1 with platform users. Push goes out via the FCM v1 HTTP API. MVVM + Jetpack Navigation + Room v4.

> **Heads-up:** the previous README described listings management, order-book oversight, REST + WebSockets, and an MIT licence — none of which exist in the code. Multiple secrets are committed in source. See [Security Notice](#-security-notice--read-first) before doing anything else.

## ⚠ Security Notice — Read first

Three credentials are committed to git history (`bc86110` initial commit, public repo):

1. **Firebase Admin service-account JSON** is inlined as a Kotlin string in `app/src/main/java/com/codingempire/adminpse/notifications/AccessToken.kt:27-42`:
   - `client_email = REDACTED_CLIENT_EMAIL`
   - `private_key_id = REDACTED_KEY_ID`
   - Full RSA `private_key` (`REDACTED_KEY_START … REDACTED_KEY_END`)

   The app uses `GoogleCredentials.fromStream(...)` with the `firebase.messaging` scope to mint OAuth tokens client-side and call FCM v1 directly from the device. Anyone with the APK can extract the key. **Rotate immediately** in IAM for project `philippine-stock-exchang-296cd`, purge from git history, and move FCM sending to a Cloud Function.

2. **CoinPayments public + private keys** are hardcoded in `app/src/main/java/com/codingempire/adminpse/fragments/HomeFragment.kt:45-47`:

   ```kotlin
   private val publicKey  = "REDACTED_COINPAYMENTS_PUBLIC_KEY"
   private val privateKey = "REDACTED_COINPAYMENTS_PRIVATE_KEY"
   private val apiUrl     = "https://www.coinpayments.net/api.php"
   ```

   These permit cryptocurrency operations on the merchant account. **Rotate at CoinPayments** and route through a backend.

3. **`app/google-services.json` is tracked.** Project `philippine-stock-exchang-db`, project number `324672116485`, web API key `REDACTED_API_KEY`. (Web API keys are not strictly secret, but combined with the missing App Check the surface is wide open.)

Note: the FCM endpoint in `notifications/Fcm.kt:15` targets project `philippine-stock-exchang-db`, while the embedded service account in `AccessToken.kt` is for `philippine-stock-exchang-296cd`. Two **different** Firebase projects — the OAuth token is invalid for the FCM call, so push sending from the device would 403 today.

There is no `.gitignore`. `local.properties`, `.gradle/`, `.idea/`, and `build/` are also tracked.

## Status

- Working tree clean on `master`. Only **two commits ever**: `bc86110` "Initial Commit!" and `9275c89` "Create README.md".
- Remote: `https://github.com/shayann07/Philippine-Stock-Exchange-admin.git`.
- This README was rewritten from a code audit; the previous one's listings / order-book / trade-oversight / REST + WebSockets / MIT claims are fiction.

## How it works

### Authentication

`ui/LoginActivity` runs Firebase Auth `signInWithEmailAndPassword`, then queries the `Admin` Firestore collection by uid; on success it persists the FCM device token to the admin's Firestore doc and starts `MainActivity`. Standalone activities `SignUpActivity`, `ForgotPasswordActivity`, `ResetPasswordActivity`, and `LauncherActivity` cover sign-up and recovery.

### Single Activity + Navigation

`ui/MainActivity` hosts a `NavHostFragment` driven by `res/navigation/nav_graph.xml`. Nineteen fragments under `fragments/` cover every admin destination:

- **Dashboard** — `HomeFragment` (active / inactive user counts, balance summary).
- **Users** — `ActiveUsersFragment`, `FragmentUser`, `UsersWithBalanceFragment`, `create_user/CreateUserFragment`, `create_user/DepositFragment`.
- **Withdrawals** — `withdraw/WithdrawalRequestsFragment` (tab container) over `AllWithdrawalsRequestFragment` / `ApprovedWithdrawalsFragment` / `RejectedWithdrawalsFragment`.
- **Plans** — `investment_plans/PlansByCategoryFragment`, `investment_plans/PlanDetailFragment`, `plan_setting/PlanSettingFragment`, `plan_setting/EditPlanSettingFragment`.
- **Chat** — `chat/ChatFragment`, `chat/DetailChatFragment`.
- **Announcements & notifications** — `AnnouncementsFragment`, `AddPosterFragment`, `NotificationFragment`.

### MVVM + Repository + Room

ViewModels (`ViewModel/UserViewModel`, `PlanViewModel`, `WithdrawViewModel`, `ChatViewModel`) drive fragments via LiveData / Flow. Repositories (`repository/UserRepository`, `PlanRepository`, `WithdrawRepository`, `ChatRepository`, plus `FirebaseHelper`) talk to Firestore and Room. Local persistence is Room (`AppDatabase`, db name `investment_database`, version 4) with entities `UserModel`, `AccountModel`, `PlanModel`, `WithdrawModel`, `TeamSettings` and DAOs `UserDao`, `PlanDao`, `WithdrawDao`. Coroutines + KTX `.await()` for async.

### External integrations

- **Firestore + Firebase Auth + Firebase Storage** for everything user-, plan-, and announcement-related (collections `users`, `accounts`, `Admin`, plans, `withdraw`, announcements, chat).
- **CoinPayments** (`HomeFragment.kt:47`) for crypto-side balance / transaction reads — keys hardcoded, see Security Notice.
- **FCM v1 HTTP API** (`notifications/Fcm.kt:15`) for push, called directly from the device with a token minted in `AccessToken.kt`. `NotificationService` extends `FirebaseMessagingService`; its notification id is `Random.nextInt()` (`NotificationService.kt:75`), so concurrent pushes can overwrite each other.

There is no Retrofit (only `converter-gson` is on the classpath without `retrofit2:retrofit`); networking is OkHttp + JSONObject for the two REST integrations.

## Tech stack

- **Build:** AGP 8.11.1, Kotlin 2.0.21, KSP 2.0.21-1.0.27, Java 11, Google Services plugin 4.4.2, View Binding.
- **App config:** `applicationId = com.codingempire.adminpse`, `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`.
- **AndroidX / Jetpack:** core-ktx 1.16.0, appcompat 1.7.1, activity 1.10.1, constraintlayout 2.2.1, lifecycle-livedata-ktx 2.8.7, swiperefreshlayout 1.1.0, navigation 2.7.7, Room 2.7.0.
- **Firebase:** firestore + firestore-ktx 25.1.3, auth + auth-ktx 23.2.0, messaging 24.1.1, storage-ktx 21.0.2.
- **UI / images:** Material 1.12.0, Glide 4.16.0, CircleImageView 3.1.0, Lottie 6.5.2.
- **HTTP / serialization:** OkHttp 4.12.0, Gson 2.12.1, `google-auth-library-oauth2-http` 1.29.0, ZXing core 3.4.1, OpenCSV 5.7.1.
- **Permissions:** `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`, `WAKE_LOCK`, plus the standard FCM `c2dm.RECEIVE` and `gsf.READ_GSERVICES`.

The repo does **not** use Hilt/Dagger, Compose, DataBinding, WorkManager, App Check, Crashlytics, Analytics, or Remote Config.

## Project layout

```
Philippine-Stock-Exchange-admin/
├── app/
│   ├── google-services.json                       ⚠ tracked (project philippine-stock-exchang-db)
│   ├── build.gradle.kts                           applicationId com.codingempire.adminpse
│   └── src/main/
│       ├── AndroidManifest.xml                    LoginActivity launcher
│       └── java/com/codingempire/adminpse/
│           ├── ui/                                MainActivity, Login/SignUp/Forgot/Reset/Launcher
│           ├── fragments/
│           │   ├── HomeFragment.kt                ⚠ CoinPayments keys hardcoded (lines 45-47)
│           │   ├── chat/, withdraw/, investment_plans/, plan_setting/, create_user/
│           │   └── …                              19 fragments total
│           ├── ViewModel/                         UserVM, PlanVM, WithdrawVM, ChatVM
│           ├── repository/                        UserRepo, PlanRepo, WithdrawRepo, ChatRepo,
│           │                                      FirebaseHelper, AppDatabase (Room v4)
│           ├── data/                              Room entities + UI models (⚠ password fields)
│           ├── notifications/
│           │   ├── AccessToken.kt                 ⚠ Firebase service-account JSON inlined
│           │   ├── Fcm.kt                         FCM v1 send (project id mismatch)
│           │   └── NotificationService.kt
│           └── adapters/, utils/, factories/
├── local.properties                               ⚠ tracked
├── .gradle/, .idea/, build/                       ⚠ tracked, no .gitignore
└── README.md
```

## Setup / run

1. **Rotate every credential listed in the [Security Notice](#-security-notice--read-first) first** — Firebase service account, CoinPayments keys, and the Firebase web API key. Once rotated, the in-source secrets are dead and the app's FCM + CoinPayments paths will fail until properly proxied through a backend.
2. Add a real `.gitignore` (at minimum: `app/google-services.json`, `local.properties`, `.gradle/`, `.idea/`, `build/`, `*.jks`) and `git rm --cached` everything currently shadowed.
3. Open in Android Studio, sync, run on an Android 7.0+ device.

## Honest limitations

- **README claims do not match the code.** No listings management, no order book, no trade oversight, no REST API, no WebSockets, no MIT licence file. The app is an admin panel for an investment / MLM platform.
- **Plaintext passwords.** `data/UserModel.kt:14` and `data/UserAccountItem.kt:14` declare `password: String = ""`; `fragments/create_user/CreateUserFragment.kt:87` writes the admin-supplied password to Firestore and Room. Drop these fields and let Firebase Auth own credentials.
- **Service account on the device.** `notifications/AccessToken.kt:27-42` lets every install mint privileged Google OAuth tokens. Move to a backend.
- **Mismatched FCM project id.** Service account is for `philippine-stock-exchang-296cd`; `Fcm.kt:15` posts to `philippine-stock-exchang-db`. Push sending from the device would 403 today; pick one project and align the credential and URL.
- **`Random.nextInt()` notification IDs** in `NotificationService.kt:75` cause collisions.
- **`GlobalScope.launch`** in `Fcm.kt:37` (and a few other callsites) — switch to `viewModelScope` / `lifecycleScope`.
- **`AsyncTask` in `AccessToken.kt`** is deprecated since API 30. Combine the rotation work with a Coroutines port.
- **Dead Retrofit dependency.** `converter-gson` is on the classpath but `retrofit2:retrofit` is not — drop it.
- **No tests** beyond stock `ExampleUnitTest` / `ExampleInstrumentedTest`.
- **No `LICENSE` file** despite the previous README claiming MIT.
