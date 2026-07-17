# Fix Firebase Build and Runtime Issues (ANR & Tool Errors)

The project has multiple issues:
1. **Build Failure**: Firebase BoM v34.16.0 removed `-ktx` artifacts and introduced API changes.
2. **Kotlin Incompatibility**: Newer Firebase libraries require Kotlin 2.1.0 metadata.
3. **ANR (App Not Responding)**: Heavy I/O in tool handlers is blocking the Main thread.
4. **Gemini Runtime Error**: "Missing thought_signature" occurs when Gemini 1.5 reasoning parts are not handled correctly by the SDK/history.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/build.gradle.kts)
- [x] Replace legacy Firebase `-ktx` dependencies with main modules.
- [MODIFY] Upgrade Kotlin version to `2.1.0`.

### Smart Assistant Logic

#### [MODIFY] [SmartAssistantViewModel.kt](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/java/com/nexo/launcher/ui/viewmodel/SmartAssistantViewModel.kt)
- [MODIFY] Add `parameters = emptyMap()` to `FunctionDeclaration` constructors for tools with no parameters.
- [MODIFY] Move heavy operations (file reading, cache cleaning) to `Dispatchers.IO` using `withContext`.
- [MODIFY] Add robustness to tool dispatching by stripping potential "default_api:" or "google_ai:" prefixes from model-generated function names.

#### [DELETE] [scratch_check.kt](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/java/com/nexo/launcher/scratch_check.kt)
- [DELETE] Remove obsolete scratch file causing build errors.

## Verification Plan

### Automated Tests
- Run `./gradlew :NexoLauncher:assembleDebug` to verify the fix for build errors.
- Manual verification of assistant responsiveness (no ANR during cache/log operations).

## Verification Plan

### Automated Tests
- Run `./gradlew :NexoLauncher:assembleDebug` to verify that the project now builds successfully.
