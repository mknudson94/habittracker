# Add Unit Tests to all :core Modules

This plan outlines the steps to add unit tests to all modules within the `:core` project (`common`, `model`, `database`, `data`, `nfc`, and `ui`).

## Proposed Changes

### Build Configuration

#### [libs.versions.toml](file:///Users/michael/AndroidStudioProjects/HabitTracker/gradle/libs.versions.toml)
- Add testing dependencies: `mockk`, `kotlinx-coroutines-test`, `turbine`, `truth`, and `robolectric`.

### core:common

#### [LocalDateUtils.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/common/src/main/java/com/mk/habittracker/core/common/LocalDateUtils.kt)
- Refactor `previousSevenDaysLabels` to accept an optional `LocalDate` for testability.

#### [NEW] [LocalDateUtilsTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/common/src/test/java/com/mk/habittracker/core/common/LocalDateUtilsTest.kt)
- Test label generation for different dates and locales.

---

### core:model

#### [NEW] [HabitTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/model/src/test/java/com/mk/habittracker/core/model/HabitTest.kt)
- Test `Habit.from()`, `Habit.toMap()`, and equality logic.

---

### core:database

#### [NEW] [ConvertersTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/database/src/test/java/com/mk/habittracker/core/database/ConvertersTest.kt)
- Test Room type converters.

---

### core:data

#### [HabitRepository.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/data/src/main/java/com/mk/habittracker/core/data/HabitRepository.kt)
- Refactor to inject `CoroutineDispatcher` to enable testing with `TestDispatcher`.

#### [NEW] [HabitRepositoryTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/data/src/test/java/com/mk/habittracker/core/data/HabitRepositoryTest.kt)
- Test repository logic, mocking `HabitDao` and Firestore.

---

### core:nfc

#### [NEW] [NfcUtilsTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/nfc/src/test/java/com/mk/habittracker/core/nfc/NfcUtilsTest.kt)
- Test NDEF parsing using Robolectric.

---

### core:ui

#### [NEW] [ThemeTest.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/core/ui/src/test/java/com/mk/habittracker/core/ui/ThemeTest.kt)
- Basic test for theme color/typography consistency.

## Verification Plan

### Automated Tests
- Run all unit tests: `./gradlew test`

### Manual Verification
- None required as this is purely a testing task.
