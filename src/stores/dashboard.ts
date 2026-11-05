import {NativeHealth} from '../native-health';
import {isNative} from '../api/transport';
import {defineStore} from 'pinia';
import {ref} from 'vue';
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
 async function save(path:'food-entries'|'weight-logs'|'profile',body:object){error.value='';try{if(session){const input=body as {recipe_slug?:string;servings?:number;kg?:number};if(path==='profile')session.setProfile(body as ProfileInput);else if(path==='food-entries')session.addFood(String(input.recipe_slug),Number(input.servings));else session.addWeight(Number(input.kg));}else await personalRequest(path,token.value,body,path==='profile'?'PATCH':'POST');await refresh();}catch(e){recordError(e);throw e;}}
 async function remove(type:'food-entries'|'weight-logs',id:number){error.value='';try{if(session){if(type==='food-entries')session.removeFood(id);else session.removeWeight(id);}else await personalRequest(`${type}/${id}`,token.value,undefined,'DELETE');await refresh();}catch(e){recordError(e);throw e;}}
 return {sessionVersion,token,data,error,loading,refresh,logout,save,remove};
});
