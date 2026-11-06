import {authenticateAccount,type Credentials} from '../api/auth';
import {NativeHealth} from '../native-health';
import {isNative} from '../api/transport';
import {defineStore} from 'pinia';
import {ref,markRaw} from 'vue';
import type {ProfileInput} from '../profile';
import {DemoSession} from '../demo-session';
export const isPublicDemo=import.meta.env.VITE_PUBLIC_DEMO==='true';
import {personalRequest,type Dashboard} from '../api/dashboard';
export const useDashboard=defineStore('dashboard',()=>{
 const session=isPublicDemo?new DemoSession():null;
 const sessionVersion=ref(0);
 const token=ref('');const data=ref<Dashboard|null>(session?.dashboard()??null);const error=ref('');const loading=ref(false);
 let generation=0;
 function recordError(e:unknown){const message=e instanceof Error?e.message:String(e);if(message==='invalidToken')logout();error.value=message;}
 async function refresh(){if(session){data.value=session.dashboard();return;}if(!token.value)return;const current=++generation;loading.value=true;error.value='';try{const value=await personalRequest<Dashboard>('dashboard',token.value);if(current===generation)data.value=value;}catch(e){if(current===generation)recordError(e);}finally{if(current===generation)loading.value=false;}}
 function logout(){if(isNative)void NativeHealth.clearSession().catch(()=>{});sessionVersion.value++;generation++;token.value='';session?.clear();data.value=session?.dashboard()??null;error.value='';loading.value=false;}
 async function save(path:'food-entries'|'weight-logs'|'profile',body:object){const version=sessionVersion.value,currentToken=token.value;error.value='';try{if(session){const input=body as {recipe_slug?:string;servings?:number;kg?:number};if(path==='profile')session.setProfile(body as ProfileInput);else if(path==='food-entries')session.addFood(String(input.recipe_slug),Number(input.servings));else session.addWeight(Number(input.kg));}else await personalRequest(path,token.value,body,path==='profile'?'PATCH':'POST');if(version===sessionVersion.value&&currentToken===token.value)await refresh();}catch(e){if(version===sessionVersion.value&&currentToken===token.value)recordError(e);throw e;}}
 async function remove(type:'food-entries'|'weight-logs',id:number){const version=sessionVersion.value,currentToken=token.value;error.value='';try{if(session){if(type==='food-entries')session.removeFood(id);else session.removeWeight(id);}else await personalRequest(`${type}/${id}`,token.value,undefined,'DELETE');if(version===sessionVersion.value&&currentToken===token.value)await refresh();}catch(e){if(version===sessionVersion.value&&currentToken===token.value)recordError(e);throw e;}}
 async function authenticate(mode:'login'|'register',input:Credentials){const version=++generation;loading.value=true;error.value='';try{const value=await authenticateAccount(mode,input);if(version!==generation)return;sessionVersion.value++;token.value=value.token;await refresh();}catch(e){if(version===generation)recordError(e);throw e;}finally{if(version===generation)loading.value=false;}}
 async function signOut(){const previous=token.value;logout();if(previous&&!isPublicDemo)try{await personalRequest('auth/logout',previous,{},'POST');}catch{}}
 return {sessionVersion,token,data,error,loading,refresh,logout,signOut,authenticate,save,remove,demoPlans:session?markRaw(session.planner):null};
});
