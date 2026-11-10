import {existsSync} from 'node:fs';
import type {KnipConfig} from 'knip';

// The state check emits this file before importing it. A fresh checkout has no cache;
// a warm checkout does. Keep only the unresolved-path exception that is needed now.
const generatedStateCheck = new URL('./node_modules/.cache/kindee-session-check/session-store-check.js', import.meta.url);

export default {
  // Vite discovers index.html, the app, its config and the injectManifest service worker.
  entry: [
    // Manual public recipe snapshot maintenance, never executed by this analysis.
    'scripts/export-public-recipes.mjs',
    // node:test via tsx is not a recognized test-runner plugin.
    'tests/**/*.test.ts',
    // Explicit SSR entry used by scripts/check-session-stores.mjs.
    'tests/session-store-check.ts',
    // Codex's git-root-based runtime command string is not a JS import.
    'scripts/codex-stop.mjs',
  ],
  project: [
    '**/*.{js,cjs,mjs,jsx,ts,cts,mts,tsx,vue}',
    // Native sources and vendored libraries use their independent platform checks.
    '!native/**', '!android/**', '!ios/**',
    // Build output, installed dependencies and local detector reports are not source.
    '!dist/**', '!node_modules/**', '!reports/**',
  ],
  // The API generator emits all backend operations, including ones this client does not use.
  ignore: ['src/api/generated/**'],
  ignoreUnresolved: existsSync(generatedStateCheck)
    ? []
    : ['../node_modules/.cache/kindee-session-check/session-store-check.js'],
} satisfies KnipConfig;
