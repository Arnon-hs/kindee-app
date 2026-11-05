<script setup lang="ts">
import {ref,onMounted,onUnmounted} from 'vue';
import {useI18n} from 'vue-i18n';
import {useRegisterSW} from 'virtual:pwa-register/vue';
const {t}=useI18n();
interface InstallEvent extends Event {prompt():Promise<void>;userChoice:Promise<{outcome:string}>}
const registrationError=ref(false);const install=ref<InstallEvent|null>(null);const installed=ref(false);const showHelp=ref(false);
const {needRefresh,offlineReady,updateServiceWorker}=useRegisterSW({onRegisterError(){registrationError.value=true;}});
function capture(e:Event){e.preventDefault();install.value=e as InstallEvent;}
function complete(){installed.value=true;install.value=null;}
async function promptInstall(){if(install.value){try{await install.value.prompt();await install.value.userChoice;}finally{install.value=null;}}else showHelp.value=!showHelp.value;}
onMounted(()=>{installed.value=window.matchMedia('(display-mode: standalone)').matches||(navigator as Navigator&{standalone?:boolean}).standalone===true;window.addEventListener('beforeinstallprompt',capture);window.addEventListener('appinstalled',complete);});
onUnmounted(()=>{window.removeEventListener('beforeinstallprompt',capture);window.removeEventListener('appinstalled',complete);});
</script>
<template><div class="pwa-controls"><p v-if="registrationError" role="alert">{{t('swError')}}</p><button v-if="!installed" @click="promptInstall">＋ {{t('install')}}</button><span v-else>✓ {{t('standalone')}}</span><div v-if="showHelp" class="install-help"><strong>{{t('installTitle')}}</strong><p>{{t('iosInstall')}}</p><p>{{t('androidInstall')}}</p><p>{{t('desktopInstall')}}</p><p>{{t('installHelp')}}</p></div><div v-if="needRefresh" class="update" role="status">{{t('updateAvailable')}}<button @click="updateServiceWorker(true)">{{t('update')}}</button><button @click="needRefresh=false">{{t('later')}}</button></div><div v-if="offlineReady" class="update" role="status">{{t('offlineReady')}}<button @click="offlineReady=false">OK</button></div></div></template>
