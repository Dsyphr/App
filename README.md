# Dsyphr

Dsyphr is a real-time chat application that breaks down language barriers through instant translation. Connect with friends and family regardless of the language you speak.

## Download

Download the latest APK from the [Releases](https://github.com/dsyphrco/dsyphr/releases) page.

## Features

- **Real-time Messaging**: Send and receive messages instantly with Firebase Realtime Database
- **Multi-language Support**: Chat in Hindi, Bengali, or English with automatic translation
- **Secure Authentication**: Email-based authentication with email verification
- **Modern UI**: Beautiful Material Design 3 interface built with Jetpack Compose
- **Contact Management**: Add and manage your contacts easily
- **Message Translation**: Automatic translation of messages between supported languages

## Technology Stack

- **UI**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **State Management**: StateFlow and Hilt
- **Backend**: Firebase (Authentication, Realtime Database)
- **Translation**: ML Kit for language translation
- **Dependency Injection**: Hilt
- **Navigation**: Jetpack Navigation Compose

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK with API 35, minSdk 26
- Firebase project with Authentication and Realtime Database enabled

### Setup

1. Clone the repository
2. Create a Firebase project and download `google-services.json`
3. Place `google-services.json` in the `app/` directory
4. Build and run: `./gradlew assembleDebug`

## Architecture

The app follows MVVM architecture with clear separation of concerns:

- **core/**: Domain models, repository interfaces, and core utilities
- **data/**: Firebase implementations and data models
- **presentation/**: UI screens and ViewModels

## Development

### Running Tests

```bash
./gradlew test              # Unit tests
./gradlew lint              # Lint checks
./gradlew ktlintCheck       # Code style
./gradlew detekt            # Static analysis
```

### Building

```bash
./gradlew assembleDebug     # Debug build
./gradlew assembleRelease   # Release build
```

## CI/CD

GitHub Actions runs linting and tests on every PR. Release APKs are built automatically when tags are pushed.

## License

MIT License
