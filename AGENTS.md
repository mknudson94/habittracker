# AGENTS.md

## Project Overview

This is an Android application built with Kotlin and Jetpack Compose.

Core technologies and architectural characteristics:

* Kotlin
* Jetpack Compose
* Material 3
* Single-Activity architecture
* Hilt for dependency injection
* Navigation 3 for navigation
* AndroidX / Jetpack libraries
* Gradle Kotlin DSL

Prefer existing project patterns and abstractions over introducing new ones. Before adding a new abstraction, dependency, or architectural pattern, look for an established equivalent elsewhere in the codebase.

## General Engineering Principles

* Keep changes focused on the requested task.
* Do not modify unrelated files or refactor unrelated code unless necessary.
* Prefer simple solutions over unnecessary abstraction.
* Follow existing naming, package, module, and architectural conventions.
* Preserve existing behavior unless the task explicitly requires changing it.
* Favor immutable state and unidirectional data flow.
* Keep business logic out of Composables.
* Avoid introducing global state or service locators.
* Prefer Kotlin/Android standard library and existing project dependencies before adding a new dependency.
* Do not add dependencies solely to solve a small problem that can reasonably be solved with existing tools.
* Do not suppress compiler, lint, or static-analysis warnings without a concrete reason.

## Architecture

Follow the architecture already established in the project.

### Modularization

The project is organized into `feature/` and `core/` modules:
* **feature/**: Contains UI and screen-specific logic. Each feature should be self-contained where possible.
* **core/**: Contains shared logic, data sources, and models used across multiple features (e.g., `core:data`, `core:database`, `core:ui`).

Core modules should never depend on feature modules. Feature modules should never depend on other feature modules. 

The general dependency direction should remain:

```text
UI
  ↓
ViewModel / Presentation
  ↓
Domain / Use Cases (when present)
  ↓
Repositories
  ↓
Data Sources / APIs / Database
```

Keep responsibilities separated:

* **Composables** render state and emit user events. They should not contain business or persistence logic.
* **ViewModels** own screen-level UI state and coordinate asynchronous work.
* **Repositories** abstract data access and coordinate local/remote sources.
* **Data sources** handle concrete persistence and network operations.
* **Domain logic** belongs in the appropriate domain layer when it is sufficiently complex or reused.

Do not introduce a domain/use-case layer merely for the sake of adding one. Match the existing project structure.

## Compose

* Prefer stateless Composables where practical.
* Hoist state to the lowest appropriate owner.
* Keep transient UI state local to the Composable when it does not need to survive configuration changes or be shared.
* Keep screen/business state in the appropriate ViewModel.
* Prefer Material 3 components and theming already used by the project.
* Reuse existing components before creating new ones.
* Follow existing spacing, typography, and theming conventions.
* Avoid putting repository, database, network, or navigation logic directly in Composables.
* Use stable, immutable UI models where practical.
* Avoid unnecessary recomposition-sensitive allocations inside frequently recomposed content.
* Prefer `collectAsStateWithLifecycle()` over `collectAsState()` for consuming `Flow`s in Composables.

### Reference Implementations

* **Feature Structure**: [HabitDetailScreen.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/feature/habit-detail/src/main/java/com/mk/habittracker/feature/habitdetail/HabitDetailScreen.kt) and [HabitDetailViewModel.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/feature/habit-detail/src/main/java/com/mk/habittracker/feature/habitdetail/HabitDetailViewModel.kt) serve as the "Gold Standard" for screen structure and state management.

When adding a new screen, follow the established screen structure and navigation patterns rather than creating a parallel pattern.

## State and Coroutines

* Use structured concurrency.
* Prefer `CoroutineScope`/`suspend` APIs that are owned by an appropriate lifecycle.
* Do not create unmanaged global coroutine scopes.
* ViewModel work should generally be scoped to the ViewModel lifecycle.
* Prefer `StateFlow`/`Flow` for observable state when consistent with existing project patterns.
* Keep UI state explicit and represent loading, success, empty, and error states where appropriate.
* Avoid using exceptions as ordinary control flow.
* Handle cancellation correctly; do not swallow `CancellationException`.
* Do not perform blocking I/O on the main thread.
* Prefer lifecycle-aware collection from Compose.

For long-running or persistent work, use the appropriate Android background-work mechanism rather than relying on a ViewModel or Activity remaining alive.

## Navigation

The app uses Navigation 3 (`androidx.navigation3`).

* **Entry Point**: The central navigation graph is defined in [AppNavigation.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/app/src/main/java/com/mk/habittracker/ui/AppNavigation.kt).
* Follow the existing Nav3 navigation architecture and entry-point patterns using `@Serializable` route objects.
* Keep navigation decisions out of low-level UI components where practical.
* Pass stable identifiers or navigation arguments rather than large mutable objects.
* Avoid coupling unrelated screens through shared mutable navigation state.
* Follow existing back-stack and navigation-state conventions.

Do not introduce Navigation Component APIs from older navigation patterns.

## Dependency Injection

The app uses Hilt.

* Prefer constructor injection.
* **ViewModel Injection**: For ViewModels requiring navigation arguments, use `@AssistedInject` and `@AssistedFactory`. See [HabitDetailViewModel.kt](file:///Users/michael/AndroidStudioProjects/HabitTracker/feature/habit-detail/src/main/java/com/mk/habittracker/feature/habitdetail/HabitDetailViewModel.kt) for reference.
* Use Hilt modules for dependencies that cannot use constructor injection.
* Match the existing component/scoping conventions.
* Avoid injecting dependencies into Composables directly.
* Do not introduce service locators or manual global dependency registries.
* Do not create unnecessarily broad singleton-scoped objects.

When adding a dependency, place it in the narrowest appropriate scope and verify that its lifetime matches its intended usage.

## Persistence and Data

* Follow the existing database and persistence architecture using **Room**.
* Keep persistence concerns inside the data layer.
* Prefer strongly typed models over passing raw maps or loosely typed values.
* Treat persisted data formats and migrations as compatibility concerns.
* Do not make destructive schema changes without an explicit migration strategy.
* Prefer repository abstractions when UI code should not know where data originates.
* When offline behavior matters, preserve the existing offline-first conventions.

## Networking

* Follow existing API/client abstractions and error-handling conventions.
* Do not perform network requests directly from Composables.
* Represent network failures explicitly at the appropriate layer.
* Avoid leaking transport-layer concerns into UI models unless the existing architecture intentionally does so.
* Do not log sensitive request/response data.

## Error Handling and Logging

* Handle errors at the layer that can make the appropriate decision.
* Do not silently ignore failures.
* Do not expose internal exception messages directly to users unless that is an explicit product requirement.
* Use the project's existing logging infrastructure.
* Never log credentials, authentication tokens, personal data, or other sensitive information.
* Avoid excessive logging in normal production paths.

## Testing

Add or update tests for behavior that changes.

Prefer:

* Unit tests for business/domain logic.
* ViewModel/presentation tests for state transitions and event handling.
* Repository/data tests where data behavior is non-trivial.
* UI tests for important user-visible behavior and interactions.

Do not add tests that merely duplicate implementation details.

When fixing a bug, prefer adding a regression test when practical.

Keep tests deterministic and avoid unnecessary real network or long-running operations.

## Gradle and Build Tooling

Always use the project's Gradle wrapper:

```bash
./gradlew
```

Do not rely on a globally installed Gradle version.

Avoid `clean` unless there is a specific reason to use it. Prefer incremental builds and targeted tasks.

When running multiple Gradle tasks, prefer a single invocation so Gradle can optimize the task graph:

```bash
./gradlew ktlintCheck detekt
```

rather than invoking each task separately.

Do not change Gradle configuration, plugins, dependency versions, or build conventions unless required by the task.

## Validation

Validation should be proportional to the scope of the change.

Do not repeatedly run expensive project-wide checks after every tiny edit. Make a coherent set of changes first, then validate.

### Kotlin / application code changes

At minimum, run:

```bash
./gradlew ktlintCheck detekt
```

Then run the most relevant tests for the changed behavior. Use the `--tests` flag for targeted verification to save time:

```bash
./gradlew :feature:home:testDebugUnitTest --tests "com.mk.habittracker.feature.home.HomeViewModelTest"
```

For example, prefer targeted tests while iterating rather than rebuilding and testing the entire application unnecessarily.

### Dependency, module, or build-configuration changes

Run:

```bash
./gradlew ktlintCheck detekt buildHealth
```

If changes are isolated to one project, use `:proj:projectHealth` instead of `buildHealth`

### UI changes

Run:

```bash
./gradlew ktlintCheck detekt
```

and relevant unit/UI tests.

### Documentation-only or non-code changes

Do not run expensive Android validation solely because a documentation-only file changed.

### Before considering a task complete

* The relevant static-analysis checks pass.
* Relevant tests pass.
* New compiler/lint/static-analysis warnings are not knowingly introduced.
* Any validation that could not be run is explicitly reported.
* Do not claim a check passed unless it was actually run.

## Static Analysis and Formatting

The project uses:

* Detekt
* Ktlint

Kotlin changes should normally be validated with:

```bash
./gradlew ktlintCheck detekt
```

Do not manually reformat unrelated files simply because a formatter touched them.

When fixing formatting issues, prefer the project's existing Ktlint configuration rather than introducing local formatting exceptions.

Do not suppress Detekt rules merely to make a check pass without understanding the underlying issue.

## Build Health

`buildHealth` / `projectHealth` is a project-wide architectural/dependency-health check.

It is intentionally not required after every small source edit.

Run it when:

* Adding or removing dependencies.
* Changing module dependencies.
* Creating or removing modules.
* Changing architecture or module boundaries.
* Modifying Gradle dependency/configuration rules.
* Preparing a complete project-level validation run.

Treat failures as actionable unless there is a documented, intentional exception.

## Generated Code

Do not manually edit generated code.

Before changing a generated file, determine:

1. What generates it.
2. Whether the generator/source configuration should be changed instead.
3. Whether regeneration is required.

Do not commit generated artifacts unless the repository's existing convention requires them.

## Resources

* Follow existing resource naming conventions.
* Keep user-visible strings in resources rather than hard-coding them in Kotlin.
* Reuse existing dimensions, typography, and theme definitions where appropriate.
* Avoid introducing duplicate resources that serve the same purpose.
* Follow existing localization conventions.

## Android Lifecycle

Respect Android component lifecycles.

* Do not assume an Activity remains alive for the lifetime of a ViewModel.
* Do not store Activity or View references in long-lived objects unless the architecture explicitly requires it and the lifecycle is managed correctly.
* Use lifecycle-aware APIs for Activity-bound functionality.
* For hardware/lifecycle-bound features, start and stop resources at the appropriate lifecycle boundary.

## Security and Privacy

Treat locally stored and transmitted user data as potentially sensitive.

* Never commit secrets, API keys, tokens, signing credentials, or private certificates.
* Never hard-code credentials into source code.
* Avoid logging sensitive data.
* Use appropriate Android secure-storage mechanisms for secrets.
* Minimize retention of sensitive local data.
* Do not weaken Android security controls merely to simplify implementation.

## Git and Change Hygiene

* Keep commits and changes focused.
* Do not modify unrelated code to "clean things up" unless necessary for the task.
* Do not rewrite history or force-push unless explicitly requested.
* Do not remove existing tests merely because they are inconvenient.
* Do not update dependency versions opportunistically during unrelated work.
* Preserve user changes already present in the working tree.
* Before modifying a file, inspect its current state and avoid overwriting unrelated work.

## Agent Workflow

Before making substantial changes:

1. Inspect the relevant existing implementation.
2. Identify established patterns nearby.
3. Make the smallest coherent change that solves the task.
4. Run the appropriate validation for the scope of the change.
5. Fix failures caused by the change.
6. Re-run validation after fixes.
7. Summarize what changed and what validation was actually performed.

Do not stop at "the code looks correct." Use the repository's tests and static-analysis tooling as the final source of truth.

## Important Constraints

* Do not invent APIs, Gradle tasks, modules, or project conventions that are not present in the repository.
* When this document conflicts with an established repository convention, inspect the repository and prefer the more specific, existing convention unless the task explicitly asks for a change.
* When a command mentioned here does not exist in the current project, inspect the Gradle configuration and use the project's actual equivalent rather than blindly failing.
* Keep this file updated when the project's architecture, tooling, or required validation process changes.
