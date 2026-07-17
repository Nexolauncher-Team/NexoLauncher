# Implementation Plan - Fix Firebase App Check Token Error

The user is encountering `com.google.firebase.FirebaseException: No AppCheckProvider installed` when using the Smart Assistant (Gemini AI). This indicates that Firebase App Check is being invoked by the Generative AI SDK, but no provider factory has been successfully registered.

## Proposed Changes

### 1. `PojavApplication.java` [MODIFY]
- **Remove `isMainProcess` restriction for Firebase initialization:** Ensure Firebase and App Check are initialized in all processes that might use them (especially the `:launcher` process).
- **Explicitly Import `BuildConfig`:** Ensure `BuildConfig.DEBUG` is correctly resolved from the app's package.
- **Add Logging:** Add logging to verify which provider is being installed.
- **Set Token Auto-Refresh:** Explicitly set token auto-refresh to true.

### 2. `SmartAssistantViewModel.kt` [MODIFY]
- **Verify Backend Configuration:** Ensure `GenerativeModel` is correctly configured. While using `GenerativeBackend.googleAI()`, App Check might still be required if using the Firebase SDK. I will keep it as is but ensure App Check is ready.

## Verification Plan

### Manual Verification
- Deploy the app in Debug mode.
- Open the Smart Assistant and send a message.
- Verify in Logcat that "Firebase initialized" and "App Check provider installed" logs appear.
- Confirm the `No AppCheckProvider installed` error no longer occurs.

### Automated Tests
- N/A (UI and Firebase integration testing requires device/emulator environment).
