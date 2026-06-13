# Shopping App - Premium E-commerce Platform

**Shopping App** is a production-ready, high-end fashion and electronics shopping application built using **Kotlin** and **Jetpack Compose**. It features a modern, animated UI inspired by top-tier apps like Myntra and Nike, fully integrated with **Firebase Realtime Database** and **Razorpay Payment Gateway**.

---

## 🌟 Key Features

### 🚀 Seamless Onboarding
*   **Animated Splash Screen**: High-end scale and overshoot animations.
*   **Phone Authentication**: Secure login via Firebase OTP.
*   **Smart Auto-Login**: Remembers user sessions for instant access.
*   **Profile Setup**: Mandatory name, email, and native gallery image picker for new users.

### 🛍️ Premium Shopping Experience
*   **Dynamic Home Screen**: Branded "LuxiQue" header with real-time cart notification badges.
*   **100+ Hardcoded Items**: Clothes, Mobiles, Shoes, and more with unique naming conventions.
*   **Category Filtering**: Smooth, instant filtering via modern rounded-box category selectors.
*   **Search**: Real-time product search functionality across the entire catalog.

### 📄 Product Details & Cart
*   **Luxury Details Layout**: Large hero images with a 4-image preview grid.
*   **Dynamic Selectors**: Interactive Size (S-3XL) and Color selection with visual feedback.
*   **Real-time Cart**: Fully synced with Firebase; quantity updates and deletions reflect instantly.
*   **Edge-to-Edge Scrolling**: Content flows elegantly behind a custom protruding navigation bar.

### 💳 Payments & Receipts
*   **Razorpay Integration**: Fully functional checkout with **Live API Keys**.
*   **Dynamic Pricing**: Includes ₹1.0 testing logic for "Snake Jacket Pro" and Free Shipping.
*   **PDF Invoice Generator**: Generates professional, branded PDF receipts with Order and Payment IDs.
*   **MediaStore API**: Automatically saves invoices to the phone's public `Downloads/Shopping_App` folder.

### 💰 Monetization & History
*   **AdMob Integration**: Non-intrusive Banner Ads implemented on Search, History, and Profile screens.
*   **Order History**: Detailed list of past transactions with "PAID" status badges and one-tap PDF downloads.

---

## 🛠️ Tech Stack

-   **UI**: Jetpack Compose (100% Declarative UI)
-   **Architecture**: MVVM + Clean Architecture + Repository Pattern
-   **Dependency Injection**: Hilt (Dagger)
-   **Backend**: Firebase Realtime Database, Auth, Storage, and Crashlytics
-   **Payments**: Razorpay Android SDK
-   **Image Loading**: Coil
-   **Navigation**: Navigation Compose (Nested Graphs)
-   **Local Logic**: Native Android PdfDocument & MediaStore API

---

## 🏗️ Project Structure

```text
com.shoppingappmahesh
├── data
│   ├── repository    # Firebase implementation of repositories
│   └── util          # Firebase Data Seeder and Helpers
├── di                # Hilt Modules (Firebase providers)
├── domain
│   ├── model         # Data Classes (User, Product, Order, etc.)
│   └── repository    # Interface definitions
├── ui
│   ├── components    # Reusable UI elements (Product cards, Ads, etc.)
│   ├── navigation    # NavHost and Screen definitions
│   ├── screens       # All UI Screens (Home, Auth, Cart, Order, etc.)
│   └── theme         # Material 3 Color, Type, and Shape definitions
└── util              # PDF Generator and Logic utilities
```

---

## 🚀 Setup & Installation

### 1. Firebase Configuration
*   Connect the project to Firebase Console.
*   Enable **Phone Authentication** and **Realtime Database**.
*   Add your `google-services.json` to the `app/` folder.
*   Set Realtime Database rules to allow `auth != null` access.

### 2. Razorpay Setup
*   The project is pre-configured with **Live Key**: `Added Your API KEY`.
*   To change keys, update `RazorpayActivity.kt`.

### 3. AdMob Setup
*   Update your App ID in `AndroidManifest.xml`.
*   Replace test Banner IDs in `BannerAdView.kt` with your production unit IDs.

---

## 📱 Screenshots

| Splash & Login | Home & Category | Product Details | My Cart |
| :---: | :---: | :---: | :---: |
| ![Splash](https://via.placeholder.com/200x400?text=Splash) | ![Home](https://via.placeholder.com/200x400?text=Home) | ![Details](https://via.placeholder.com/200x400?text=Details) | ![Cart](https://via.placeholder.com/200x400?text=Cart) |

---

## ⚙️ How the Code Works

This project follows the **Clean Architecture** principles combined with the **MVVM (Model-View-ViewModel)** pattern to ensure a scalable, maintainable, and testable codebase.

### **1. Core Architecture**
-   **Domain Layer**: Contains the core business logic, including Data Models (`Product`, `User`, `Order`) and Repository interfaces. It is completely independent of the data source.
-   **Data Layer**: Implements the repository interfaces using **Firebase Realtime Database**. It handles all data CRUD operations and real-time listeners.
-   **UI Layer (Presentation)**: Built entirely with **Jetpack Compose**. Each screen has a dedicated **Hilt-injected ViewModel** that manages the UI state using `StateFlow`.

### **2. Application Startup Flow**
1.  **ShoppingApp.kt**: The entry point where **Hilt Dependency Injection** and **Google Mobile Ads** are initialized.
2.  **MainActivity.kt**: Serves as the primary container. It hosts the `LuxFloatingBottomBar` and the `NavGraph`.
3.  **SplashScreen.kt**: Performs an asynchronous session check.
    -   If the user is not logged in, they are routed to the **Auth Flow**.
    -   If logged in but the profile is incomplete, they are routed to **Profile Setup**.
    -   Otherwise, they land instantly on the **Home Screen**.

### **3. Authentication & Profile Management**
-   **Phone Auth**: Utilizes Firebase Phone Authentication for secure OTP-based logins.
-   **Profile Setup**: A one-time mandatory flow for new users to capture their name, email, and profile picture (via native gallery picker), saved under `/users/$uid`.

### **4. Real-time Shopping Logic**
-   **Catalog**: The `FirebaseDataSeeder` automatically populates 100 premium items into the database on the first launch.
-   **Cart Sync**: Cart operations (Add/Update/Remove) are performed directly on the Firebase node `/cart/$userId`. This ensures the user's cart is synced across all their devices in real-time.

### **5. Payment & Order Lifecycle**
-   **Razorpay Integration**: When a user clicks "Pay", the app launches `RazorpayActivity`. Upon a successful transaction, the SDK returns a `payment_id`.
-   **Order Creation**: The app then packages the cart items, total amount, and `payment_id` into an `Order` object, saves it to `/orders`, and clears the user's cart.

### **6. Technical Utilities**
-   **PDF Generation**: The `PdfGenerator` uses the native Android `PdfDocument` API to draw a professional invoice canvas.
-   **Storage**: Utilizes the **MediaStore API** to save generated receipts into the public `Downloads/LuxiQue` folder without requiring legacy storage permissions.
-   **Image Loading**: Powered by **Coil** for efficient, cached image rendering of high-resolution fashion assets.

---

## 🔍 Deep Technical Explanation

### **1. Data Source & Seeding (Where is the data coming from?)**
-   **Static-to-Dynamic Flow**: The project uses a hybrid approach. 100 premium products and 5 categories are hardcoded inside the `FirebaseDataSeeder.kt` utility.
-   **Automated Sync**: On the very first launch of the app, the `MainActivity` triggers the seeder. This seeder pushes the entire catalog (including names, descriptions, and high-quality image URLs) into the **Firebase Realtime Database**.
-   **Real-time Fetching**: Once seeded, all screens (Home, Search, Details) fetch this data dynamically using Kotlin **Flows**. This ensures that if you change any price or item in the Firebase Console, it updates on all users' phones instantly.

### **2. Authentication Mechanism (How are users verified?)**
-   **Step 1: Phone Auth**: The app uses `Firebase Phone Authentication`. When a user enters their number, a request is sent to Firebase, which returns a `verificationId` and sends an SMS OTP.
-   **Step 2: Credential Verification**: The 6-digit OTP is combined with the `verificationId` to create a `PhoneAuthCredential`. This is passed to `auth.signInWithCredential(credential)`, which establishes a secure user session.
-   **Step 3: Session Persistence**: The login is permanent. The `SplashScreen` uses `FirebaseAuth.getInstance().currentUser` to check if a session exists. If it does, the user skips the login screen entirely.

### **3. End-to-End Application Flow**
The app follows a strict state-driven navigation path:

1.  **Splash Start**: Initializes Hilt components and AdMob. Performs an immediate check: `Is user logged in?` AND `Is profile complete?`
2.  **Onboarding**:
    -   If new: **Login Screen (Phone)** -> **OTP Verification** -> **Profile Setup** (Picture, Name, Email).
    -   Data is saved in Firebase under `/users/{userId}`.
3.  **Shopping**:
    -   **Home**: Displays categories. Selecting one triggers a filter in the `HomeViewModel` using the seeded database data.
    -   **Cart**: Every "Add to Cart" action writes to the Firebase `/cart/{userId}` node. This makes the cart **device-independent** (login on any phone and see your same items).
4.  **Checkout & Payment**:
    -   User enters shipping details.
    -   The app calculates the total and launches `RazorpayActivity`.
    -   Upon successful transaction, Razorpay returns a `payment_id`.
5.  **Order Fulfillment**:
    -   A new **Order** object is created containing the `payment_id`, cart items, and timestamp.
    -   This is pushed to `/orders/{userId}`.
    -   The cart node in Firebase is then **automatically cleared**.
6.  **Post-Purchase**:
    -   **Success Screen**: Displays dynamic success details.
    -   **PDF Generation**: The `PdfGenerator` uses the system's `Canvas` API to draw a branded invoice, which is then saved to the public `Downloads/LuxiQue` folder using the **Android MediaStore API**.

---

## 📜 License

This project is developed as a premium production-ready template. All rights reserved.

**Developed by Mahesh Kashyap** 🚀


