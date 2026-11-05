import {ref} from 'vue';
/** Native WebViews use the bundled assets and native updates, never a Service Worker. */
export function useRegisterSW(_options?:unknown){return {needRefresh:ref(false),offlineReady:ref(false),updateServiceWorker:async(_reload?:boolean)=>{}};}
