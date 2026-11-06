import assert from 'node:assert/strict';
import {createPinia,setActivePinia} from 'pinia';
import {useDashboard} from '../src/stores/dashboard';
import {usePlanning} from '../src/stores/planning';
import {DemoSession} from '../src/demo-session';

export async function checkSessionStores(){
 const originalFetch=globalThis.fetch;
 try{
  setActivePinia(createPinia());
  const account=useDashboard();const plans=usePlanning();
  const oldToken='a'.repeat(64),newToken='b'.repeat(64);
  account.token=oldToken;account.data=new DemoSession().dashboard();
  let resolve!:(value:Response)=>void;
  globalThis.fetch=()=>new Promise<Response>(done=>{resolve=done;});
  const saving=account.save('food-entries',{recipe_slug:'chicken-rice-bowl',servings:1}).catch(()=>{});
  account.logout();account.token=newToken;account.data=new DemoSession().dashboard();
  resolve(new Response('{}',{status:401}));await saving;
  assert.equal(account.token,newToken,'Stale save error must not log out the new account');
  assert.ok(account.data);
  globalThis.fetch=async()=>new Response('{}',{status:401});
  await plans.refresh('2026-11-06','2026-11-12');
  assert.equal(account.token,'','Current session expiry must clear credentials');
  assert.equal(account.data,null);assert.deepEqual(plans.entries,[]);assert.equal(plans.shopping,null);
  account.token=newToken;account.data=new DemoSession().dashboard();
  let sent='';
  globalThis.fetch=async(_input,init)=>{sent=new Headers(init?.headers).get('Authorization')??'';return new Response(null,{status:204});};
  await account.signOut();
  assert.equal(sent,'Bearer '+newToken);assert.equal(account.token,'');assert.equal(account.data,null);
  console.log('PASS: stale save responses preserve the new session; current expiry clears plans and account; logout revokes the captured token.');
 }finally{globalThis.fetch=originalFetch;}
}
