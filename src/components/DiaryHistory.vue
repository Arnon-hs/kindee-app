<script setup lang="ts">
import {ref,computed} from 'vue';
import {useI18n} from 'vue-i18n';
import {useDashboard} from '../stores/dashboard';
import {recipeLabel} from '../recipe-labels';
const props=defineProps<{weight?:boolean}>();const store=useDashboard();const {t,locale}=useI18n();const busy=ref(false);const removed=ref(false);
const data=computed(()=>store.data);
function formatDate(value:string){const date=new Date(value);return Number.isNaN(date.getTime())?'':date.toLocaleString(locale.value,{timeZone:data.value?.timezone??'UTC',month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'});}
async function remove(id:number){busy.value=true;removed.value=false;try{await store.remove(props.weight?'weight-logs':'food-entries',id);removed.value=true;}catch{}finally{busy.value=false;}}
</script>
<template><section v-if="data" class="widget diary-history"><div class="widget-title"><h2>{{t(weight?'weightHistory':'diaryTitle')}}</h2><span>{{weight?t('kg'):data.date}}</span></div><p v-if="removed" role="status" class="history-status">{{t('entryRemoved')}}</p><template v-if="weight"><p v-if="!data.weight.logs.length">{{t('weightEmpty')}}</p><ul v-else class="history-list"><li v-for="log in data.weight.logs.slice().reverse()" :key="log.id"><div><strong>{{log.kg}} {{t('kg')}}</strong><small>{{formatDate(log.recorded_at)}}</small></div><button :disabled="busy" :aria-label="t('remove')+' '+log.kg+' '+t('kg')" @click="remove(log.id)">{{t('remove')}}</button></li></ul></template><template v-else><p v-if="!data.food_entries.length">{{t('diaryEmpty')}}</p><ul v-else class="history-list"><li v-for="entry in data.food_entries" :key="entry.id"><div><strong>{{recipeLabel(entry.recipe_slug,locale,entry.title||t('recipe'))}}</strong><small>{{entry.nutrition.calories}} {{t('kcal')}} · {{entry.servings??'—'}} {{t('portions')}} · {{formatDate(entry.eaten_at)}}</small></div><button :disabled="busy" :aria-label="t('remove')+' '+recipeLabel(entry.recipe_slug,locale,entry.title||t('recipe'))" @click="remove(entry.id)">{{t('remove')}}</button></li></ul></template></section></template>
