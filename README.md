# Street Tycoon - Android Game MVP

A lightweight, offline-first incremental tycoon game where players grow a chain of themed street stalls across a stylized Indian city. Built with Kotlin (UI/app logic) and C++ (deterministic game simulation via NDK).

## Features

### Core Gameplay
- **Tap-to-serve mechanics**: Manually serve customers by tapping
- **Auto-serve helpers**: Hire helpers for passive income generation
- **Progression system**: Upgrade stalls, unlock zones, and expand your empire
- **Offline earnings**: Earn money even when you're away (up to 4 hours, 70% efficiency)
- **Daily rewards**: Claim increasing rewards for consecutive days

### Technical Highlights
- **Hybrid Architecture**: Kotlin for UI, C++ for deterministic game simulation
- **Offline-first**: Local storage with Room database
- **Native Performance**: C++ simulation with JNI bridge for optimal performance
- **Deterministic Simulation**: Tick-based game logic ensures consistent behavior
- **JSON State Management**: Snapshot-based save/load system
- **Jetpack Compose UI**: Modern declarative UI framework

### Game Content (MVP)
- **6 City Zones**: Progressive unlock system
- **4 Stall Types**: Tea, Dosa, Momos, and Juice stalls
- **3 Upgrade Tiers**: Per stall with exponential progression
- **Helper System**: Hire multiple helpers per stall for passive income
- **Monetization**: Rewarded ads, IAP token packs, cosmetic stall skins

### Localization
- English (default)
- Hindi (हिन्दी)
- Kannada (ಕನ್ನಡ)

## Architecture

### Tech Stack
- **Language**: Kotlin (UI) + C++ (Simulation)
- **UI Framework**: Jetpack Compose
- **Database**: Room (local persistence)
- **Native Integration**: Android NDK with JNI
- **Build System**: Gradle with CMake for native code
- **Monetization**: Google AdMob (ads) + Google Play Billing (IAP)

### Architecture Diagram

```
┌─────────────────────────────────────────────────┐
│              Jetpack Compose UI                  │
│  (MapScreen, StallScreen, ShopScreen)           │
└───────────────────┬─────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────┐
│              GameViewModel                       │
│  - Tick loop coroutine (100ms intervals)        │
│  - Auto-save (30s intervals)                    │
│  - State management (Flow)                      │
└───────────────────┬─────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────┐
│           GameRepository (Room)                  │
│  - GameSnapshotEntity (JSON storage)            │
│  - PlayerPrefsEntity (settings)                 │
└───────────────────┬─────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────┐
│         GameSimulation (Kotlin Wrapper)          │
│  - JNI bridge to native simulation              │
│  - JSON serialization/deserialization           │
└───────────────────┬─────────────────────────────┘
                    │ (JNI)
┌───────────────────▼─────────────────────────────┐
│      Native C++ Game Simulation                  │
│  - GameState (authoritative state)              │
│  - tick(deltaMs) - deterministic updates        │
│  - applyAction(json) - player actions           │
│  - getSnapshot() - state export                 │
└─────────────────────────────────────────────────┘
```

### Data Flow

1. **UI → ViewModel**: User actions (tap serve, upgrade, etc.)
2. **ViewModel → Native Sim**: Action JSON sent via JNI
3. **Native Sim**: Processes action, updates state deterministically
4. **Native Sim → ViewModel**: Returns action result (success/failure)
5. **ViewModel → UI**: State snapshot (100ms tick updates UI)
6. **ViewModel → Repository**: Auto-save every 30s to Room DB

### JNI API Surface

The JNI bridge is kept minimal for performance:

```kotlin
// Native methods
external fun nativeCreate(): Long
external fun nativeDestroy(handle: Long)
external fun nativeInitializeNewGame(handle: Long)
external fun nativeInitializeFromJson(handle: Long, json: String): Boolean
external fun nativeTick(handle: Long, deltaTimeMs: Long)
external fun nativeGetSnapshot(handle: Long): String
external fun nativeApplyAction(handle: Long, actionJson: String): String
external fun nativeCalculateOfflineEarnings(handle: Long, offlineTimeMs: Long): Double
```

## Project Structure

```
Street-Tycoon/
├── app/
│   ├── build.gradle.kts          # App-level build config with NDK
│   ├── CMakeLists.txt            # CMake config for C++ compilation
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── cpp/                  # C++ native simulation
│   │   │   ├── game_simulation.cpp/h
│   │   │   ├── game_state.cpp/h
│   │   │   ├── json_serializer.cpp/h
│   │   │   └── jni_bridge.cpp
│   │   ├── java/com/streettycoon/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/             # Room database
│   │   │   │   ├── GameDatabase.kt
│   │   │   │   └── GameRepository.kt
│   │   │   ├── game/
│   │   │   │   ├── model/        # Data classes
│   │   │   │   │   └── GameModels.kt
│   │   │   │   └── native/       # JNI wrapper
│   │   │   │       └── GameSimulation.kt
│   │   │   ├── monetization/     # Ads & IAP
│   │   │   │   ├── AdsManager.kt
│   │   │   │   └── IAPManager.kt
│   │   │   ├── ui/
│   │   │   │   ├── GameViewModel.kt
│   │   │   │   ├── navigation/
│   │   │   │   │   └── Navigation.kt
│   │   │   │   ├── screens/
│   │   │   │   │   ├── MapScreen.kt
│   │   │   │   │   ├── StallScreen.kt
│   │   │   │   │   └── ShopScreen.kt
│   │   │   │   └── theme/
│   │   │   │       ├── Theme.kt
│   │   │   │       └── Type.kt
│   │   │   └── utils/
│   │   │       └── ShareUtils.kt
│   │   └── res/
│   │       ├── values/           # English strings
│   │       ├── values-hi/        # Hindi strings
│   │       ├── values-kn/        # Kannada strings
│   │       └── xml/
├── build.gradle.kts              # Project-level build config
├── settings.gradle.kts
└── README.md
```

## Setup Instructions

### Prerequisites

1. **Android Studio**: Arctic Fox (2020.3.1) or later
2. **Android NDK**: Version 21.0+ (install via Android Studio SDK Manager)
3. **CMake**: Version 3.22.1+ (install via SDK Manager)
4. **Minimum SDK**: API 24 (Android 7.0)
5. **Target SDK**: API 34 (Android 14)

### Installation Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/Street-Tycoon.git
   cd Street-Tycoon
   ```

2. **Open in Android Studio**:
   - File → Open → Select the `Street-Tycoon` directory
   - Wait for Gradle sync to complete

3. **Install NDK and CMake** (if not already installed):
   - Tools → SDK Manager → SDK Tools tab
   - Check "NDK (Side by side)" and "CMake"
   - Click "Apply" to install

4. **Build the project**:
   ```bash
   ./gradlew build
   ```

5. **Run on device/emulator**:
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio or:
   ```bash
   ./gradlew installDebug
   ```

### Build Variants

- **Debug**: Development build with logging enabled
- **Release**: Production build with ProGuard/R8 optimization

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease
```

## Game Design

### Progression Balance

#### Starting State
- Initial cash: ₹100
- 1 Tea Stall (unlocked in Zone 0 - Marketplace)
- Tap income: ₹1 per serve
- Passive income: ₹0/s

#### Upgrade Costs
- Stall upgrade cost: `baseCost * (1.15 ^ level)`
- Helper hire cost: `baseCost * (1.3 ^ helperCount)`
- Zone unlock costs: 0, 500, 2000, 5000, 10000, 25000

#### Income Scaling
- Tap income: `baseTapIncome * (1 + (level - 1) * 0.5)`
- Helper income: `baseIncome * 0.5` per helper
- Total stall income: `helperIncome * (1 + (level - 1) * 0.5)`

#### Offline Accrual
- Maximum offline time: 4 hours
- Efficiency penalty: 70% of online earnings
- Formula: `totalIncomePerSecond * offlineSeconds * 0.7`

### Monetization Strategy

#### Rewarded Ads
- **2x Earnings Boost**: 1 hour of doubled income
- **Instant Upgrade**: Skip upgrade wait time
- **Offline Earnings Boost**: Claim additional offline earnings

#### In-App Purchases
- **Small Token Pack**: 100 tokens for $0.99
- **Medium Token Pack**: 550 tokens for $4.99 (10% bonus)
- **Large Token Pack**: 1500 tokens for $9.99 (25% bonus)

#### Token Uses
- Cosmetic stall skins (50-100 tokens)
- Special event access
- Premium helpers

## Testing

### Unit Tests

```bash
# Run Kotlin unit tests
./gradlew test

# Run instrumented tests on device
./gradlew connectedAndroidTest
```

### Performance Testing

Test on low-end devices:
- Minimum: 1GB RAM, ARM7 processor
- Target: 60 FPS UI, <100ms tick latency

### Native Simulation Tests

C++ simulation can be tested independently:
1. Deterministic behavior (same inputs → same outputs)
2. Offline accrual calculations
3. Upgrade cost scaling
4. Save/load round-trip integrity

## Deployment

### Pre-Launch Checklist

- [ ] Replace AdMob test IDs with production IDs
- [ ] Set up Google Play Console IAP products
- [ ] Configure signing keys for release builds
- [ ] Test on multiple devices (low-end, mid-range, high-end)
- [ ] Verify all localizations
- [ ] Test offline functionality
- [ ] Ensure save/load works correctly
- [ ] Test IAP and ad integrations

### Release Build

1. **Configure signing** in `app/build.gradle.kts`:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file("your-keystore.jks")
           storePassword = "your-password"
           keyAlias = "your-alias"
           keyPassword = "your-password"
       }
   }
   ```

2. **Build release APK**:
   ```bash
   ./gradlew assembleRelease
   ```

3. **Build App Bundle** (recommended for Play Store):
   ```bash
   ./gradlew bundleRelease
   ```

## Roadmap

### Phase 1: MVP (Weeks 1-4) ✅
- ✅ Core game loop (tap-to-serve + helpers)
- ✅ 6 zones with 4 stall types each
- ✅ Local persistence with Room
- ✅ Offline earnings
- ✅ Daily rewards
- ✅ Basic monetization hooks

### Phase 2: Content & Polish (Weeks 5-6)
- [ ] More stall types (10+ varieties)
- [ ] Quest system with objectives
- [ ] Festival events (time-limited)
- [ ] Sound effects and background music
- [ ] Enhanced animations and particle effects
- [ ] Tutorial/onboarding flow

### Phase 3: Social & Retention (Weeks 7-8)
- [ ] Leaderboards (per zone, global)
- [ ] Achievement system
- [ ] Social sharing improvements
- [ ] Cloud save backup
- [ ] Analytics integration
- [ ] A/B testing framework

### Phase 4: Post-Launch
- [ ] Soft prestige system
- [ ] Special celebrity promo stalls
- [ ] Seasonal themes
- [ ] Mini-games
- [ ] Guilds/communities

## Performance Optimization

### Native Simulation
- Runs on background thread (Dispatchers.Default)
- Minimal JNI calls (snapshot every 100ms)
- Efficient JSON serialization
- Deterministic tick-based logic

### UI Optimization
- Lazy loading for long lists
- State hoisting to avoid recompositions
- Remember for expensive calculations
- Proper coroutine lifecycle management

## Troubleshooting

### Common Issues

**NDK not found**:
```
Error: NDK is not installed
```
Solution: Install NDK via SDK Manager (Tools → SDK Manager → SDK Tools)

**CMake version mismatch**:
```
Error: CMake version X.X.X not found
```
Solution: Update CMake version in `app/build.gradle.kts` or install required version

**JNI library not loaded**:
```
java.lang.UnsatisfiedLinkError: dlopen failed
```
Solution: Clean and rebuild project (`./gradlew clean build`)

**Room schema export**:
```
Warning: Schema export directory not found
```
Solution: Add schema location in `build.gradle.kts` or disable export

## Contributing

This is an MVP project. Contributions are welcome!

### Guidelines
1. Follow Kotlin coding conventions
2. Write unit tests for new features
3. Update documentation for API changes
4. Test on multiple devices before PR

## License

Copyright (c) 2024. All rights reserved.

This is a demonstration project for educational purposes.

## Contact

For questions or support:
- GitHub Issues: https://github.com/yourusername/Street-Tycoon/issues
- Email: your.email@example.com

---

**Built with ❤️ for the Indian mobile gaming community**
