# 🎬 3–5 Minute Video Demonstration Script & Walkthrough

> **Company**: Algions AI Labs Private Limited  
> **Candidate**: Anshul Sharma  
> **Project**: E-Commerce Shopping Android Application  
> **Target Video Length**: 3 to 4 Minutes  
> **Backend Architecture**: Node.js REST API + **MongoDB (Mongoose ODM)**  
> **Android Architecture**: Clean MVVM + Jetpack Compose (Material 3)

---

## 📹 Video Recording Setup
1. **Screen 1 (Left / Main)**: Android Emulator (Pixel or equivalent running Android 14 / 15).
2. **Screen 2 (Right / Background)**: Terminal window running `node server.js` showing real-time MongoDB inventory updates and order logs.
3. **Audio**: Clear microphone narration following the script below.

---

## ⏱️ Scene-by-Scene Script & Timestamps

### Scene 1: Introduction, Clean Install & Splash Screen (0:00 – 0:35)
- **Visual**: Show Android home screen. Open terminal and run:
  ```powershell
  adb install -r release/app-debug.apk
  ```
  - App launches $\rightarrow$ Show **Animated Splash Screen** with pulsating brand logo and tagline: *"Smart Shopping • Fast Delivery"*.
  - Automatically transitions to the Welcome / Sign In screen.
- **Spoken Script**:
  > *"Hello, my name is Anshul Sharma, and this is my submission for the Algions AI Labs Advanced Mobile App Assessment — the E-Commerce Shopping Android Application.*  
  > *We begin with a clean install using our submitted debug APK. Notice our animated splash screen that smoothly validates user session and guides us to sign in."*

---

### Scene 2: Authentication & Secure Session (0:35 – 0:55)
- **Visual**:
  - Tap **"Fill Demo Credentials"** (`demo@algions.com` / `Password123!`).
  - Point out password visibility toggle.
  - Tap **"Sign In"**. 
  - Token is securely stored in `EncryptedSharedPreferences` with AES-256-GCM backed by Android Keystore.
- **Spoken Script**:
  > *"I'll click 'Fill Demo Credentials' and sign in. Our session token is encrypted using Android Jetpack Security backed by Android Keystore, so sensitive tokens are never logged in cleartext."*

---

### Scene 3: 5-Tab Navigation, Wishlist & Promo Banner (0:55 – 1:40)
- **Visual**:
  - Point out the **Material 3 Bottom Navigation Bar** with 5 tabs: **Shop**, **Wishlist**, **Cart**, **Orders**, and **Profile**.
  - Show the **Spring Offer Banner** with coupon code `ALGIONS20`.
  - Type `"Nike"` in search bar $\rightarrow$ instant debounced filtering. Clear search.
  - Tap the heart icon on Sony Headphones $\rightarrow$ heart turns red and Wishlist badge shows `1`.
  - Switch to **Wishlist tab** $\rightarrow$ tap orange **"Move to Cart"** button $\rightarrow$ snackbar confirms item moved, wishlist badge updates, and cart badge becomes `1`.
- **Spoken Script**:
  > *"Here on the Shop screen, we have a 5-tab Material 3 bottom navigation bar, debounced search, category filtering, and promotional banners. Users can save items to their Wishlist with one tap, and move them to their shopping cart seamlessly with real-time reactive badge updates."*

---

### Scene 4: Promo Coupon & Simulated Failure Safety Demonstration (1:40 – 2:40)
- **Visual**:
  - Switch to **Cart tab** $\rightarrow$ Tap **"Proceed to Checkout"**.
  - Point out: Bottom navigation is hidden during checkout for distraction-free completion.
  - In **"Promotional Discount"**, enter coupon `ALGIONS20` and tap **Apply**.
  - Show: MongoDB verifies coupon, applies **20% discount** ($-\$32.00$), and grand total dynamically drops from $\$172.79 \rightarrow \$138.23$.
  - **Rubric Safety Demonstration**:
    - Select **"🔴 Out-of-Stock Collision (409 Conflict)"**.
    - Tap **"Pay & Confirm"**.
    - Show: Button shows progress spinner; **app does NOT navigate to success**.
    - Error banner appears gracefully without crash. Cart items remain preserved.
- **Spoken Script**:
  > *"In checkout, I'll enter our promo coupon ALGIONS20. The server validates it against MongoDB and instantly deducts 20% from the total. Now demonstrating the core assessment safety rule: the app must never confirm an order or deduct stock locally before the backend confirms it. Selecting Out-of-Stock Collision and tapping Pay & Confirm, the app shows an authorizing state, halts upon HTTP 409, and displays an actionable error without crashing or corrupting state."*

---

### Scene 5: Server Confirmation, MongoDB Stock Decrement & Live Tracking (2:40 – 3:30)
- **Visual**:
  - Select **"🟢 Normal Success (201 Created)"**.
  - Tap **"Pay & Confirm"**.
  - Check background terminal: MongoDB atomically decrements inventory: `[INVENTORY] Deducted 1 units... Remaining: 1`. Order document created in MongoDB.
  - App navigates to **OrderTrackingScreen**.
  - Show live order timeline: `Order Placed` $\rightarrow$ `Packed & Prepared` $\rightarrow$ `Shipped`.
  - Tap **"All Orders"** $\rightarrow$ Show order in **Orders tab**.
- **Spoken Script**:
  > *"Selecting Normal Success, the backend confirms the payment, atomically decrements stock in MongoDB, and returns HTTP 201 Created. The app then transitions to our live order tracking timeline, and stores the receipt in our MongoDB Orders collection."*

---

### Scene 6: User Profile & Dynamic Dark Mode Switch (3:30 – 4:00)
- **Visual**:
  - Switch to **Profile tab**.
  - Show user avatar ("AD"), "Verified Buyer" badge, and delivery address editor.
  - Toggle **Dark Mode switch** $\rightarrow$ App instantly transforms into a sleek Material 3 dark theme.
  - Tap **Sign Out** $\rightarrow$ Confirm sign out dialog $\rightarrow$ Returns to Welcome screen.
- **Spoken Script**:
  > *"Finally, on the Profile screen, users can manage their saved addresses, jump to past orders, and toggle instantaneous Dark Mode. Clicking Sign Out clears secure session tokens and safely returns the user to the login screen. Thank you!"*
