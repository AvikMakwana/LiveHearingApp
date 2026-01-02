# 🦻 WeHear – Smart Hearing Assistant for Android

> **Hear more. Hear better. Hear smarter.**

WeHear transforms your Android device into a **powerful smart hearing aid**, combining low-latency audio processing, intelligent sound balancing, and a beautifully crafted user experience.

Designed for accessibility, clarity, and comfort — WeHear helps you stay connected to the world around you.

---

## ⬇️ Download Latest APK

👉 **[Download Latest Release](https://github.com/AvikMakwana/LiveHearingApp/releases/latest)**  

Always download the most recent APK from the official GitHub Releases page to ensure you get the latest features and fixes.

---

## ✨ Key Features

### 🎧 Live Hearing Mode  
Real-time sound amplification using ultra-low-latency `AudioRecord` and `AudioTrack` pipelines.

### 🔉 Background Listening  
Runs seamlessly as a **Foreground Service**, even when the screen is locked.

### 🎚️ Left–Right Audio Balance  
Fine-tuned stereo control to assist users with unilateral hearing loss.

### 🌊 Dynamic Sound Visualizer  
A fluid, animated waveform that reacts instantly to environmental audio.

### 🛡️ Smart Feedback Protection  
Automatically prevents audio feedback by detecting unsafe conditions.

### 🎨 Modern & Clean UI  
Crafted with **Jetpack Compose**, featuring a polished dark-mode-first design.

---

## 🧠 Technology Stack

| Layer | Tech |
|------|------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Dagger Hilt |
| Concurrency | Kotlin Coroutines & Flow |
| Audio Engine | PCM Audio Processing |
| Background Tasks | Foreground Services |

---

## 🗂️ Project Structure

```
com.avikmakwana.livehearingapp
├── domain          # Core audio processing logic
├── service         # Foreground audio service
├── ui
│   ├── screens     # Compose UI screens
│   ├── theme       # App theming & colors
│   └── viewmodel   # ViewModels
└── di              # Dependency injection
```

---

## 🚀 Getting Started

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/AvikMakwana/LiveHearingApp.git
```

### 2️⃣ Open in Android Studio  
Use **Android Studio Hedgehog or newer**

### 3️⃣ Run on Device  
> ⚠️ A physical device with wired or Bluetooth headphones is required.

---

## 🤝 Contributing

We welcome all contributions ❤️

1. Fork the repository  
2. Create a feature branch  
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Commit your changes  
   ```bash
   git commit -m "Add your feature"
   ```
4. Push and open a Pull Request  

---

## 📜 License

This project is licensed under the **MIT License**.

---

<p align="center">
  <strong>WeHear</strong> • Designed & Developed by <strong>Avik Makwana</strong>  
</p>
