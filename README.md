# 🦻 Live Hearing App (WeHear Ecosystem)

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Hilt](https://img.shields.io/badge/DI-Hilt-orange?style=for-the-badge)

> **A Smart Hearing Aid application powered by Android's Audio Engine.** > *Experience low-latency active listening, background environment analysis, and precise audio balancing.*

---

## 📥 Download

Get the latest APK directly from GitHub releases.

<a href="https://github.com/AvikMakwana/LiveHearingApp/releases/latest">
  <img src="https://img.shields.io/badge/Download_APK-WeHear_Smart_Mode-00E5FF?style=for-the-badge&logo=github&labelColor=1E2129" height="50">
</a>

---

## ✨ Features

This app converts your Android device into a **Smart Hearing Aid** using advanced audio processing.

* **🎧 Live Hearing Mode:** Amplifies surrounding sound in real-time using `AudioRecord` and `AudioTrack` low-latency pipelines.
* **🔉 Background Service:** Runs continuously in the background (Foreground Service) even when the screen is off.
* **🎚️ Left/Right Balance Control:** Software-based panning allows users to focus audio on their left or right ear (ideal for unilateral hearing loss).
* **🌊 Dynamic Visualizer:** A beautiful, reactive "WeHear Ripple" animation that pulses with sound amplitude.
* **🛡️ Feedback Safety:** Intelligent detection prevents audio feedback loops by ensuring headphones are connected before activation.
* **🎨 Premium UI:** "Dark Mode" native design using **Jetpack Compose**, featuring the WeHear brand aesthetic.

---

## 🛠️ Tech Stack & Architecture

Built with **Clean Architecture** principles and modern Android standards.

* **Language:** Kotlin (100%)
* **UI:** Jetpack Compose (Material3)
* **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
* **Dependency Injection:** Dagger Hilt
* **Concurrency:** Kotlin Coroutines & Flow
* **Audio Engine:** Low-level PCM Byte Stream processing (Mono Mic -> Stereo Output)
* **Background Processing:** Android Foreground Services

### Project Structure
```text
com.avikmakwana.livehearingapp
├── domain           # Audio Engine (AudioRecord/AudioTrack Logic)
├── service          # Foreground Service (Notification & Background process)
├── ui
│   ├── screens      # Jetpack Compose Screens
│   ├── theme        # WeHear Theme (Colors, Type)
│   └── viewmodel    # Hilt ViewModels
└── di               # Hilt Modules
