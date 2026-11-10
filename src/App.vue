<script setup lang="ts">
import {isNative} from './api/transport';
import {ref,onMounted,onUnmounted} from 'vue';
import {useI18n} from 'vue-i18n';
import PwaControls from './components/PwaControls.vue';
import PhotoCredits from './components/PhotoCredits.vue';
const navigation=[
 {name:'home',path:'/',icon:'◉'},
 {name:'recipes',path:'/recipes',icon:'◷'},
 {name:'prep',path:'/meal-prep',icon:'▤'},
 {name:'progress',path:'/progress',icon:'↗'},
 {name:'settings',path:'/settings',icon:'⚙'},
];
const isPublicDemo=import.meta.env.VITE_PUBLIC_DEMO==='true';
const {t,locale}=useI18n();function setLanguage(){document.documentElement.lang=locale.value;}const online=ref(navigator.onLine);function network(){online.value=navigator.onLine;}
onMounted(()=>{window.addEventListener('online',network);window.addEventListener('offline',network);});onUnmounted(()=>{window.removeEventListener('online',network);window.removeEventListener('offline',network);});
</script>
<template><a class="skip-link" href="#main">{{t('navigation')}}</a><header class="topbar"><RouterLink to="/" class="wordmark"><span>k✳</span> KinDee</RouterLink><label><span class="sr-only">{{t('language')}}</span><select v-model="locale" @change="setLanguage"><option>ru</option><option>en</option><option>th</option></select></label><RouterLink to="/settings" :aria-label="t('settings')">⚙</RouterLink></header><div v-if="!online" class="offline" role="status">{{t(isPublicDemo?'offlineBannerDemo':'offlineBannerCore')}}</div><main id="main" tabindex="-1" class="app-main"><RouterView/></main><nav class="bottom-nav" :aria-label="t('navigation')"><RouterLink v-for="item in navigation" :key="item.name" :to="item.path"><span>{{item.icon}}</span>{{t(item.name)}}</RouterLink></nav><footer class="app-footer"><PwaControls v-if="!isNative"/><PhotoCredits/><p>{{t('footer')}}</p></footer></template>
