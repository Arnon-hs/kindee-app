/// <reference lib="webworker" />
import {clientsClaim} from 'workbox-core';
import {precacheAndRoute, cleanupOutdatedCaches, createHandlerBoundToURL} from 'workbox-precaching';
import {registerRoute, NavigationRoute} from 'workbox-routing';
import {NetworkFirst} from 'workbox-strategies';
import {ExpirationPlugin} from 'workbox-expiration';
import {CacheableResponsePlugin} from 'workbox-cacheable-response';
import {isPublicRecipe} from './cache-policy';
declare const self: ServiceWorkerGlobalScope & {__WB_MANIFEST: Array<{url:string;revision:string|null}>};
clientsClaim();
precacheAndRoute(self.__WB_MANIFEST);
cleanupOutdatedCaches();
self.addEventListener('message', event=>{if(event.data?.type==='SKIP_WAITING')self.skipWaiting();});
registerRoute(new NavigationRoute(createHandlerBoundToURL(new URL('index.html',self.registration.scope).pathname),{denylist:[/^\/api\//]}));
// Workbox 7.4.1 declares these initialized arrow hooks as possibly undefined.
// Expose only checked hooks; retain their original closures and constructor setup.
const cacheable=new CacheableResponsePlugin({statuses:[200]});
const expiration=new ExpirationPlugin({maxEntries:50,maxAgeSeconds:86400,purgeOnQuotaError:true});
if(!cacheable.cacheWillUpdate||!expiration.cachedResponseWillBeUsed||!expiration.cacheDidUpdate)throw new Error('Required Workbox recipe cache hooks unavailable');
registerRoute(({url,request})=>isPublicRecipe(url,request,self.location.origin),new NetworkFirst({cacheName:'kindee-public-recipes-v1',networkTimeoutSeconds:4,plugins:[{cacheWillUpdate:cacheable.cacheWillUpdate},{cachedResponseWillBeUsed:expiration.cachedResponseWillBeUsed,cacheDidUpdate:expiration.cacheDidUpdate}]}));
// Personal endpoints are deliberately unregistered: no dashboard, diary, weight or tokens in Cache Storage.
