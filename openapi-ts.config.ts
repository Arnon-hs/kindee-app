import {defineConfig} from '@hey-api/openapi-ts';

export default defineConfig({
  input: './contracts/openapi.yaml',
  output: 'src/api/generated',
  // Keep the existing Fetch/Capacitor transport and its session/cache behavior.
  // Migrate validators at individual feature boundaries, not through a new SDK.
  plugins: ['@hey-api/typescript', {name: 'zod', dates: {offset: true}}],
});
