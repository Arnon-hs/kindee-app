<script setup lang="ts">
import {computed,ref,watch} from 'vue';
import {useRoute} from 'vue-router';
import {useI18n} from 'vue-i18n';
import {useDashboard,isPublicDemo} from '../stores/dashboard';
import {recipeLabel} from '../recipe-labels';
import {snapshotDate} from '../demo-session';
import OnboardingView from './OnboardingView.vue';
import HealthConnection from '../components/HealthConnection.vue';
import {measuredWeightChange} from '../health-types';
import {isNative,configureNativeApi} from '../api/transport';
import ProfileSettings from '../components/ProfileSettings.vue';
import FoodPhoto from '../components/FoodPhoto.vue';
import DiaryHistory from '../components/DiaryHistory.vue';
const store=useDashboard();const {t,te,locale}=useI18n();const route=useRoute();
const apiUrl=ref('');
const tokenInput=ref('');const weight=ref<number|null>(null);const saving=ref(false);const saved=ref('');
watch(()=>store.sessionVersion,()=>{weight.value=null;tokenInput.value='';saved.value='';});
function resetDemo(){saved.value='';weight.value=null;store.logout();}
const tab=computed(()=>route.name);const d=computed(()=>store.data);
const ratio=computed(()=>d.value?Math.min(100,Math.round(d.value.nutrition.consumed.calories/d.value.nutrition.goals.calories*100)):0);
const weights=computed(()=>d.value?.weight.logs??[]);
const weightPoints=computed(()=>{const list=weights.value;if(!list.length)return '';const values=list.map(x=>Number(x.kg));const min=Math.min(...values)-.5,max=Math.max(...values)+.5;return values.map((v,i)=>`${20+i*(260/Math.max(1,values.length-1))},${95-(v-min)/(max-min)*70}`).join(' ');});
const errorText=computed(()=>te(store.error)?t(store.error):store.error?t('requestFailed'): '');
async function login(){store.logout();store.token=tokenInput.value.trim();tokenInput.value='';try{if(isNative)configureNativeApi(apiUrl.value);await store.refresh();}catch(e){store.error=e instanceof Error?e.message:'requestFailed';}}
async function addMeal(slug:string){saving.value=true;saved.value='';try{await store.save('food-entries',{recipe_slug:slug,servings:1});saved.value=isPublicDemo?'demoRecipeAdded':'recipeAdded';}catch{}finally{saving.value=false;}}
async function addWeight(){if(!weight.value)return;saving.value=true;saved.value='';try{await store.save('weight-logs',{kg:weight.value});saved.value=isPublicDemo?'demoWeightAdded':'weightAdded';weight.value=null;}catch{}finally{saving.value=false;}}
</script>
<template>
<div class="dashboard-heading"><div><span class="eyebrow">{{d?.date??'KINDEE / THAILAND FIRST'}}</span><h1>{{t('hello')}}</h1><p>{{t('subtitle')}}</p></div><span class="sun" aria-hidden="true">✳</span></div>
<aside v-if="isPublicDemo" class="demo-mode" role="note"><strong>{{t('demoTitle')}}</strong><p>{{t('demoNotice')}}</p><a href="/">← {{t('about')}}</a></aside>
<p v-if="errorText" class="error" role="alert">{{errorText}}</p><p v-if="saved" class="success" role="status">{{t(saved)}}</p>
<section v-if="!d" class="widget connect"><span class="eyebrow">LARAVEL API · PRIVATE DASHBOARD</span><h2>{{t('connectTitle')}}</h2><p>{{t('connectBody')}}</p><form @submit.prevent="login"><label v-if="isNative">{{t('apiUrl')}}<input v-model="apiUrl" type="url" placeholder="http://192.168.1.10:8080" required><small>{{t('apiUrlHelp')}}</small></label><label>{{t('token')}}<input v-model="tokenInput" type="password" autocomplete="off" required minlength="64" maxlength="64"></label><button :disabled="store.loading">{{t(store.loading?'loading':'connect')}}</button></form><small>{{t('tokenHelp')}}</small><RouterLink class="inline-link" to="/recipes">{{t('openRecipes')}} →</RouterLink></section>
<OnboardingView v-if="d&&!isPublicDemo&&(!d.onboarding_completed||tab==='onboarding')"/>
<template v-else-if="d">
<div v-if="tab==='home'" class="quick-actions"><RouterLink to="/recipes">＋ {{t('recipe')}}</RouterLink><RouterLink to="/progress">＋ {{t('weightShort')}}</RouterLink><button :disabled="store.loading" @click="isPublicDemo?resetDemo():store.refresh()">↻ {{t(isPublicDemo?'resetDemo':'refresh')}}</button></div>
<div class="dashboard-grid">
<section v-if="tab==='home'" class="widget calories"><div class="widget-title"><h2>{{t('calories')}}</h2><span>{{t('entries',{value:d.entry_count})}}</span></div><div class="calorie-body"><div class="ring" role="img" :aria-label="`${ratio}%`" :style="{background:`conic-gradient(#b6d07b ${ratio}%,#e6ebdc 0)`}"><div><b>{{Math.round(d.nutrition.consumed.calories)}}</b><small>{{t('ofGoal',{value:d.nutrition.goals.calories})}}</small></div></div><div class="calorie-note"><span>{{t('remaining')}}</span><strong>{{Math.max(0,Math.round(d.nutrition.goals.calories-d.nutrition.consumed.calories))}}</strong><small>{{t('goalHint')}}</small></div></div><div class="macro-grid"><div v-for="(key,i) in (['protein_g','fat_g','carbs_g'] as const)"><span>{{t(['protein','fat','carbs'][i])}}</span><strong>{{Math.round(d.nutrition.consumed[key])}} <small>/ {{d.nutrition.goals[key]}} {{t('grams')}}</small></strong><progress :value="d.nutrition.consumed[key]" :max="Math.max(1,d.nutrition.goals[key])"/></div></div><p class="widget-note">{{d.timezone}} · {{t(isPublicDemo?'demoGoals':'profileGoals')}}</p></section>
<section v-if="tab==='home'" class="widget recommendations"><div class="widget-title"><h2>{{t('now')}}</h2><RouterLink to="/recipes">{{t('all')}} ↗</RouterLink></div><p class="widget-note">{{t(isPublicDemo?'snapshot':'fromApi')}} · {{t('estimates')}}</p><article v-for="r in d.recommendations" :key="r.slug" class="food-row"><FoodPhoto :slug="r.slug" :alt="recipeLabel(r.slug,locale,r.title)" compact/><div><h3>{{recipeLabel(r.slug,locale,r.title)}}</h3><p>{{r.minutes}} {{t('minutes')}} · {{r.nutrition.calories}} {{t('kcal')}} · {{r.nutrition.protein_g}} {{t('grams')}} {{t('protein')}}</p><small>{{t('cleanup')}} {{r.cleanup_minutes}} {{t('minutes')}} · {{r.nutrition.price_thb===null?t('priceUnknown'):'≈ ฿'+r.nutrition.price_thb}}</small></div><button :disabled="saving" :aria-label="t('add')+' '+recipeLabel(r.slug,locale,r.title)" @click="addMeal(r.slug)">＋</button></article><p v-if="!d.recommendations.length">{{t('noRecipes')}}</p></section>
<section v-if="tab==='home'||tab==='prep'" class="widget prep-widget"><div class="widget-title"><h2>{{t('prep')}}</h2><span class="chip">{{t('suggestion')}}</span></div><template v-if="d.meal_prep.recipe"><FoodPhoto :slug="d.meal_prep.recipe.slug" :alt="recipeLabel(d.meal_prep.recipe.slug,locale,d.meal_prep.recipe.title)"/><h3>{{recipeLabel(d.meal_prep.recipe.slug,locale,d.meal_prep.recipe.title)}}</h3><p>{{t('prepHint',{value:d.meal_prep.portions})}}</p><p class="widget-note">{{t('storage')}}</p><RouterLink class="inline-link" :to="{path:'/recipes',query:{recipe:d.meal_prep.recipe.slug,portions:d.meal_prep.portions}}">{{t('portionsRecipes')}} →</RouterLink></template><p v-else>{{t('noRecipes')}}</p></section>
<section v-if="tab==='home'||tab==='progress'" class="widget weight-widget"><div class="widget-title"><h2>{{t('weight')}}</h2><span>{{t('kg')}}</span></div><div v-if="weights.length"><div class="weight-value">{{weights[weights.length-1].kg}} <small>{{t('kg')}}</small></div><svg v-if="weights.length>1" viewBox="0 0 300 110" role="img" :aria-label="t('weightChart')"><polyline :points="weightPoints" fill="none" stroke="#5f8748" stroke-width="3"/></svg><p class="widget-note">{{t('weightCount',{value:weights.length})}}</p></div><p v-else class="empty-state">{{t('weightEmpty')}}</p><form @submit.prevent="addWeight"><label>{{t('weightInput')}}<input v-model.number="weight" type="number" min="20" max="400" step="0.01" required :placeholder="t('forExample')"></label><button :disabled="saving">{{t('save')}}</button></form></section>
<HealthConnection v-if="tab==='home'||tab==='settings'"/><p v-if="tab==='progress'&&measuredWeightChange(weights)!==null">{{t('measuredChange',{value:measuredWeightChange(weights)})}}</p><RouterLink v-if="tab==='settings'&&!isPublicDemo" class="inline-link" to="/onboarding">{{t('setupProfile')}}</RouterLink><DiaryHistory v-if="tab==='home'"/><DiaryHistory v-if="tab==='progress'" weight/><ProfileSettings v-if="tab==='settings'"/>
</div><p class="provenance">{{isPublicDemo?t('sourceNote',{value:snapshotDate}):t('estimates')}}</p>
</template>
<section v-if="tab==='settings'" class="widget"><h2>{{t('installation')}}</h2><p>{{t('iosInstall')}}</p><p>{{t('androidInstall')}}</p><p>{{t('desktopInstall')}}</p><p>{{t(isPublicDemo?'offlineDemo':'offlineCore')}}</p></section>
</template>
