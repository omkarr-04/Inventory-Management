# KK Auto - Inventory & Repair Shop Management System

A professional, production-ready Android application designed for commercial vehicle repair shops. Built with **Kotlin**, **Jetpack Compose**, and **Firebase Firestore**, this system handles everything from real-time inventory tracking to professional GST billing and PDF invoice generation.

---

## Key Features

### Inventory Management
* **Real-time Tracking:** Instant sync across multiple devices using Firebase Firestore.
* **Smart Alerts:** Customizable low-stock thresholds per item with automatic Android notifications.
* **Search & Filter:** Quickly find parts by name or category.
* **INR Formatting:** All pricing follows the Indian Rupee (₹) standard.

### Job Card System
* **Workflow Tracking:** Monitor vehicle status from `PENDING` -> `IN_PROGRESS` -> `COMPLETED` -> `DELIVERED`.
* **Stock Integration:** Automatically reduces inventory levels when parts are added to a job using Firestore Transactions.
* **Multi-Part Support:** Add multiple parts to a single vehicle repair job effortlessly.

### Billing & Payments
* **Professional Invoicing:** Supports Labor Charges and GST percentage calculations.
* **Payment Tracking:** Monitor `PAID`, `PARTIAL`, and `UNPAID` statuses.
* **Due Date Alerts:** Get notified when a customer's payment is overdue.
* **PDF Generation:** Generate A4-sized professional invoices and share them instantly via WhatsApp or Email.

### Business Dashboard
* **Daily Insights:** View today's total revenue and job count at a glance.
* **Financial Health:** Track total pending dues across all customers.
* **Inventory Health:** Immediate visibility into how many items are currently low on stock.

---

## Tech Stack

* **UI:** Jetpack Compose (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Database:** Firebase Firestore (NoSQL Real-time)
* **Background Tasks:** WorkManager
* **Navigation:** Jetpack Navigation Compose
* **PDF Engine:** Android Graphics Canvas (Native PDF Document)
* **Language:** Kotlin Coroutines & Flow

---

## Project Structure

```text
Inventory-Management/
├── app/
│   ├── src/main/java/com/example/inventory_management/
│   │   ├── ui/theme/             # Material 3 Design System
│   │   ├── CurrencyUtils.kt      # INR (₹) Formatting
│   │   ├── DashboardScreen.kt    # Analytics & Stats UI
│   │   ├── InventoryRepository.kt# Firestore Data Access
│   │   ├── JobHistoryScreen.kt   # History & Details UI
│   │   ├── NotificationHelper.kt # Android System Alerts
│   │   ├── PdfHelper.kt          # Invoice PDF Engine
│   │   └── ...                   # ViewModels & Data Models
│   ├── build.gradle.kts          # Dependencies (Firebase, Compose, WorkManager)
│   └── google-services.json      # Firebase Config (User-provided)
```

---

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/Inventory-Management.git
```

### 2. Firebase Configuration
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new project named `Inventory-Management`.
3. Add an **Android App** with package name `com.example.inventory_management`.
4. Download the `google-services.json` file and place it in the `app/` directory of the project.
5. In the Firebase Console, enable **Cloud Firestore** and set the rules to **Test Mode** (or configure production rules).

### 3. Build & Run
1. Open the project in **Android Studio (Ladybug or newer)**.
2. Sync Project with Gradle Files.
3. Run the app on an Emulator or Physical Device.
4. (Optional) For Android 13+, allow notification permissions when prompted to receive low-stock alerts.

---

## Screenshots

| Dashboard | Inventory | Job Details |
|-----------|-----------|-------------|
| ![Dashboard Placeholder] | ![Inventory Placeholder] | ![Job Details Placeholder] |

---

## Contributing
Contributions are welcome! If you'd like to improve the GST logic, add customer phone number databases, or enhance the PDF layout, feel free to fork the repo and submit a PR.

## License
This project is licensed under the MIT License - see the LICENSE file for details.

---
**Developed for KK Auto - Dahisar, Mumbai**
