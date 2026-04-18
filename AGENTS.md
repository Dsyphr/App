# Agent Instructions - Dsyphr Android App

## Commands

**Build & Run**
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew clean assembleDebug    # Clean and build
```

**Tests**
```bash
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumentation tests (requires device/emulator)
```

**Other tasks**
```bash
./gradlew dependencies           # Show dependency tree
./gradlew app:dependencies       # Show app module dependencies
```

## Architecture

- **Entry point**: `MainActivity.kt` with Jetpack Navigation
- **Navigation routes**: `login` → `home` → `contact/{username}/{uid}` → `chat`
- **UI**: Jetpack Compose with Material3
- **State**: Firebase Realtime Database
- **Auth**: Firebase Authentication (email) + Google Sign-In
- **Key packages**:
  - `dataClasses/` - User, Message data models
  - `screens/` - Compose screens (login, home, chat, settings)
  - `ui/theme/` - Theme configuration

## Setup Requirements

- **SDK**: Android SDK with API 35, minSdk 26
- **JDK**: Version 17 (for CI), JVM target 11
- **Firebase**: `app/google-services.json` required (not in VCS)
- **Local SDK path**: `/var/home/fine/Android/Sdk` (configured in `local.properties`)

## CI / Release

- Tag format: `v*.*.*` (e.g., `v1.0.0`)
- `google-services.json` decoded from `GOOGLE_SERVICES_JSON` secret
- APK output: `app/build/outputs/apk/debug/*.apk`

## Testing Quirks

- Unit tests: Standard JUnit (run via `./gradlew test`)
- Instrumentation tests: Require Android device/emulator (`./gradlew connectedAndroidTest`)
