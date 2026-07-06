# HabitTracker

HabitTracker is an Android app for building and tracking habits. It is built with Kotlin, Jetpack Compose, Material 3, Hilt, Room, and Jetpack Navigation 3.

## Tech stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation 3
- Hilt dependency injection
- Room persistence
- Lifecycle/ViewModel

## Project structure

```text
app/src/main/java/mk/habittracker/
├── data/          # Data models, database, DAOs, repositories
├── ui/            # Compose screens and UI state
└── HabitTrackerApp.kt
```

## Getting started

### Requirements

- Android Studio
- JDK 11+
- Android SDK 37

### Build

Open the project in Android Studio and run the `app` configuration, or build from the terminal:

```bash
./gradlew :app:assembleDebug
```

### Run tests

```bash
./gradlew test
```

## Current app flow

The app currently uses Navigation 3 with `MainScreen` as the start route. From there, future screens such as adding or viewing habit details can be added as additional `NavKey` routes.

## Notes

- Sample data is stored in `app/src/main/assets/sampleData.sql`.
- The app uses Hilt, so `HabitTrackerApp` must remain registered as the application class in `AndroidManifest.xml`.

## TODO

- [ ] maybe unify add NFC tag bottom sheet?
- [ ] create NFC listener to register tag
- [ ] create NFC listener to add a check-in on read
- [ ] tests
- [ ] create repository abstraction for db
- [ ] remote data source
- [ ] streak calculator
