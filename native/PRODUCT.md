# Kindee

<!-- impeccable:product-schema 1 -->

## Platform
adaptive

## Users
People choosing what to eat nearby and tracking their food. The test location is Homa Phuket Town; the general product supports an address or device location.

## Product Purpose
Build a mixed meal plan from cooking, cafes and delivery, then track what was eaten using a reviewed AI estimate and optional watch activity data. The owner confirmed this combined workflow on 10 November 2026.

## Stack
Existing Fud AI native Android Kotlin/Compose and iPhone SwiftUI code, connected to the existing Kindee Laravel backend. The owner chose working native screens before image comps.

## Capabilities and Constraints
Keep food recognition, manual review, food diary, Health Connect, HealthKit and Apple Watch foundations. Add Kindee account access, nearby menus, address/location selection and mixed meal plans. Do not call the original author's hosted AI, billing, challenge or backup services as our backend. Provider API keys and account tokens must stay out of source and build artifacts; signed-in account tokens may be stored using native encrypted storage. The owner authorized a separate Kindee service on the existing OVH/Coolify server, reusing the existing OpenAI key and using a technical HTTPS hostname for this test. This does not authorize changes to AtlasRepo services or a public app-store release. Android food AI and Coach use the dedicated server; iPhone food AI source targets it, while iPhone Coach parity is still pending. Real menu observations are a bounded private pilot, not a live delivery feed. Abot failed the Homa acceptance test; JINGHAI access is absent. Real iPhone/watch acceptance is pending hardware and Xcode.

## Brand Commitments
Product name Kindee. Warm, plain language. Respect native Android and iPhone controls. Preserve upstream license and attribution; no use of the author's commercial identity as our own.

## Product Principles
- A plan connects nearby food to everyday decisions.
- An AI result is a reviewable estimate, never silently saved as measured nutrition.
- Menu price, source and freshness are visible.
- Health sharing is optional and specific.

## Current interface refinement
The owner requested light colors and less text. Android defaults to light once, while preserving subsequent appearance choices. The Plan shows a selected day instead of the entire week, uses short controls and discloses menu sources and timestamps under Menu details. Secondary sharing and connection actions live in the account sheet.
