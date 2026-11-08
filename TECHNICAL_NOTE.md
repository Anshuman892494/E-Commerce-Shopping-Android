# Technical Note: Engineering Deep Dive & Release Readiness

> **Project**: E-Commerce Shopping Android Application  
> **Client / Evaluator**: ALGIONS AI LABS PRIVATE LIMITED  
> **Author**: Anshul Sharma  
> **Document Purpose**: Technical breakdown of Secure Storage, State Persistence, Accessibility Audits, and Production Release Hardening.

---

## 1. Secure Storage & Credential Management

### 1.1 Threat Model & Security Requirements
In mobile e-commerce applications, sensitive data such as JWT authentication tokens, refresh tokens, and user credentials must be shielded against:
- **Device Rooting & File Extraction**: Attackers inspecting `/data/data/<package_name>/shared_prefs`.
- **Log Leakage**: Accidental dumping of Authorization headers in Android Logcat.
- **Man-in-the-Middle (MitM) Attacks**: Cleartext interception on untrusted public Wi-Fi.

### 1.2 Implementation Details (`SecureStorage.kt`)
To meet standard enterprise security guidelines, the application implements **Android Jetpack Security (`androidx.security.crypto:security-crypto:1.1.0-alpha06`)**:

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
    context,
    "secure_ecommerce_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

- **Key Encryption (`AES256_SIV`)**: Preference keys are deterministically encrypted so keys cannot be correlated with user attributes.
- **Value Encryption (`AES256_GCM`)**: Authenticated encryption ensuring both confidentiality and cryptographic integrity. The underlying master key is stored in the **Android Keystore hardware security module (HSM)**, making key extraction impossible without hardware tampering.
- **Logcat Redaction**: `AppContainer.kt` configures `HttpLoggingInterceptor().redactHeader("Authorization")` to ensure tokens are never printed to terminal logs.

---

## 2. State Persistence & Offline Architecture

### 2.1 Storage Layer Design (`LocalDatabaseHelper.kt`)
The application implements an **Offline-First Cache-Aside Strategy** using a local SQLite database (`ecommerce_app.db`):

| Table | Primary Key | Stored Data | Cache Strategy |
| :--- | :--- | :--- | :--- |
| `cached_products` | `id` (VARCHAR) | JSON payload of `Product` + `category` | Updated on every successful `GET /api/products`. Retained indefinitely for offline catalog browsing. |
| `cart_items` | `variant_key` (VARCHAR) | Selected items, variant info, and quantity | Persisted in real-time. Survives complete app force-stops and reboots. |
| `cached_orders` | `id` (VARCHAR) | Confirmed `Order` records and status | Written exclusively upon server `HTTP 201 Created` confirmation. Accessible offline. |

### 2.2 Distinguishing Offline Actions from Server-Confirmed State
A fundamental criterion of the Algions assessment is:
> *"Do not confirm an order or deduct stock locally until the backend confirms the order."*

Our `OrderRepository` enforces this strictly:
1. When the user taps "Pay & Confirm", the UI enters an explicit `isPlacingOrder = true` loading state.
2. The order payload is transmitted to `POST /api/orders`.
3. If the server responds with an error (`402 Declined`, `409 Out of Stock`, or timeout):
   - The order is **never** written to `cached_orders`.
   - The user's local cart items and quantities are **preserved**.
   - The UI surfaces an actionable error notice with a retry trigger.
4. Only upon receiving HTTP 201 Created does the repository:
   - Save the confirmed order to `cached_orders`.
   - Invoke `cartRepository.clearCart()`.
   - Transition UI state to the `OrderTrackingScreen`.

---

## 3. Accessibility (a11y) & WCAG 2.1 AA Compliance

The UI was audited against **WCAG 2.1 AA** standards and Android Accessibility guidelines:

### 3.1 Touch Targets ($\ge 48 \times 48\,\text{dp}$)
- All interactive controls (stepper minus/plus buttons, category chips, remove icons, back buttons) enforce minimum dimensions of $48\,\text{dp} \times 48\,\text{dp}$ using `Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)`.
- Primary CTA buttons have a height of $52\,\text{dp}$ across all screens.

### 3.2 Contrast Ratios
- **Primary Brand (`#1E3A8A`) on Surface (`#FFFFFF`)**: Contrast ratio **10.5 : 1** (exceeds AAA requirement).
- **Secondary Action (`#EA580C`) on Dark/Light**: High visibility with bold typography.
- **Body Text (`#0F172A`) on Background (`#F8FAFC`)**: Contrast ratio **16.1 : 1** (exceeds AAA requirement).

### 3.3 Semantic Screen-Reader Labels
- Images specify contextual descriptions: `contentDescription = product.title`.
- Badged shopping cart icon specifies full descriptive semantics: `"View shopping cart with X items"`.
- Product cards announce: `"${product.title}, $${product.price}, ${if (stock > 0) "In Stock" else "Out of Stock"}"`.

---

## 4. Failure Modes & Non-Crashing Recovery Matrix

| Scenario | HTTP / Failure Trigger | System Response & UI Recovery |
| :--- | :--- | :--- |
| **Out-of-Stock Collision** | Server returns `409 Conflict` | Displays red warning banner; cart items are retained; stock is not decremented locally. User can adjust quantity. |
| **Card Declined** | Server returns `402 Payment Required` | Displays "Card was declined: Insufficient funds"; button resets to active; user can change payment details or retry. |
| **Network Loss / Timeout** | SocketTimeout / `504 Gateway Timeout` | OkHttp cancels connection after 8s; UI catches `IOException` and shows retry prompt without crashing. |
| **Repeated Taps (Double-Submit)**| Rapid double-clicking "Pay & Confirm" | Button immediately disables itself upon first click (`enabled = !uiState.isPlacingOrder`); prevents duplicate charges. |
| **App Restart with Items in Cart**| Process death / phone reboot | `LocalDatabaseHelper` re-populates `CartRepository.cartItems` on startup; cart is 100% restored. |

---

## 5. Production Release Readiness & Hardening

Before shipping to Google Play Store, the following production configurations are scheduled:

### 5.1 R8 / ProGuard Code Shrinking & Obfuscation
Enable full optimization in `app/build.gradle.kts`:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```
*Custom ProGuard Rules (`proguard-rules.pro`)*:
```proguard
# Preserve Retrofit & Gson data models
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class e_commerce_app.com.data.model.** { *; }
```

### 5.2 Network Security Config (HTTPS Enforcement)
In production, cleartext traffic is disabled. Android `res/xml/network_security_config.xml` will enforce certificate pinning:
```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.algions.com</domain>
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">BASE64_HASH_OF_CERTIFICATE=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

### 5.3 Automated CI/CD Pipeline (GitHub Actions)
```yaml
name: Android CI
on: [push, pull_request]
jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'oracle'
      - name: Run Unit Tests
        run: cd Android && ./gradlew test
      - name: Build Debug APK
        run: cd Android && ./gradlew assembleDebug
```

