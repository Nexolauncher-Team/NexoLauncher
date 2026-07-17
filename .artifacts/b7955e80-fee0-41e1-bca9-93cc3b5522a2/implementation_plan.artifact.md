# Implementation Plan: Final Rebranding to Sameer Yadav

This plan finalizes the branding of Nexo Launcher by setting **Sameer Yadav** as the main developer and preparing the legal documents with the new support email.

## User Review Required

> [!IMPORTANT]
> - **Main Developer**: "MovTery" will be replaced by **Sameer Yadav**.
> - **Author Button**: The button next to your name will display "**Soon**" as requested.
> - **Contact Email**: All legal documents and app references will use `contact.nexolauncher@gmail.com`.

## Proposed Changes

### NexoLauncher Module

#### [MODIFY] [strings.xml](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/res/values/strings.xml)
- Change `about_MovTery_desc` to "Main Developer of Nexo Launcher".
- Update any other strings referring to the old author.

#### [MODIFY] [AboutInfoPageFragment.kt](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/java/com/nexo/launcher/ui/fragment/about/AboutInfoPageFragment.kt)
- Replace "å¢¨åŒ—MovTery" with "**Sameer Yadav**".
- Change the button text for the author item to "**Soon**" and set the link to a placeholder.

#### [MODIFY] [UrlManager.kt](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/java/com/nexo/launcher/utils/path/UrlManager.kt)
- Update `URL_SUPPORT` to a placeholder for now.
- Update `URL_HOME` and `URL_GITHUB_HOME` to point to the `nexolauncher` GitHub organization.

#### [NEW] [privacy.html](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/assets/privacy.html) (Temporary Local Copy for Reference)
- Generate the final HTML with `contact.nexolauncher@gmail.com`.

#### [NEW] [terms.html](file:///C:/Users/Admin/NexoLauncher/NexoLauncher/src/main/assets/terms.html) (Temporary Local Copy for Reference)
- Generate the final HTML with `contact.nexolauncher@gmail.com`.

## Verification Plan

### Manual Verification
1. Open the **About** screen.
2. Verify the main developer is listed as **Sameer Yadav**.
3. Verify the button says "**Soon**".
4. Check that the Privacy Policy and Terms of Service buttons still open the correct organization links.
