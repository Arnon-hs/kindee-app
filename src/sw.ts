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
registerRoute(({url,request})=>isPublicRecipe(url,request,self.location.origin),new NetworkFirst({cacheName:'kindee-public-recipes-v1',networkTimeoutSeconds:4,plugins:[new CacheableResponsePlugin({statuses:[200]}),new ExpirationPlugin({maxEntries:50,maxAgeSeconds:86400,purgeOnQuotaError:true})]}));
// Personal endpoints are deliberately unregistered: no dashboard, diary, weight or tokens in Cache Storage.
