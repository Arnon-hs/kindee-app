---
name: Kindee
description: Native pocket food guide with a reviewed food diary
colors:
  forest: "#183E32"
  pale-sage: "#BAD5AD"
  sage: "#D9E8C9"
  deep-sage: "#304B38"
  paper: "#FAF8F1"
  night-paper: "#111C17"
  surface: "#FFFFFF"
  night-surface: "#1D2B22"
  night-ink: "#E5EDE3"
  secondary-ink: "#526257"
  night-secondary-ink: "#B5C5B6"
  divider: "#CDD6CA"
  night-divider: "#3F5146"
  error: "#AB3720"
  night-error: "#FFB4A3"
typography:
  display-large:
    fontFamily: "system-ui"
    fontSize: "40sp"
    fontWeight: 700
    lineHeight: "48sp"
  display-medium:
    fontFamily: "system-ui"
    fontSize: "34sp"
    fontWeight: 700
    lineHeight: "40sp"
  display-small:
    fontFamily: "system-ui"
    fontSize: "28sp"
    fontWeight: 700
    lineHeight: "34sp"
  headline-large:
    fontFamily: "system-ui"
    fontSize: "24sp"
    fontWeight: 600
    lineHeight: "30sp"
  headline-medium:
    fontFamily: "system-ui"
    fontSize: "22sp"
    fontWeight: 600
    lineHeight: "28sp"
  headline-small:
    fontFamily: "system-ui"
    fontSize: "20sp"
    fontWeight: 600
    lineHeight: "26sp"
  title-large:
    fontFamily: "system-ui"
    fontSize: "18sp"
    fontWeight: 600
    lineHeight: "24sp"
  title-medium:
    fontFamily: "system-ui"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "22sp"
  title-small:
    fontFamily: "system-ui"
    fontSize: "14sp"
    fontWeight: 500
    lineHeight: "20sp"
  body-large:
    fontFamily: "system-ui"
    fontSize: "16sp"
    fontWeight: 500
    lineHeight: "24sp"
  body-medium:
    fontFamily: "system-ui"
    fontSize: "14sp"
    fontWeight: 500
    lineHeight: "20sp"
  body-small:
    fontFamily: "system-ui"
    fontSize: "12sp"
    fontWeight: 500
    lineHeight: "18sp"
  label-large:
    fontFamily: "system-ui"
    fontSize: "14sp"
    fontWeight: 600
    lineHeight: "20sp"
  label-medium:
    fontFamily: "system-ui"
    fontSize: "12sp"
    fontWeight: 600
    lineHeight: "16sp"
  label-small:
    fontFamily: "system-ui"
    fontSize: "11sp"
    fontWeight: 500
    lineHeight: "14sp"
rounded:
  diary-group: "22dp"
  diary-thumbnail: "14dp"
  review-sheet-top: "28dp"
spacing:
  "4": "4dp"
  "6": "6dp"
  "8": "8dp"
  "12": "12dp"
  "14": "14dp"
  "16": "16dp"
  "20": "20dp"
  "24": "24dp"
components:
  button-primary:
    backgroundColor: "{colors.forest}"
    textColor: "{colors.night-ink}"
    typography: "{typography.label-large}"
  button-primary-dark:
    backgroundColor: "{colors.pale-sage}"
    textColor: "{colors.night-paper}"
    typography: "{typography.label-large}"
  button-text:
    textColor: "{colors.forest}"
    typography: "{typography.label-large}"
  input-outlined:
    textColor: "{colors.forest}"
    typography: "{typography.body-large}"
  navigation-selected:
    backgroundColor: "{colors.sage}"
    textColor: "{colors.forest}"
  diet-chip-selected:
    backgroundColor: "{colors.sage}"
    textColor: "{colors.forest}"
  meal-offer:
    textColor: "{colors.forest}"
    typography: "{typography.title-large}"
  diary-summary:
    textColor: "{colors.forest}"
    typography: "{typography.headline-large}"
  diary-group:
    rounded: "{rounded.diary-group}"
    padding: "12dp 14dp"
  diary-thumbnail:
    rounded: "{rounded.diary-thumbnail}"
    size: "76dp"
---

# Design System: Kindee

## Overview

**Creative North Star: "Pocket food field guide"**

Kindee uses warm paper, forest ink and quiet sage selection to make everyday food decisions easy to scan. Native system type and familiar controls keep the guide practical. The owner selected candidate 3, Pocket food field guide, code-first, with FORM seed `ed399248`; this document captures the implemented replacement world.

This is a native visual specification for the default light Kindee theme. Android Material 3 tokens and the compact phone compositions are the current rendered reference. The light-default, concise selected-day Plan refinement preserves the same world. iPhone uses SwiftUI and HIG conventions; its palette, light default and cloud API changes are source-only. Existing deep settings and workout compositions remain inherited. This document does not certify a redesign of every upstream screen.

**Key Characteristics:**
- Warm paper and forest ink, with sage indicating selection.
- System typography that respects platform text scaling.
- Flat selected-day meal lists and a compact diary summary with accessible provenance.
- Native controls, deliberate disclosures and explicit confirmation states.

The normative tokens above are extracted from Android `ui/theme/Color.kt`, `Theme.kt` and `Type.kt`. Typography values use native `sp`; spacing and shape values use `dp`. `system-ui` is the portable label for Compose `FontFamily.Default`, not an added font dependency. The SwiftUI sources use system text styles rather than this numeric Android scale. Frontmatter component entries describe observed roles and scoped dimensions, not new override APIs.

Visual review disposition: **ship for the light-default, concise Plan refinement on compact Android portrait Seeker only**, with no material finish fixes. Fresh references: [light Plan](.impeccable/review/cloud/phone-android.png), [large type Plan](.impeccable/review/cloud/phone-android-large-type.png), [Diary](.impeccable/review/cloud/phone-android-diary.png). The earlier dark Plan capture remains historical evidence, not a fresh dark-mode review of this refinement. No iPhone, tablet, landscape or watch visual acceptance is claimed. iOS validation was Swift syntax parsing only; the iPhone app was not built or tested. Native system controls and typography intentionally follow Material and HIG; no web detector ran. This is a private pilot, with no public app-store release authorized.

## Colors

The default palette uses a warm neutral ground, restrained green actions and readable semantic text pairs. Android starts in light appearance, including a one-time migration of the stored appearance setting; subsequent user choices of light, dark or system remain available. Status and navigation bar icon contrast follows the actual app appearance rather than the device setting alone. iPhone source also defaults to light, without device acceptance.

### Primary

- **Forest** is the default light primary and main ink. The compatibility key remains `fudPink`; the rendered default is forest. Android primary, secondary and tertiary share this action hue.
- **Pale sage** is the default dark primary. Its action foreground is night paper. Light filled actions use night ink on forest.

### Secondary

- **Sage** is the light selection container for navigation, segmented choice and native selected chips, paired with forest.
- **Deep sage** is the dark selection container, paired with night ink. Secondary is a semantic selection role here, not an additional marketing accent.

### Neutral

- **Paper / night paper** are the app backgrounds.
- **Surface / night surface** are the native container and navigation surfaces.
- **Night ink** is the dark main text and light filled-action foreground.
- **Secondary ink / night secondary ink** carry descriptions, portion details, timestamps, source labels and unselected navigation. These are the strengthened `onSurfaceVariant` values.
- **Divider / night divider** provide outline and outline-variant roles.

Android semantic **error / night error** are distinct from the directional clay swatch in the brief. The frontmatter records the implemented semantic-role choices; the unused directional clay is not a runtime token. This records source intent, not a claim that every error state received a visual contrast audit. Other inherited user-selectable theme colors remain in source and are outside the default-theme extraction.

**The Semantic Pair Rule.** Use the foreground paired with the active theme role. Secondary meal and diary text uses onSurfaceVariant, not a new opacity reduction.

Source-only iPhone parity: `Views/Theme.swift` supplies dynamic forest and pale sage. `appBackground.colorset` and `appCard.colorset` match the Android background and surface colors to their stored sRGB precision. `AccentColor.colorset` is empty; the app applies tint from `AppThemeColor`. SwiftUI secondary text is system-derived and errors use system red, so it does not implement the Android secondary/error values exactly. The inherited Graphite option also differs between the platforms. Do not claim complete theme parity.

## Typography

**Display, body and labels:** native system font. Android weights and sizes are the explicit `Type.kt` scale in the frontmatter. iPhone uses SwiftUI roles such as title, headline, subheadline, footnote and caption.

The character is compact and plain. Semibold headings identify meals and sections; medium body text carries practical details. Display roles remain available in Android source, while the Plan lead uses headline-large and every MealOffer title uses title-large. The first meal has no separate prominence role. Supporting meal detail uses body-medium; disclosed source and checked-at text use body-small. Actions use native Material labels. The diary summary uses headline-large and title-large. Inherited diary rows also contain local sizes, including a 16sp semibold meal name and 11sp macro pills; they do not redefine the shared type scale.

**The Native Type Rule.** Use the native text role and platform scaling. Do not substitute a decorative web font or fix the rendered screenshot size.

## Layout

Android Plan uses one scrolling column with system top-bar insets, 20dp side gutters, 16dp gaps between list items and 140dp bottom content padding. Meal details use a 6dp internal gap. Address/account sheets use scrolling content with keyboard insets, 20dp padding and 12dp gaps. Food preference choices wrap in a native FlowRow with 8dp horizontal gaps. These are scoped observations, not a universal page grid.

The selected compact Android Plan composition puts the address and native My plan / Nearby menus switch first, then a horizontally scrolling date strip. The initial selection is today when present, otherwise the first saved date. It shows the selected day's three meals rather than a 21-row weekly stream. Preferences, Plan options and the primary Build my week / Rebuild week action sit before the meals. The unknown-nutrition option is disclosed inside Plan options. The address button shows the first comma-separated part on one ellipsized line; full search-result addresses remain available in the address sheet. Draft state is labeled Preview and saving requires a native confirmation; it does not record food as eaten. Secondary diary/activity sharing and connection controls live in the account sheet, with no duplicate Plan footer.

Diary uses the date strip, a compact calorie line with horizontal native progress and an aligned macro summary. Its calorie summary has 24dp horizontal padding and 12dp vertical gaps. Food groups retain inherited rows with 14dp horizontal and 12dp vertical padding and a 12dp thumbnail-to-text gap. The native bottom bar keeps Plan, Diary, Progress, Coach and Settings; configured hidden tabs still apply. Navigation scroll constants are 112dp and 88dp for docked controls. Do not hard-code screenshot pixels as layout dimensions.

No app breakpoint tokens were established for this work. Large type was captured on the compact Android phone; content continues in the native scroller. Larger-device composition remains outside the visual acceptance scope. iPhone source uses NavigationStack, List, Form and TabView, not an Android pixel port.

## Elevation & Depth

The new Plan is flat: paper ground, native separators and tonal selection. Android navigation explicitly sets tonal elevation to zero. Native sheets and dialogs retain Material depth and interaction behavior. There is no new shared shadow scale.

Inherited diary FoodRow/WaterLogRow and some review containers still use translucent surface fills, fine gradient borders and grouped corners. They remain local implementation details; this document does not pretend they were removed. The default forest gradient has equal endpoints, so it is visually solid. iPhone depth follows its native containers.

**The Quiet Surface Rule.** New Plan rows use whitespace and native dividers. Retain inherited component treatments within their existing scope until a separate change replaces them.

## Shapes

Use native control shapes for buttons, segmented choices, chips and fields. The project does not override MaterialTheme shapes globally; do not introduce a universal custom radius. The explicit rounded tokens are scoped to inherited diary groups, diary thumbnails and the food review sheet's top corners. Intermediate grouped diary rows can be square while the first and last rows own the outer corners. Macro pills use CircleShape. New Plan meals remain unboxed rows with dividers.

## Components

### Buttons

Android Button is the filled primary action; TextButton carries secondary actions and OutlinedButton carries the address. Colors come from the active Material scheme. Shape, touch target, focus, pressed and disabled treatment are native defaults, not app-authored web states. Busy requests disable relevant actions. Keep the existing semantic label and icon descriptions. iPhone uses native Button and Link.

### Chips

Food preferences use Material FilterChip for diet selection and Checkbox for mutually exclusive ingredient Like / Avoid choices. Selected color follows the selection container. Preference chips wrap; the Plan date FilterChips instead occupy a horizontal LazyRow with 8dp gaps and native selected semantics. Diary macro pills are read-only summaries, not filters.

### Cards / Containers

Use the scoped grouped diary treatment for existing diary entries, with thumbnail and text aligned at the top. Thumbnail size is 76dp. Plan MealOffer is a plain Column with a native divider, not a reusable card grid. Review containers remain inherited. Do not apply a card shell to every list item.

### Inputs / Fields

Account, address and food preferences use native outlined fields on Android and Form fields on iPhone. Keyboard type matches email, password, address or numeric input. Android sheets account for the keyboard with imePadding and scroll. Validation messages use the semantic error role on Android. Do not replace native focus or selection behavior with bespoke decoration.

### Navigation

Android NavigationBar uses the surface token and no tonal elevation. The selected icon has the selected container foreground, the indicator uses the selection container, the selected label uses primary, and unselected content uses onSurfaceVariant. Native icons remain paired with visible labels. iPhone TabView uses system tab behavior and app tint.

### Meal observation and plan preview

Keep the selected date, slot, meal name, restaurant, fulfillment, price and nutrition state easy to scan. Restaurant rows retain an external provider action alongside Menu details. That disclosure holds the readable provider/source, checked-at time and reminder to confirm ingredients, address and final price before ordering; these details are retained without repeating expanded paragraphs in every row. Android maps provider identifiers to Restaurant website, Grab, LINE MAN or Menu observation and formats checked-at time in the device zone. The private pilot contains six dated menu observations and has no automatic live supplier. Unknown nutrition remains explicit.

The Android Plan options and MealOffer details regions use the authored `animateContentSize()` disclosure motion. They use native defaults and normal system animation scaling; no custom duration or easing token was established. Avoid looping decorative motion. Native confirmation separates preview from replacing generated meals.

Source-only iPhone differences remain: its Plan retains the earlier first-meal and remaining-week composition; meal rows show readable provider names but currently omit the checked-at line, and the unknown-nutrition toggle is directly visible rather than disclosed through Android's Plan options. Its food-AI connection disclosure says Coach stays local. Treat these as unresolved parity details, not accepted iPhone behavior.

### Diary and AI review

Use the horizontal calorie progress line and plain macro columns above grouped entries. Time, portion, goal text and macro text use the strengthened secondary role. Non-manual, non-barcode Android entries retain the visible AI estimate label. The review sheet begins with the instruction to check food, portion and nutrition before logging. Keep provenance close to the value; an estimate is not measured nutrition.

The Android account sheet explicitly discloses that submitted food photos and descriptions are sent to OpenAI, and that Coach also sends the message, profile and nutrition summary. Keep this specific disclosure next to Connect Kindee AI and retain explicit diary/activity sharing confirmations. This records the visible consent pattern, not infrastructure rules.

The sidecar's synthesized tonal ramps are panel metadata, not additional native palette tokens. Its self-contained HTML/CSS snippets are illustrative translations for the design panel. Their CSS pixels represent nominal native dimensions and their web focus/hover states enable the panel only. The Kotlin and Swift sources remain the native behavioral authority; these snippets are not native runtime acceptance.

## Do's and Don'ts

### Do:
- Do use the default theme role pairs and native scalable typography.
- Do keep price and nutrition uncertainty visible, with provider, observation time and ordering checks accessible through the meal's Menu details disclosure.
- Do retain visible AI-estimate provenance in review and diary contexts.
- Do use native scrolling, keyboard insets, focus and confirmation controls.
- Do treat compact Android captures as the accepted visual scope and iPhone source as a separate implementation.

### Don't:
- Don't turn private dated menu observations into a live-supplier claim.
- Don't use fabricated food imagery or present estimated nutrition as measured.
- Don't hide source labels through reduced opacity or replace them with provider identifiers.
- Don't infer tablet, iPhone or watch visual acceptance from the Android captures.
- Don't copy inherited glass treatments into new Plan rows or claim all upstream screens were redesigned.
