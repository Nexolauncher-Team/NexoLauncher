# Implementation Plan - MobileGlues "Seamless Integration"

This plan outlines the direct integration of the **MobileGlues** renderer into NexoLauncher. It includes GPU detection, automatic recommendation, and a robust installer to ensure a perfect "first launch" experience.

## Analysis & Research Results

- **License**: LGPL-2.1 allows redistribution.
- **Components Identified**:
    - `libmobileglues.so`: Main translation engine.
    - `libangle.so`: Backend for Adreno GPUs (highly recommended).
    - `config.json`: To be stored in **Internal App Storage** (`/data/user/0/.../files/MG/`) to avoid broad storage permissions.
- **Redistribution Strategy**:
    - I will implement a **Managed Integration Hub**. This system checks for bundled libs in `jniLibs` first. If missing, it uses a **Verified Downloader** to fetch the exact architecture-specific binaries from the official GitHub Release API.
    - **Update Channels**: Supports **Stable** (default) and **Beta** channels via a setting in the launcher.
- **GPU Recommendation Logic**:
    - **Adreno**: Suggest **Vulkan Zink** or **MobileGlues (with ANGLE)**.
    - **Mali**: Suggest **GL4ES** or **MobileGlues (Compute Mode)**.
    - **PowerVR/Vivante**: Suggest **GL4ES**.

## Proposed Changes

### Component: Core Renderer & Detection

#### [NEW] [GPUManager.kt](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/java/com/nexo/launcher/feature/gpu/GPUManager.kt)
- Detects GPU Vendor and Renderer string via a temporary OpenGL context.
- Stores the info in `SharedPreferences` for quick access.
- Provides `getRecommendedRenderer()` based on hardware and Minecraft version.

#### [NEW] [MobileGluesRenderer.kt](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/java/com/nexo/launcher/renderer/renderers/MobileGluesRenderer.kt)
- Implements `RendererInterface`.
- Handles environment variables like `POJAVEXEC_EGL` pointing to the internal library path.
- Injects `MESA_GL_VERSION_OVERRIDE` (4.6) for maximum mod compatibility.

---

### Component: Installer & Integrity

#### [NEW] [MobileGluesHub.kt](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/java/com/nexo/launcher/feature/renderer/MobileGluesHub.kt)
- **Architecture Detection**: Uses `Architecture.java` to determine which `.so` to download.
- **Integrity Verification**: Checks SHA-256 hashes of downloaded files.
- **Configuration Automation**: Automatically creates optimized `config.json` in internal storage.
- **Update Logic**: Fetches from "Stable" or "Beta" GitHub release tags based on user settings.
- **Rollback Mechanism**: Reverts to stable renderer if initialization fails.

#### [NEW] [MobileGluesUpdateManager.kt](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/java/com/nexo/launcher/feature/renderer/MobileGluesUpdateManager.kt)
- Periodically checks for new releases on GitHub based on the selected channel (Stable/Beta).
- Handles notification logic for the user.

#### [NEW] [MobileGluesSetupDialog.kt](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/java/com/nexo/launcher/ui/dialog/MobileGluesSetupDialog.kt)
- Displays a themed progress bar, architecture info, and error messages during setup.
- Provides a summary of what's being installed for transparency.

---

### Component: UI & Integration

#### [MODIFY] [MainActivity.java](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/java/com/nexo/launcher/MainActivity.java)
- Trigger GPU detection on the first run.
- Prompt the user with a "High Performance Rendering" setup if MobileGlues is recommended but missing.

#### [MODIFY] [SmartAssistantViewModel.kt](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/java/com/nexo/launcher/ui/viewmodel/SmartAssistantViewModel.kt)
- Teach the AI about the "Managed Integration Hub".
- Enable the AI to trigger `MobileGluesHub.install()` directly if a user says "make it faster" or "fix lag".

## Verification Plan

### Automated Tests
- Test logic for `GPUManager` to ensure vendors are parsed correctly (case-insensitivity).
- Verify `MobileGluesHub` URL formatting for all 4 architectures.

### Manual Verification
- **Adreno Device**: Verify ANGLE backend is enabled in `config.json`.
- **Mali Device**: Verify Compute Mode is prioritized.
- **Integrity**: Force a corrupt download to verify the "Rollback" and "Re-download" logic.

## Open Questions

> [!IMPORTANT]
> 1. Should we use `/sdcard/MG/` for the config, or use internal storage to avoid requesting "Manage All Files" permission? (Modern Android makes `/sdcard/` access harder).
> 2. Do you want to include a "Beta" or "Stable" channel for MobileGlues updates?
