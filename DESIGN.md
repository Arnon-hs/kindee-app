# Kindee design system entrypoint

This release preserves the incumbent native paper/forest identity and the separate retained Vue client. Read native/DESIGN.md for native token vocabulary and source mappings; historical acceptance statements there predate the current runtime report in docs/IOS_CLOUD_TEST.md.

## Sources and composition

- iOS: native/ios/calorietracker/Views/Theme.swift and semantic AppColors, native TabView/NavigationStack/Form/List/Button/Menu, scalable system type.
- Android: native/android/app/src/main/java/com/apoorvdarshan/calorietracker/ui/theme and existing Compose controls.
- Retained PWA: src/style.css, src/components and src/views. Its CSS is not a new native token contract.
- No managed design-system/components, layouts or patterns registry has been introduced. Existing views remain source-inspected unmanaged fallbacks, not certified reusable contracts.

## Applied boundary

The server-connection error uses the existing native account Form and Text error style. It remains visible for both disconnected and connected states. Session persistence, date matching, API requests and calorie calculation remain engineering-owned logic; design-system does not authorize changing them by styling.

The calorie diary displays consumed food; a saved plan displays planned food. AI labels disclose estimates, and menu source/check time/price information remains accessible. Never make a planned meal look logged as eaten. iPhone screenshots are captured from Simulator; promotional artwork is labelled separately.

## Verification

Use the platform build commands in native/README.md and the concrete observations in docs/IOS_CLOUD_TEST.md. The account error placement was inspected against the incumbent Form composition; the successful connection, menus, mixed plan, photo review and target action were checked on an iPhone simulator. No new visual world, typography family, component library or shared token change is part of this release.
