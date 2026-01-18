# Cataract Detector App (KataMata)

A modern Android application built with Kotlin and Jetpack Compose for AI-powered cataract detection using CNN (Convolutional Neural Network).

## 🚀 Features

### Core Functionality
- **AI Analysis**: Real-time cataract detection using a TensorFlow Lite CNN model (`MobileNetV2`).
- **Image Processing**: Advanced image checks for brightness, variance, and edge density to ensure quality analysis.
- **Localization**: Full support for **English** and **Indonesian** languages.
- **User Authentication**: Secure Login and Registration system with session management.
- **History Tracking**: Saves analysis results locally for future reference.

### UI/UX
- **Splash Screen**: Animated welcome screen with app branding.
- **Authentication**: Modern Login and Registration screens with validation and error handling.
- **Home Screen**: Intuitive dashboard for starting new checks or viewing info.
- **Check Screen**: Interactive camera capture and image selection with ROI (Region of Interest) adjustment.
- **Result Screen**: Detailed analysis results with confidence scores, image breakdown, and medical disclaimers.
- **Profile Screen**: User profile management, statistics, and history access.
- **Info Hub**: Educational resources about cataract symptoms, prevention, and treatment.

## 🏗️ Architecture

This project follows the **MVP (Model-View-Presenter)** architecture pattern:

```
app/src/main/java/com/dicoding/cataract_detection_app_final_project/
├── data/                     # Data layer (API, Session, Local Storage)
│   ├── api/                  # Retrofit services and response models
│   └── ...
├── model/
│   └── CataractModel.kt      # TFLite Model integration & Image Processing
├── presenter/
│   ├── AuthPresenter.kt      # Authentication logic
│   ├── HistoryPresenter.kt   # History management
│   └── MainPresenter.kt      # Core app logic
├── view/
│   ├── LoginView.kt          # Login UI
│   ├── HomeView.kt           # Dashboard UI
│   ├── CheckView.kt          # Image capture & analysis UI
│   ├── ResultView.kt         # Results & Breakdown UI
│   └── ...
├── utils/
│   └── ErrorTranslator.kt    # Localization & Error handling
└── MainActivity.kt           # Entry point
```

## 🎨 Design System

- **Material 3**: Modern, accessible design components.
- **Theming**: Custom color palette (Primary Blue #1976D2) with support for Light/Dark modes.
- **Typography**: Complete Material 3 type scale.
- **Adaptive**: Responsive layouts for various screen sizes.

## �️ Technical Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVP
- **ML/AI**: TensorFlow Lite (MobileNetV2)
- **Networking**: Retrofit, OkHttp
- **Image Loading**: Coil
- **Async**: Coroutines

## 📱 Screens

### 1. Authentication
- **Login/Register**: Secure access with email/password.
- **Password Recovery**: Forgot password functionality.

### 2. Dashboard (Home)
- Quick access to Analysis, Info, and Profile.
- "Cataract vs Normal" visual comparison.

### 3. Check & Analysis
- **Camera/Gallery**: Capture or pick images.
- **ROI Selection**: Crop and focus on the eye area.
- **Breakdown**: View technical metrics (Brightness, Variance, Edge Density).

### 4. Results
- **Diagnosis**: Normal, Cataract, or Unknown.
- **Confidence**: Probability percentage of the prediction.
- **Disclaimer**: Medical warning cards detailed advice.

### 5. Profile & History
- Manage account settings.
- View past analysis records with ability to delete entries.
- Change app language (English/Indonesian).

## 📋 Requirements

- Android Studio Hedgehog | 2023.1.1 or later
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Java 17
- Kotlin 2.0+

## 🚀 Getting Started

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   ```

2. **Open in Android Studio**
   - Import the project and wait for Gradle sync.

3. *(Optional)* **Configure Backend**
   - Ensure the local PHP backend is running (if testing Auth).
   - Update `ApiService.kt` base URL if needed.

4. **Build and Run**
   - Run on an emulator or physical device.

## 🔧 Key Dependencies

```kotlin
// UI
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.material3)
implementation(libs.coil.compose)

// Network
implementation(libs.retrofit)
implementation(libs.converter.gson)

// ML
implementation(libs.tensorflow.lite)
implementation(libs.tensorflow.lite.support)
```

## 🔮 Future Enhancements

- [ ] Cloud syncing for history data.
- [ ] Export results to PDF.
- [ ] Dark mode toggle in Settings.
- [ ] Multiple eye condition detection.

## 📄 License

This project is licensed under the MIT License.
