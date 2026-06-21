# PayTag - Expense Tracker for Couples

A simple Android app to tag and track daily expenses with category-wise breakdown, monthly summaries, and auto-detection of UPI payments.

## Features

- **One-Click Expense Tagging** — Quickly log expenses with note, amount, and category
- **Category-wise Dashboard** — Color-coded pie chart showing spending breakdown by category
- **Monthly Total** — Auto-calculated total from 1st of current month
- **Edit & Delete** — Modify or remove any previous entry
- **Custom Categories** — Add your own categories beyond the defaults
- **PDF Report Sharing** — Generate and share a detailed PDF report of your monthly spending
- **Auto Payment Detection** — Detects payments from PhonePe, GPay, Paytm, BHIM, CRED, etc. via notification listener and prompts you to tag them
- **Colorful Material Design UI** — Gradient header, color-coded categories, card-based layout

## Default Categories

| Category    | Color   |
|-------------|---------|
| Food        | Red     |
| Transport   | Teal    |
| Bill        | Blue    |
| Entertainment | Yellow |
| Shopping    | Purple  |
| Health      | Mint    |
| Education   | Gold    |
| Other       | Green   |

## Tech Stack

- **Language:** Kotlin
- **Database:** Room (SQLite)
- **UI:** Material Design 3, ViewBinding, RecyclerView, CardView
- **Architecture:** LiveData + Flow + Coroutines
- **PDF Generation:** Android PdfDocument API
- **Payment Detection:** NotificationListenerService
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)

## Project Structure

```
PayTag/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/paytag/
│   │   │   ├── MainActivity.kt          # Main dashboard + add/edit/delete
│   │   │   ├── data/
│   │   │   │   ├── Expense.kt           # Room Entity
│   │   │   │   ├── ExpenseDao.kt        # Database queries
│   │   │   │   └── AppDatabase.kt       # Room Database
│   │   │   ├── adapter/
│   │   │   │   ├── ExpenseAdapter.kt    # Transaction list adapter
│   │   │   │   └── CategoryAdapter.kt   # Category breakdown adapter
│   │   │   ├── util/
│   │   │   │   ├── PieChartView.kt      # Custom pie chart view
│   │   │   │   └── PdfHelper.kt         # PDF report generator
│   │   │   └── service/
│   │   │       └── PaymentNotificationListener.kt  # Auto-detect payments
│   │   ├── res/
│   │   │   ├── layout/                  # XML layouts
│   │   │   ├── drawable/                # Icons, gradients
│   │   │   ├── values/                  # Colors, strings, themes
│   │   │   └── xml/                     # File provider paths
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── gradle/wrapper/
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Installation

1. Copy `app-debug.apk` to your Android phone
2. Open the file and tap **Install**
3. Grant notification permission when prompted
4. Enable notification access for auto payment detection (optional)

## Usage

### Adding an Expense
1. Tap the **+ Add Expense** button
2. Enter note, amount, and select category
3. Tap **Save**

### Editing an Expense
- Tap the **pencil icon** on any transaction card
- Modify details and tap **Update**

### Deleting an Expense
- Tap the **trash icon** on any transaction card
- Confirm deletion

### Sharing a Report
- Tap **Share PDF Report** on the monthly summary card
- Choose an app to share (WhatsApp, Email, etc.)

### Auto Payment Detection
1. On first launch, tap **Enable** on the notification access prompt
2. Go to Settings > Notification Access > Turn on **PayTag**
3. Payments from PhonePe, GPay, Paytm, BHIM, CRED, etc. will be auto-detected
4. A notification appears — tap to tag the expense

## Permissions

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Future online sync |
| `POST_NOTIFICATIONS` | Payment detection alerts |
| `WRITE_EXTERNAL_STORAGE` | Save PDF reports (Android 9 and below) |
| Notification Listener | Auto-detect UPI payments |

## Building from Source

```bash
# Set JAVA_HOME to JDK 17
set JAVA_HOME=C:\Program Files\Java\jdk-17

# Set ANDROID_HOME to SDK location
set ANDROID_HOME=%USERPROFILE%\android-sdk

# Build debug APK
gradlew.bat assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## License

This project is for personal use.
