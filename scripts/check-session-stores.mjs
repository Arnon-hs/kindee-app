import {build} from 'vite';
// This is a Node store integration check: no preview server or browser is started.
await build({configFile:false,define:{'import.meta.env.VITE_PUBLIC_DEMO':JSON.stringify('false'),'import.meta.env.VITE_LOCAL_API':JSON.stringify('false')},build:{ssr:'tests/session-store-check.ts',outDir:'node_modules/.cache/kindee-session-check',emptyOutDir:true}});
const {checkSessionStores}=await import('../node_modules/.cache/kindee-session-check/session-store-check.js');
await checkSessionStores();
