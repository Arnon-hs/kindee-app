# Automated web checks

`npm ci` installs the Kindee pre-commit hook with simple-git-hooks 2.14.0. Installation preserves an existing custom pre-commit hook or core.hooksPath, and retains other Git hooks. It never edits global Git or Codex settings. Source ZIPs without this repository's Git root skip installation.

- `npm run precommit:check` checks staged index content in a temporary snapshot. It runs strict lint on staged web source, whole-web typechecking, generated API integrity, Knip and clone scans. It includes deletions and copies only the small Capacitor manifests that Knip actually reads. Native source remains outside these web checks.
- `npm run agent:check` runs all independent working-tree checks, then builds before running the dependent tests/session checks. It collects failures instead of reporting only the first one. No provider calls or native builds.
- CI's quality job reports the full lint backlog, rejects warnings in changed files against the actual base SHA, rejects dead code and rejects qualifying clones. Independent gates still run after another gate fails. Existing web/API/Capacitor jobs remain separate. No branch-protection setting was changed.

The staged checker rejected a staged non-null assertion even while its working-tree version had been fixed, and preserved that unstaged fix. Local pre-commit took about 2.1 seconds; a complete agent check took about 7 seconds. These are local observations; CI timing is recorded separately on its PR.

The initially considered nano-staged 1.0.2 filters changed paths to ACMR, excluding deletion-only commits. This project needs whole-web checks on deletions, so the plan uses the small index-snapshot checker directly with simple-git-hooks. Nano-staged is not retained. No check rewrites source or stages fixes automatically.

## Codex Stop hook

`.codex/hooks.json` uses the documented project-local Stop event and `scripts/codex-stop.mjs`. It runs the changed-input form of the agent checker, returns JSON only, collects failures, and stands down when `stop_hook_active` is already true. Hook payloads can contain private transcript data; this implementation neither persists nor echoes them.

Installed Codex CLI 0.142.4 reported stable hooks enabled. The protocol was manually tested: malformed source blocks with both lint/dead-code findings, a repeated Stop stands down, and clean source returns `{}`. This is not proof of automatic execution in the current desktop chat, whose working directory is the orchestration folder outside this Git checkout.

Codex requires review and trust of a new non-managed hook definition through `/hooks`. Open this repository as the project, review the concrete command and trust it there. The definition and scripts are ready; automatic trusted execution remains unverified. No trust database was edited and no bypass flag was used. Git pre-commit and CI do not depend on Codex trust. See the [official OpenAI hook documentation](https://learn.chatgpt.com/docs/hooks).

## Root configuration regression

The shared lint-input selector includes Vite, Capacitor, Knip and OpenAPI config files. New regression cases exposed a combined-string mistake that had skipped Knip/OpenAPI configuration paths. Those cases failed before the fix and passed afterward. Existing space, hash, percent and newline filename cases still pass. Config source is checked explicitly; JSON and other tooling inputs also trigger whole-project checks.

The ten existing non-null warnings in planning/health remain a report. Changed source must clear warnings. The one-month review is due 2026-12-11; it has not been performed early.
