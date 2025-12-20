# 🤖 Loyalty Points - Android App

Welcome to the **Loyalty Points Android App**! This is the mobile interface that customers use to log in and see their loyalty status. It's built with modern "Jetpack Compose" technology, which makes the app feel smooth and responsive.

---

## 🛠️ Prerequisites & Setup

To run this app on your computer, you need:
1.  **Android Studio** (Ladybug or later recommended).
2.  **Java 17 SDK**: Required by modern Android build systems.
3.  **Android SDK 34**: The specific version of Android the app is built for.

### First-Time Setup
1.  Open **Android Studio**.
2.  Go to `File > Open` and select this `loyalty-points-android` folder.
3.  Wait for the "Gradle Sync" to finish. This might take a few minutes as it downloads all the necessary "ingredients" for the first time.

---

## 🌟 What does this app do?
This is the "face" of the project. It allows a user to:
1.  **Securely Log In**: Enter their username and password.
2.  **Stay Informed**: See a clear red banner if the internet is down.
3.  **Prevent Errors**: The login button won't even let you click it if your password is too short or if you're offline!
4.  **Auto-Remember**: It can remember your username for next time.

---

## 📂 Key Files & What They Do

### `LoginScreen.kt`
The **Drawing Table**. This file describes exactly what the screen looks like—the buttons, the text fields, and the colors.

### `LoginViewModel.kt`
The **Mood Manager**. This is the cleverest part of the app. It decides what the screen should show right now. Is it loading? Should an error message pop up? Is the user offline?

### `AuthRepository.kt`
The **Safe Keeper**. This file handles the actual data. It knows how to "talk" to the login server and how to save your username securely on the phone.

---

## 🚀 How to Run (No Tech Help Needed!)

1.  **Open in Android Studio**.
2.  **Start a Phone**: In Android Studio, look for the "Device Manager" (phone icon) and start an emulator (like a Pixel 7).
3.  **Click Run**: Hit the green "Play" button (▶️) at the top.

### Using the Terminal
If you're already in a terminal, you can build the app files with this command from this folder:
```bash
./gradlew assembleDebug
```

---

## ✅ How we know it's working
We have a team of "digital robots" (automated tests) that check the app for bugs. To run them all and see a report, use this command:
```bash
./gradlew jacocoTestReport
```
If it finishes successfully, the app is "Safe to Fly"!
