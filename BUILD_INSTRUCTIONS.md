# Street Tycoon - Build Instructions

This guide helps you build the Street Tycoon Android app from source.

## Prerequisites

### Required Software

1. **Java Development Kit (JDK)**
   - Version: JDK 17 or higher
   - Download: https://adoptium.net/
   - Verify: `java -version`

2. **Android SDK**
   - Android Studio (recommended) OR
   - Command-line tools: https://developer.android.com/studio#command-tools
   - Required SDK: API 24-34
   - NDK: r21 or higher (for C++ compilation)
   - CMake: 3.22.1 or higher

3. **Git** (for cloning the repository)
   - Download: https://git-scm.com/

### System Requirements

- **Minimum**: 8GB RAM, 10GB free disk space
- **Recommended**: 16GB RAM, 20GB free disk space
- **OS**: Windows 10+, macOS 10.14+, or Linux (Ubuntu 18.04+)

---

## Quick Start (Using Android Studio)

### 1. Install Android Studio

Download from: https://developer.android.com/studio

During installation, ensure you install:
- Android SDK
- Android SDK Platform (API 34)
- Android Virtual Device (for emulator)

### 2. Install NDK and CMake

1. Open Android Studio
2. Go to **Tools → SDK Manager**
3. Click **SDK Tools** tab
4. Check:
   - ✅ NDK (Side by side)
   - ✅ CMake
5. Click **Apply** and wait for installation

### 3. Clone and Open Project

```bash
git clone https://github.com/yourusername/Street-Tycoon.git
cd Street-Tycoon
```

Open Android Studio → **File → Open** → Select the `Street-Tycoon` folder

### 4. Build

- Click **Build → Make Project** (Ctrl+F9 / Cmd+F9)
- Or click the **Run** button (green triangle) to build and run

---

## Command Line Build (No Android Studio)

### 1. Set Up Environment

#### Windows

```cmd
set ANDROID_HOME=C:\Users\YourName\AppData\Local\Android\Sdk
set PATH=%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\cmdline-tools\latest\bin;%PATH%
```

Add to System Environment Variables for persistence.

#### macOS/Linux

```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
```

Add to `~/.bashrc` or `~/.zshrc` for persistence.

### 2. Install Required SDK Components

```bash
# Install SDK platforms
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"

# Install NDK and CMake
sdkmanager "ndk;26.1.10909125"
sdkmanager "cmake;3.22.1"

# Accept licenses
sdkmanager --licenses
```

### 3. Build Using Gradle Wrapper

#### Windows

```cmd
cd Street-Tycoon
gradlew.bat clean assembleDebug
```

#### macOS/Linux

```bash
cd Street-Tycoon
./gradlew clean assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Install on Device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Troubleshooting

### Issue 1: `org/gradle/api/internal/HasConvention` Error

**Cause**: Gradle version incompatibility

**Solution**: Use the Gradle wrapper (included in project)

```bash
# Windows
gradlew.bat clean build

# macOS/Linux
./gradlew clean build
```

Do NOT use system-installed Gradle (`gradle` command).

### Issue 2: NDK Not Found

**Error**: `NDK is not installed` or `CMake not found`

**Solution**:

1. **Via Android Studio**:
   - Tools → SDK Manager → SDK Tools
   - Install NDK (Side by side) and CMake

2. **Via Command Line**:
   ```bash
   sdkmanager "ndk;26.1.10909125"
   sdkmanager "cmake;3.22.1"
   ```

3. **Set NDK Path** (if needed):
   Add to `local.properties`:
   ```properties
   ndk.dir=/path/to/android/sdk/ndk/26.1.10909125
   ```

### Issue 3: Java Version Mismatch

**Error**: `Unsupported class file major version 61`

**Cause**: Wrong JDK version

**Solution**: Install JDK 17

```bash
# Verify Java version
java -version
# Should show: openjdk version "17.x.x"
```

Set JAVA_HOME:
- **Windows**: `set JAVA_HOME=C:\Program Files\Java\jdk-17`
- **macOS/Linux**: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk`

### Issue 4: Build Cache Corruption

**Symptoms**: Random build failures, inconsistent errors

**Solution**: Clean build

```bash
./gradlew clean
./gradlew --stop  # Stop Gradle daemon
./gradlew assembleDebug
```

Or delete cache manually:
- Windows: `%USERPROFILE%\.gradle\caches`
- macOS/Linux: `~/.gradle/caches`

### Issue 5: Out of Memory

**Error**: `OutOfMemoryError` during build

**Solution**: Increase Gradle memory in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

### Issue 6: Deprecated Warnings

**Warning**: `android.defaults.buildfeatures.buildconfig=true is deprecated`

**Solution**: This has been fixed in the latest code. If you still see it:

1. Open `gradle.properties`
2. Remove the line: `android.defaults.buildfeatures.buildconfig=true`
3. Clean and rebuild

### Issue 7: CMake Version Mismatch

**Error**: `CMake version X.X.X not found`

**Solution**: Install required version or update `CMakeLists.txt`:

```bash
# Install specific version
sdkmanager "cmake;3.22.1"
```

Or update in `app/CMakeLists.txt`:
```cmake
cmake_minimum_required(VERSION 3.18.1)  # Lower version
```

---

## Build Variants

### Debug Build (Default)

```bash
./gradlew assembleDebug
```

- Includes debugging symbols
- Not optimized
- Can be debugged with Android Studio
- Larger APK size

**Output**: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

```bash
./gradlew assembleRelease
```

- Optimized with ProGuard/R8
- Smaller APK size
- Requires signing configuration

**Note**: Release builds need a keystore. See [Signing](#signing-for-release).

---

## Signing for Release

### 1. Generate Keystore

```bash
keytool -genkey -v -keystore street-tycoon.keystore -alias street-tycoon -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Configure Signing

Create `keystore.properties` (add to `.gitignore`):

```properties
storeFile=../street-tycoon.keystore
storePassword=your_password_here
keyAlias=street-tycoon
keyPassword=your_key_password_here
```

### 3. Update `app/build.gradle.kts`

Add signing config:

```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            // ...
        }
    }
}
```

### 4. Build Signed APK

```bash
./gradlew assembleRelease
```

**Output**: `app/build/outputs/apk/release/app-release.apk`

---

## Building AAB (App Bundle) for Play Store

Google Play requires App Bundles (AAB) for new apps:

```bash
./gradlew bundleRelease
```

**Output**: `app/build/outputs/bundle/release/app-release.aab`

Upload this file to Google Play Console.

---

## Running on Device/Emulator

### Option 1: Android Studio

1. Connect device via USB (enable USB debugging)
2. Or start an emulator
3. Click **Run** button (green triangle)

### Option 2: Command Line

```bash
# List connected devices
adb devices

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.streettycoon/.MainActivity

# View logs
adb logcat | grep StreetTycoon
```

---

## Build Configuration

### Current Versions

- **Gradle**: 8.9 (via wrapper)
- **Android Gradle Plugin**: 8.5.2
- **Kotlin**: 2.0.21
- **Compose Compiler**: 1.5.15
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **NDK**: r26+ recommended

### Dependencies

See `app/build.gradle.kts` for full list. Key dependencies:

- Jetpack Compose BOM 2023.10.01
- Room 2.6.1
- Coroutines 1.7.3
- AdMob 22.6.0
- Billing 6.1.0

---

## Performance Tips

### Faster Builds

1. **Enable Gradle Daemon** (already enabled in `gradle.properties`):
   ```properties
   org.gradle.daemon=true
   org.gradle.parallel=true
   org.gradle.caching=true
   ```

2. **Increase Memory**:
   ```properties
   org.gradle.jvmargs=-Xmx4096m
   ```

3. **Use Build Cache**:
   ```bash
   ./gradlew --build-cache assembleDebug
   ```

4. **Exclude Unused ABIs** (for testing):
   In `app/build.gradle.kts`:
   ```kotlin
   ndk {
       abiFilters.addAll(listOf("arm64-v8a"))  // Only one ABI
   }
   ```

### Clean Build (When Needed)

```bash
./gradlew clean
./gradlew --stop
./gradlew assembleDebug --refresh-dependencies
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Android CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}

      - name: Build with Gradle
        run: ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

---

## Getting Help

### Gradle Issues

```bash
# Run with stacktrace
./gradlew assembleDebug --stacktrace

# Run with debug info
./gradlew assembleDebug --debug

# Check Gradle version
./gradlew --version
```

### Android Studio Issues

1. **File → Invalidate Caches / Restart**
2. **Build → Clean Project**
3. **Build → Rebuild Project**

### Common Commands

```bash
# List all tasks
./gradlew tasks

# Check dependencies
./gradlew app:dependencies

# Run tests
./gradlew test

# Run instrumented tests (requires device)
./gradlew connectedAndroidTest
```

---

## Additional Resources

- **Android Developer Guide**: https://developer.android.com/guide
- **Gradle Documentation**: https://docs.gradle.org/
- **Kotlin Documentation**: https://kotlinlang.org/docs/
- **NDK Guide**: https://developer.android.com/ndk/guides
- **Project README**: See `README.md` for architecture details

---

## Support

For build issues:
1. Check this guide's Troubleshooting section
2. Search GitHub Issues: https://github.com/yourusername/Street-Tycoon/issues
3. Create a new issue with:
   - OS and version
   - Gradle version (`./gradlew --version`)
   - Full error message
   - Steps to reproduce

---

**Last Updated**: November 2024
**Tested On**: Windows 11, macOS Sonoma, Ubuntu 22.04
