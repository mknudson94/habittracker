# Walkthrough - Unit Tests for :core Modules

I have successfully added unit tests to all modules within the `:core` project. This involved refactoring some components for better testability and setting up a robust testing infrastructure.

## Changes Overview

### Build Configuration
- Added `MockK`, `Kotlinx Coroutines Test`, `Turbine`, `Truth`, and `Robolectric` to `libs.versions.toml`.
- Configured each `:core` module's `build.gradle.kts` with the necessary test dependencies.

### core:common
- Refactored [LocalDateUtils.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/common/src/main/java/com/mk/habittracker/core/common/LocalDateUtils.kt) to allow injecting a reference date for testing.
- Added [LocalDateUtilsTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/common/src/test/java/com/mk/habittracker/core/common/LocalDateUtilsTest.kt).

### core:model
- Added [HabitTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/model/src/test/java/com/mk/habittracker/core/model/HabitTest.kt) to verify data mapping and equality logic.

### core:database
- Fixed a column name mismatch in [HabitDao.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/database/src/main/java/com/mk/habittracker/core/database/HabitDao.kt) that caused KSP errors.
- Added [ConvertersTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/database/src/test/java/com/mk/habittracker/core/database/ConvertersTest.kt).

### core:data
- Significant refactor of [HabitRepository.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/data/src/main/java/com/mk/habittracker/core/data/HabitRepository.kt) to inject `FirebaseFirestore`, `FirebaseAuth`, and `CoroutineDispatcher`.
- Updated [AppModule.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/app/src/main/java/com/mk/habittracker/AppModule.kt) to provide these new dependencies.
- Added [HabitRepositoryTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/data/src/test/java/com/mk/habittracker/core/data/HabitRepositoryTest.kt).

### core:nfc
- Added [NfcUtilsTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/nfc/src/test/java/com/mk/habittracker/core/nfc/NfcUtilsTest.kt) using Robolectric to test intent parsing.

### core:ui
- Added [ThemeTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/ui/src/test/java/com/mk/habittracker/core/ui/ThemeTest.kt).

## Verification Results

### Automated Tests
I verified the changes by running all unit tests in the project:
`./gradlew test`

All tests passed successfully across all core modules.

> [!NOTE]
> Robolectric tests were configured to run with SDK 34 to avoid compatibility issues with the project's target SDK 37.
