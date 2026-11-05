# KinDee PWA and native clients

Vue 3, TypeScript, Vite, Pinia, Router, vue-i18n, vite-plugin-pwa (`injectManifest`). The presentation site and the product PWA are separate artifacts. The presentation is not a personal dashboard.

## Run and install

1. From repository root: `cp .env.example .env`, set a persistent `APP_KEY`, then `docker compose up --build -d` and `docker compose exec api php artisan migrate --seed --force`.
2. Open http://localhost:8080 on the development machine. Localhost is a browser secure context. For a phone, deploy this gateway behind a real HTTPS origin with its Laravel API reachable on the same `/api` origin. A phone's `http://LAN-IP:8080` is not an installable secure context. Do not expose development APP_DEBUG to the Internet.
3. Local/test seeding creates `demo@kindee.local` with empty diary/weight and configurable example goals. An administrator can issue a token using `docker compose exec api php artisan kindee:token demo@kindee.local`. Output is a secret: enter it in the password field; do not publish or commit it. Only its SHA-256 hash and expiration are stored; rotation revokes the old token. Tokens expire after 30 days. This is a provisioning flow, not public self-service registration.
4. iPhone/iPad: open in Safari, Share → Add to Home Screen → Add. Launch from that icon. No `beforeinstallprompt` on iOS; the UI gives manual instructions. In-app messenger browsers may not support installation.
5. Android Chrome: use the in-app install button when the browser offers it, or ⋮ → Install app / Add to Home screen. Browser eligibility and prior dismissal control the prompt.
6. Desktop Chrome/Edge: address-bar install icon or menu. macOS Safari: File → Add to Dock where supported.
7. On launch from the icon the manifest requests `display: standalone`. The UI detects `(display-mode: standalone)`. Browser-level verification on real devices remains required.

## Offline and updates

The shell, routes, icons and offline.html are precached. Navigation falls back to the app shell; offline status is visible. Anonymous GET `/api/v1/recipes` and recipe details use NetworkFirst, at most 50 entries, 24-hour TTL; visit Recipes online once before disconnecting. Cached list includes ingredients. Filters work against that public catalog; cached details fall back to the catalog when necessary. Portion recalculation requires the API. No offline queue for writes: diary/weight saves need network and show errors on failure.

Dashboard, widget projections, food entries, weight, tokens and any request with Authorization are never registered for runtime caching. Personal endpoints return private,no-store. Token and dashboard are memory-only, cleared on logout/reload. Public shell contains no personal data. Existing in-memory dashboard may remain visible during a disconnection; the global offline banner marks the state. Failed writes never show a success message.

New service workers wait. The update prompt asks users to save their form inputs; only explicit Update sends SKIP_WAITING and reloads. First activation claims clients so subsequent public recipe requests can be cached. Do not add broad API caching rules or automatic activation that discards unsaved forms.

## Internal and native widgets

Four in-app widgets: My calories, What should I eat now, Meal Prep, Weight & progress. Aggregation endpoint `/api/v1/dashboard`; narrow projections `/api/v1/dashboard/widgets/{calories|recommendations|meal-prep|weight}`. Both enforce the same personal token and no-store policy. Dates follow the user's IANA timezone; immutable nutrition snapshots prevent historical drift.

`src/platform.ts` is the boundary for future Capacitor implementations. Browser capabilities explicitly report `systemWidgets: false`. No Capacitor package or native target is claimed as delivered. A future shell must implement OS keychain/keystore storage, deep link routing and logout cleanup before keeping credentials across launches.

WidgetKit and Android App Widgets require separate native targets and authenticated API clients. Keep widget snapshots in protected App Group/OS storage, with freshness timestamps and explicit consent. Do not bridge private widget snapshots through Service Worker caches. OS refresh scheduling differs by platform and is not guaranteed real-time. The current PWA does not create home-screen system widgets.

## Verification limits

Typecheck, production build, artifact checks and cache-policy tests can run here. Managed environment has no callable cua_repl.js tool; Sites instructions prohibit improvising a replacement browser route. Therefore browser install, standalone rendering, real offline navigation, update lifecycle, screenshots at 360/390/1440px and iOS/Android compatibility are **not yet verified**. A generated manifest is not proof of installation. PWA DoD remains open until those browser/device checks and an HTTPS Core deployment pass.

## Sites public demo release

The same client now has an explicit public-demo build (`KINDEE_PUBLIC_DEMO=true npm run build`). It lives under `/app/`, uses hash navigation and a worker scoped to `/app/`, keeping the presentation outside the worker. Installation and offline recipe reading are available for device testing on the public HTTPS origin.

The demo is not connected to a public private-data API. A reproducible public-only exporter (`node scripts/export-public-recipes.mjs` with the local Core running) captures `/api/v1/recipes?market=TH` into a versioned JSON snapshot. The exported catalog identifies illustrative seed provenance and unknown allergens. Its timestamp is displayed. Personal endpoints are never exported. Portions are calculated locally from the rounded public per-serving estimates in this mode.

`DemoSession` owns ephemeral food/weight entries in memory, begins empty and can be reset; reload/update clears it. No localStorage, IndexedDB, auth tokens or server persistence are used for personal demo inputs. UI warns about that boundary. The public build has no personal token form and must never accept production credentials. The normal build still uses Laravel API and the existing personal-token flow.

This enables a useful shareable PWA demo without pretending that PHP/PostGIS has been deployed to Sites. Production personal API deployment, full UI translations and browser/device PWA QA remain separate acceptance criteria.

## Local epic completion update

Detailed client forms, seeded recipes/ingredients, validation, installation, offline/update and privacy messages now support RU/EN/TH. The prior note about Russian-only detailed forms describes the earlier release and is superseded. Seven real Commons photographs with visible attribution are delivered as local WebP variants and included in the public asset precache. Picture pixels are not used to infer nutrients or allergens.

Settings now update actual authenticated profile goals/market/timezone using PATCH /api/v1/profile. The public demo applies those changes only to its memory session. Unsupported market pricing remains unknown; the API does not label a US recipe as Thai or reinterpret a THB amount as USD. Ingredient quantities scale with requested portions. See EPIC_STATUS.md for the final local acceptance matrix and the two outstanding browser/device checks.

## Native health extension

The shared Vue application now has Capacitor phone shells and a Wear OS client; iPhone/Apple Watch targets require macOS validation. See [health/onboarding and build instructions](HEALTH_AND_ONBOARDING.md). HealthKit/Health Connect are available only through native code, with explicit permission and selected-source imports. Native WebViews use hash routes and bundled assets with no Service Worker. The standalone PWA remains supported and does not cache health data or pretend to expose native health APIs. System WidgetKit/Android App Widgets are still not implemented.
