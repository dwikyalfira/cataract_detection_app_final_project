# Cataract Detector App

A modern Android application built with Kotlin and Jetpack Compose for AI-powered cataract detection using CNN (Convolutional Neural Network).

## 🚀 Features

### Current Implementation (UI Only)
- **Splash Screen**: Beautiful welcome screen with app logo and branding
- **Home Screen**: Main interface with image upload/capture functionality
- **Result Screen**: Displays detection results with confidence scores
- **Info Screen**: Comprehensive information about cataracts
- **Profile Screen**: User profile and app statistics

### Planned Features (Future Implementation)
- CNN model integration for actual cataract detection
- Real image processing and analysis
- Cloud-based model updates
- User history and tracking
- Export and sharing capabilities

## 🏗️ Architecture

This project follows the **MVP (Model-View-Presenter)** architecture pattern:

```
app/src/main/java/com/dicoding/cataract_detection_app_final_project/
├── model/
│   └── CataractModel.kt          # Placeholder for CNN model
├── presenter/
│   └── MainPresenter.kt          # Business logic and navigation
├── view/
│   ├── SplashView.kt             # Splash screen UI
│   ├── HomeView.kt               # Main home screen UI
│   ├── ResultView.kt             # Results display UI
│   ├── InfoView.kt               # Information screen UI
│   └── ProfileView.kt            # Profile screen UI
├── ui/theme/
│   ├── Color.kt                  # Color definitions
│   ├── Theme.kt                  # Material 3 theme
│   └── Type.kt                   # Typography definitions
└── MainActivity.kt               # Main activity with navigation
```

## 🎨 Design System

- **Material 3 (Material You)**: Modern design system
- **Primary Color**: Blue (#1976D2)
- **Typography**: Material 3 typography scale
- **Icons**: Material Design icons
- **Cards**: Rounded corners with elevation
- **Responsive**: Adapts to different screen sizes

## 📱 Screens

### 1. Splash Screen
- App logo with eye icon
- App name: "Cataract Detector"
- Blue background (#1976D2)
- Auto-navigation to Home after 2.5 seconds

### 2. Home Screen
- Title and image preview area
- Upload Image button (dummy)
- Capture Image button (dummy)
- Navigation to Info and Profile screens
- Loading indicator during processing

### 3. Result Screen
- Dummy analyzed image display
- Prediction result with color coding
- Confidence score display
- Analysis summary
- Back to Home navigation

### 4. Info Screen
- Comprehensive cataract information
- Scrollable content sections:
  - What is cataract?
  - Common symptoms
  - Prevention tips
  - Risk factors
  - Treatment options

### 5. Profile Screen
- User profile picture (circular)
- User information (name, email, etc.)
- App statistics (scans, healthy results, alerts)
- Back to Home navigation

## 🛠️ Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVP (Model-View-Presenter)
- **Navigation**: Custom state-based navigation
- **Theme**: Material 3
- **Icons**: Material Design Icons
- **Build System**: Gradle with Kotlin DSL

## 📋 Requirements

- Android Studio Hedgehog | 2023.1.1 or later
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36 (Android 14)
- Java 11 or later
- Kotlin 2.0.21

## 🚀 Getting Started

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd cataract_detection_app_final_project
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory
   - Click "OK"

3. **Sync and Build**
   - Wait for Gradle sync to complete
   - Build the project (Build → Make Project)
   - Run on an emulator or physical device

4. **Run the App**
   - Click the "Run" button (green play icon)
   - Select your target device
   - The app will install and launch

## 🔧 Dependencies

The project uses the following key dependencies:

```kotlin
// Core Android
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.lifecycle.runtime.ktx)
implementation(libs.androidx.activity.compose)

// Compose
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.ui)
implementation(libs.androidx.ui.graphics)
implementation(libs.androidx.ui.tooling.preview)
implementation(libs.androidx.material3)

// Navigation
implementation(libs.androidx.navigation.compose)
```

## 🎯 Navigation Flow

```
Splash Screen (2.5s)
    ↓
Home Screen
    ↓
├── Upload/Capture Image → Result Screen → Home
├── Info Button → Info Screen → Home
└── Profile Button → Profile Screen → Home
```

## 🔮 Future Enhancements

### Phase 1: CNN Integration
- [ ] Integrate TensorFlow Lite
- [ ] Implement actual image processing
- [ ] Add model loading and inference
- [ ] Real-time camera integration

### Phase 2: Advanced Features
- [ ] User authentication
- [ ] Cloud storage for results
- [ ] Export functionality
- [ ] Sharing capabilities
- [ ] Offline mode

### Phase 3: Analytics & ML
- [ ] Usage analytics
- [ ] Model performance tracking
- [ ] A/B testing for UI improvements
- [ ] Personalized recommendations

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Developer**: [Your Name]
- **Project**: Final Project for Android Development Course
- **Institution**: Dicoding Indonesia

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation

---

**Note**: This is currently a UI prototype. The CNN model integration will be implemented in future phases.

