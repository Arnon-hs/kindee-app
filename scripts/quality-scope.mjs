const rootInputs=new Set(['package.json','package-lock.json','tsconfig.json','vite.config.ts','capacitor.config.ts','knip.config.ts','openapi-ts.config.ts','.oxlintrc.json','.jscpd.json','.jscpd.markup.json','.gitattributes']);
const lintConfigs=new Set(['vite.config.ts','capacitor.config.ts','knip.config.ts','openapi-ts.config.ts']);

export function isLintInput(path) {
  return lintConfigs.has(path)||(/^(src|tests|scripts)\//.test(path)&&/\.(?:[cm]?[jt]sx?|vue)$/.test(path));
}

export function isQualityInput(path) {
  return rootInputs.has(path)||/^(src|tests|scripts|contracts|public|\.github\/workflows|\.codex)\//.test(path);
}
