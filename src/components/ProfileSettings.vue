<script setup lang="ts">
import {reactive,ref,watch,computed} from 'vue';
import {useI18n} from 'vue-i18n';
import {useDashboard,isPublicDemo} from '../stores/dashboard';
import {defaultProfile,type ProfileInput} from '../profile';
const {t}=useI18n();const store=useDashboard();const form=reactive<ProfileInput>({...defaultProfile});const saving=ref(false);const saved=ref(false);
const zones=computed(()=>[...new Set(['Asia/Bangkok','America/New_York','America/Los_Angeles','Europe/London',form.timezone])]);
watch(()=>store.data,data=>{if(data)Object.assign(form,{market:data.market.country_code,timezone:data.timezone,...data.nutrition.goals});},{immediate:true});
async function save(){saving.value=true;saved.value=false;try{await store.save('profile',{...form});saved.value=true;}catch{}finally{saving.value=false;}}
</script>
<template><section class="widget settings"><h2>{{t('profileTitle')}}</h2><p>{{t('profileHelp')}}</p><form class="profile-form" @submit.prevent="save"><label>{{t('market')}}<select v-model="form.market"><option value="TH">Thailand · THB</option><option value="US">USA · USD</option></select></label><label>{{t('timezone')}}<select v-model="form.timezone"><option v-for="zone in zones">{{zone}}</option></select></label><label v-for="(key,i) in (['calories','protein_g','fat_g','carbs_g'] as const)">{{t(['goalCalories','goalProtein','goalFat','goalCarbs'][i])}}<input v-model.number="form[key]" type="number" :min="key==='calories'?1:0" :max="key==='calories'?10000:1000" step="1" required></label><button :disabled="saving">{{t(saving?'loading':'save')}}</button></form><p v-if="saved" role="status">{{t(isPublicDemo?'demoProfileSaved':'profileSaved')}}</p><p>{{t(isPublicDemo?'demoPrivacy':'privatePrivacy')}}</p><button @click="isPublicDemo?store.logout():store.signOut()">{{t(isPublicDemo?'clearDemo':'logout')}}</button></section></template>
