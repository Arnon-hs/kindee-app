# KinDee local MVP — accounts and weekly planning

Current scope: a runnable local Laravel/PostGIS product and installable web/native clients. The public Site remains an explicitly labelled memory-only demo because no persistent production API server has been supplied.

## Working product loop

1. Open recipes anonymously, choose a recipe and portions.
2. Create your own account with name, email, password and market; no administrator-issued token is required. Email delivery/verification and password reset are not implemented or claimed.
3. Complete goal/body-state onboarding; connect a supported native health source with consent, or explicitly choose later/unsupported. No watch readings are fabricated.
4. Add meals to the diary and record weight. Totals and profile goals persist in PostgreSQL.
5. Save a recipe on a calendar date with 1–20 portions. Plans store immutable nutrition and ingredient snapshots and do not change the food diary.
6. Open the rolling seven-day planner and its shopping list. Ingredient quantities aggregate by ingredient and preparation state; raw and cooked weights remain separate. Remove a plan and the list recalculates.
7. Sign in from another client: the same account records are available. Logout revokes only the presented session. Tokens are hash-only server-side, expire after 30 days and remain memory-only in the client.

## Run on your Mac

Clone the private platform and public app as sibling directories, named `kindee-platform` and `kindee-app`. From the platform directory run `python3 scripts/start-local.py` with Docker Desktop running. It creates an ignored local .env with a stable random app key if needed, builds/starts the stack, migrates the database and seeds the illustrative recipe catalog only when empty. Open http://localhost:8080 and create an account.

The default PostGIS image explicitly uses amd64 for compatibility, including Docker Desktop emulation on Apple Silicon. An optional ARM64 image recipe is supplied in compose.native.yaml / infra/postgis, but its build could not be completed in this 32 GB cloud filesystem and no M4 runtime/performance pass is claimed. `--native-postgis` is experimental and opt-in. Cloud-specific CA secrets are optional outside the managed proxy environment; TLS verification remains enabled.

HTTP on localhost is a secure-context exception for PWA development. Another phone opening a Mac LAN HTTP address does not gain install/offline support automatically; use trusted HTTPS for personal PWA testing. Android debug clients can use the explicit local API URL. Apple device builds require macOS/Xcode and provisioning.

## Fresh checks

- Laravel/PostGIS: 25 tests, 252 assertions passed.
- Pint PSR-12: 73 PHP files passed.
- Client: 19 tests, TypeScript and normal/public-demo builds. Covers plan scaling, isolation, immutable snapshots, invalid dates, market validation and private cache exclusion.
- Node store integration checks passed for stale requests, session expiry and captured-token logout.
- OpenAPI 3.1 validated after adding accounts and planning contracts.
- `python scripts/check-mvp-accounts.py` passed against the actual local gateway/API/PostgreSQL: registration, two independent sessions, onboarding, saved plans/shopping, diary/weight/profile, removal and logout. All personal responses checked for private/no-store. Only its generated disposable account was deleted.
- Provider probe: two contract/validation tests; public schema inspected, no paid run or live dataset acquisition.

Evidence: docs/evidence/mvp-accounts-http.txt and grab-abot-schema-probe.json. Public recipe nutrition/THB costs remain illustrative seed estimates, not a verified food-composition or commercial menu dataset.

## Still unverified or externally blocked

Browser/device screenshots, physical install/standalone/offline/update acceptance, native watch synchronization and Apple binaries are unverified. GrabFood TH supplier data is not connected. See the private platform supplier validation document for the concrete candidate, failed sources, bounded probe and missing trial credentials. The legacy CLI provisioning/token path is retained for existing local profiles.
