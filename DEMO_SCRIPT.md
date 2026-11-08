# 🎬 3–5 Minute Video Demonstration Script & Walkthrough

> **Company**: Algions AI Labs Private Limited  
> **Candidate**: Anshul Sharma  
> **Project**: E-Commerce Shopping Android Application  
> **Target Video Length**: 3 to 4 Minutes  

---

## 📹 Video Recording Setup
1. **Screen 1 (Left / Main)**: Android Emulator (Pixel or equivalent running Android 14 / 15).
2. **Screen 2 (Right / Background)**: Terminal window running `node server.js` showing real-time inventory and order logs.
3. **Audio**: Clear microphone narration following the script below.

---

## ⏱️ Scene-by-Scene Script & Timestamps

### Scene 1: Introduction & Clean Install (0:00 – 0:35)
- **Visual**: Show Android home screen. Open terminal and run:
  ```powershell
  adb install -r Android/app/build/outputs/apk/debug/app-debug.apk
  ```
- **Spoken Script**:
  > *"Hello, my name is Anshul Sharma, and this is my submission for the Algions AI Labs Advanced Mobile App Assessment — the E-Commerce Shopping Android Application.*  
  > *We begin with a clean install using our submitted debug APK. As you can see, the APK installs cleanly and launches immediately with our edge-to-edge Material 3 theme."*

---

### Scene 2: Authentication & Secure Session (0:35 – 1:00)
- **Visual**: App launches into Welcome / Auth Screen. 
  - Tap **"Fill Demo Credentials"** (`demo@algions.com` / `Password123!`).
  - Point out password visibility toggle.
  - Tap **"Sign In"**. 
  - Mention: Token is stored securely in `EncryptedSharedPreferences` with AES-256-GCM.
- **Spoken Script**:
  > *"Here on the authentication screen, we have support for registration, standard login, and guest checkout. I'll click 'Fill Demo Credentials' and sign in. Notice how our session token is encrypted using Android Jetpack Security backed by Android Keystore, so credentials are never logged in cleartext."*

---

### Scene 3: Product Discovery, Search & Variants (1:00 – 1:45)
- **Visual**:
  - Show catalog with high quality images and stock badges.
  - Type `"Headphones"` in search bar $\rightarrow$ instant debounced filtering. Clear search.
  - Filter by category chips: tap **"Footwear"** $\rightarrow$ shows Nike shoes with "Only 2 left" badge.
  - Open **"Sony WH-1000XM5 Headphones"** detail screen.
  - Select Color swatch (Midnight Black $\rightarrow$ Platinum Silver).
  - Select Size / Option.
  - Increment quantity stepper from 1 to 2.
  - Tap **"Add to Cart"** $\rightarrow$ Snackbar feedback appears: *"Item added to your shopping cart!"*.
  - Show top bar cart badge updates to `2`.
- **Spoken Script**:
  > *"Now we are in product discovery. We have instant debounced search, category filtering, and stock status indicators. Opening the Sony Headphones, we can select color swatches and variant sizes. The stepper is dynamically bounded by server inventory. We'll add 2 units to the cart, and notice the instant visual feedback and badge update."*

---

### Scene 4: Cart Management & Stock Validation (1:45 – 2:20)
- **Visual**:
  - Tap Shopping Cart icon.
  - Review items, variant descriptions, and price breakdown (Subtotal, Tax at 8%, Free Shipping over $100).
  - Tap `+` and `-` to verify quantity adjustments.
  - Tap **"Proceed to Checkout"**.
- **Spoken Script**:
  > *"In the shopping cart, our app executes an automatic stock validation call with the backend. It calculates subtotal, 8% sales tax, and free shipping for orders over \$100. We proceed to checkout."*

---

### Scene 5: Acceptance Test Demonstration — Failure & Safety Cases (2:20 – 3:15)
- **Visual**:
  - Checkout screen displays pre-filled delivery address.
  - Scroll to **"2. Payment Simulation Scenario"**.
  - **Test Case A (Out of Stock Collision - 409)**:
    - Select **"🔴 Out-of-Stock Collision (409 Conflict)"**.
    - Tap **"Pay & Confirm"**.
    - Notice: Button shows progress spinner; **app does NOT navigate to success**.
    - Error alert appears: *"One or more items in your cart became out of stock."*
    - Check terminal: Stock was **not** decremented. Cart was **not** cleared.
  - **Test Case B (Card Declined - 402)**:
    - Select **"🟡 Card Declined (402 Payment Required)"**.
    - Tap **"Pay & Confirm"**.
    - Error appears: *"Card was declined: Insufficient funds."* App recovers without crashing.
- **Spoken Script**:
  > *"Now demonstrating the core safety requirement of the assessment: the app must never confirm an order or deduct stock locally before the backend confirms it. First, I select the Out-of-Stock race condition. When I click Pay & Confirm, the app shows an authorizing loader, but halts upon receiving HTTP 409. As you see, no order is recorded locally, and our cart items are preserved. Similarly, testing Card Declined surfaces a 402 error gracefully without crashing."*

---

### Scene 6: Successful Checkout, Server Stock Deduction & Live Tracking (3:15 – 3:55)
- **Visual**:
  - Select **"🟢 Normal Success (201 Created)"**.
  - Tap **"Pay & Confirm"**.
  - Terminal shows: `[INVENTORY] Deducted 2 units... [ORDER] Created Order #ORD-XXXXX. Server Confirmed.`
  - App navigates to **OrderTrackingScreen**.
  - Show green checkmark banner, Order ID, Estimated Delivery.
  - Show live order timeline: `Order Placed` $\rightarrow$ `Packed & Prepared` $\rightarrow$ `Shipped`.
  - Tap **"All Orders"** $\rightarrow$ Show order in **OrderHistoryScreen**.
- **Spoken Script**:
  > *"Finally, we select Normal Success. Upon receiving HTTP 201 Created from the backend, stock is decremented server-side, the cart is cleared, and we transition to the Order Confirmation and Live Tracking screen. The timeline dynamically advances based on order status, and we can inspect our past orders anytime under My Orders."*

---

### Scene 7: Offline Caching & Wrap-up (3:55 – 4:15)
- **Visual**:
  - Enable Airplane mode on emulator.
  - Return to Catalog: Amber banner **"Offline Mode — Displaying cached catalog"** appears.
  - Cached products remain accessible.
  - Disable Airplane mode: Data reconnects seamlessly.
- **Spoken Script**:
  > *"Lastly, toggling Airplane mode confirms that our local SQLite database retains the catalog and order history for seamless offline browsing. Thank you for reviewing my project!"*

