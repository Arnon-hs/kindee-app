import {defineStore} from 'pinia';
import {ref,watch} from 'vue';
import {useDashboard,isPublicDemo} from './dashboard';
import {personalRequest} from '../api/dashboard';
import {type PlanEntry,type PlanInput,type ShoppingList,validRange} from '../planning';
export const usePlanning=defineStore('planning',()=>{
 const account=useDashboard();const entries=ref<PlanEntry[]>([]);const shopping=ref<ShoppingList|null>(null);const busy=ref(false);const error=ref('');let generation=0;
 const current=(version:number,token:string,session:number)=>version===generation&&account.token===token&&account.sessionVersion===session;
 async function refresh(from:string,to:string){validRange(from,to);const version=++generation,token=account.token,session=account.sessionVersion;busy.value=true;error.value='';try{const [plans,list]=isPublicDemo?[{entries:account.demoPlans!.list(from,to)},account.demoPlans!.shopping(from,to)]:await Promise.all([personalRequest<{entries:PlanEntry[]}>(`meal-plans?from=${from}&to=${to}`,token),personalRequest<ShoppingList>(`shopping-list?from=${from}&to=${to}`,token)]);if(current(version,token,session)){entries.value=plans.entries;shopping.value=list;}}catch(e){if(current(version,token,session)){entries.value=[];shopping.value=null;if(e instanceof Error&&e.message==='invalidToken'){account.logout();account.error='invalidToken';}else error.value='planningFailed';}}finally{if(current(version,token,session))busy.value=false;}}
 async function mutate(operation:()=>unknown|Promise<unknown>){const token=account.token,session=account.sessionVersion;try{await operation();if(token===account.token&&session===account.sessionVersion)await account.refresh();}catch(e){if(token===account.token&&session===account.sessionVersion&&e instanceof Error&&e.message==='invalidToken'){account.logout();account.error='invalidToken';}throw e;}}
 async function add(input:PlanInput){await mutate(()=>isPublicDemo?account.demoPlans!.add(input,account.data!.market.country_code):personalRequest('meal-plans',account.token,input));}
 async function remove(id:number){await mutate(()=>isPublicDemo?account.demoPlans!.remove(id):personalRequest(`meal-plans/${id}`,account.token,undefined,'DELETE'));}
 function clear(){generation++;entries.value=[];shopping.value=null;error.value='';busy.value=false;}
 watch(()=>account.sessionVersion,clear,{flush:'sync'});
 return {entries,shopping,busy,error,refresh,add,remove,clear};
});
