# Generated API boundary

The schema is copied from the actual private Kindee backend, not inferred from frontend casts. `contracts/source.json` pins the backend commit, canonical path, SHA-256 and public schema URL. The server publishes those exact schema bytes at `/api/v1/openapi.yaml`.

Commands:

- `npm run api:generate`: generate TypeScript definitions and Zod validators from the checked-in snapshot with Hey API 0.99.0.
- `npm run api:check`: verify the source hash and regenerate into a disposable reports directory; fail if any generated filename or byte differs.
- `npm run api:check:server`: fetch the fixed public schema URL without credentials and fail on HTTP errors, redirects or a different hash.

CI runs both checks. To update a contract, review the actual backend change, copy its canonical schema, update the verified commit/hash, regenerate and inspect the consumer changes. A temporary outage can fail the live drift check; do not bypass it by inventing a matching schema.

Generated output is excluded from lint, Knip, duplication and later mutation analysis because the backend defines its full operation set. GitHub marks it generated. It remains compiler checked, and deterministic regeneration checks its integrity.

The first migrated boundary is account login/registration. A 2xx response must pass the generated session envelope, 64-character token, expiration and user validators before entering client state. Invalid payloads produce the existing generic auth error without copying Zod input details into UI errors. Existing Fetch/Capacitor transport, private cache policy and error priority remain in place. No generated SDK replaces that transport.

Other web feature boundaries retain their existing handwritten types for now. Full generation covers their schema definitions, but generation alone does not validate their runtime responses. Further migration should happen per feature after checking required fields and nullability against the backend. Swift/Kotlin remain independent clients.

## Verification

Source-hash and generated-output drift plants failed and were removed. Malformed successful account responses were rejected. Forty-eight real account error outcomes matched the previous implementation; a deliberately wrong expected outcome was detected. Build, 32 tests and session isolation passed. Full lint moved from 15 to 11 warnings by simplifying the touched auth priority branches. Remaining warnings are the planning/health non-null assertions and ingredient-label nested ternary, not a new clean-lint claim.

Hey API's packaged MIT license and Zod's MIT license were checked. The generator is a development dependency; Zod 4.6.7 is a runtime dependency. This is not a dependency vulnerability remediation or whole-API runtime-validation claim.
