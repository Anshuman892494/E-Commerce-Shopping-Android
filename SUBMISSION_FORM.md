# 📋 Algions AI Labs Submission Form — Ready-to-Submit Reference

> **Assessment**: Advanced Individual Project Assessment  
> **Role**: Android & Mobile App Development  
> **Project Title**: E-Commerce Shopping Android Application  
> **Candidate**: Anshul Sharma  

Use the pre-filled fields below to quickly copy-paste into the official Google Form / Submission Portal.

---

### Section 1: Candidate Identification
| Form Field | Value / Details |
| :--- | :--- |
| **Full Name** | Anshul Sharma |
| **Registration Number** | *[Enter your University / College Reg Number]* |
| **University Email** | *[Enter your University Email Address]* |

---

### Section 2: Assignment Confirmation
| Form Field | Value / Details |
| :--- | :--- |
| **Internship Role** | Android & Mobile App Development |
| **Exact Assigned Project Title** | E-Commerce Shopping Android Application |

---

### Section 3: Code & Version
| Form Field | Value / Details |
| :--- | :--- |
| **Repository URL** | `https://github.com/Anshuman892494/E-Commerce-Shopping-Android` |
| **Primary Branch** | `main` |
| **Final Commit ID** | `b65977eb0579e49c719e5d4cb05c866d9b9aae72` (or latest `main` HEAD) |
| **Access Confirmation** | Public repository (or reviewer granted collaborator access) |

---

### Section 4: Demonstration & Media Links
| Form Field | Value / Details |
| :--- | :--- |
| **3–5 Minute Video Demo Link** | *[Google Drive / YouTube Unlisted link of the recording]* |
| **Live Demo URL** | Local backend REST service at `http://localhost:5000` (or `http://10.0.2.2:5000` for Android emulator) |
| **APK Download Link** | *[Google Drive Link or GitHub Release Link to `app-debug.apk`]* |
| **Android Version / Device Tested** | Android 15 (API level 37), Pixel 10a Emulator (also tested down to minSdk 24 / Android 7.0) |

---

### Section 5: Documentation Links
| Form Field | Value / Details |
| :--- | :--- |
| **README Link** | Included in root repository (`README.md`) |
| **Technical Report Link** | Included in root repository (`TECHNICAL_NOTE.md`) |
| **Design System Link** | Included in root repository (`DESIGN_SYSTEM.md`) |
| **Demo Script Link** | Included in root repository (`DEMO_SCRIPT.md`) |

---

### Section 6: Technology Stack & Limitations
| Form Field | Value / Details |
| :--- | :--- |
| **Technology Stack** | **Android Client**: Kotlin 2.2, Jetpack Compose (Material 3), Navigation Compose, 5-Tab Bottom Navigation, Animated Splash Screen, Retrofit 2, OkHttp 3, Gson, Coil Compose 2.7, Android SQLite (`SQLiteOpenHelper`) for offline products, cart and wishlist caching, Android Jetpack Security (`EncryptedSharedPreferences`).<br/>**Backend**: Node.js, Express.js REST API with **MongoDB Database (Mongoose ODM)**, API Key authentication, atomic stock inventory updates (`$inc: { stock: -qty }`), and promotional coupon validation. |
| **Build & Setup Instructions** | **Backend**: `cd Server && npm install && npm start`<br/>**Android Unit Tests**: `cd Android && .\gradlew.bat test`<br/>**Android APK Build**: `cd Android && .\gradlew.bat assembleDebug`<br/>**APK Path**: `Android/app/build/outputs/apk/debug/app-debug.apk` |
| **Known Incomplete Features / Assumptions** | Connects to a simulated sandbox payment gateway rather than production Stripe/Razorpay. Local SQLite database caches products, active cart across restarts, and order history. |

---

### Section 7: AI Tools & Attribution
| Form Field | Value / Details |
| :--- | :--- |
| **AI Tools Used** | Antigravity AI Pair Programmer (Google DeepMind) |
| **Purpose of Use** | Architectural layout design, Compose UI boilerplate scaffolding, test case construction, and rubric compliance audits. |
| **Attribution** | Unsplash e-commerce product imagery used under free Unsplash license. Material 3 icon components from AndroidX library. |

---

### Section 8: Declarations
- [x] **Originality**: I confirm this submission is my own engineering work designed and developed to fulfill the assessment criteria.
- [x] **Ability to Explain**: I am fully prepared to explain the MVVM architecture and make live code changes during the 15-minute walkthrough and 10-minute live debugging task.
- [x] **No Real Sensitive Data**: Only synthetic mock customer data and dummy sandbox payment details are used.
- [x] **Working Links**: All submitted repository, APK, and video links have public / reviewer access enabled.
