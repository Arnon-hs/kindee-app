import assert from 'node:assert/strict';
import test from 'node:test';
import {authenticateAccount} from '../src/api/auth';

const input={email:'fixture@example.test',password:'fixture-password'};
const session={token:'0'.repeat(64),expires_at:'2026-12-11T00:00:00+00:00',user:{id:1,name:'Fixture',email_verified:false}};

async function response(body:unknown,status:number,check:()=>Promise<void>) {
 const original=globalThis.fetch;
 globalThis.fetch=async()=>new Response(JSON.stringify(body),{status,headers:{'Content-Type':'application/json'}});
 try { await check(); } finally { globalThis.fetch=original; }
}

test('both account operations accept the real server session shape including offset dates',async()=>{
 for(const mode of ['login','register'] as const)await response({data:session},mode==='login'?200:201,async()=>{
  assert.deepEqual(await authenticateAccount(mode,input),session);
 });
});

test('a successful HTTP status cannot admit a malformed session or expose its data in an error',async()=>{
 for(const data of [null,{}, {data:{...session,token:'truncated'}}, {data:{...session,expires_at:'tomorrow'}}, {data:{...session,user:null}}]) {
  await response(data,200,async()=>{
   await assert.rejects(authenticateAccount('login',input),error=>error instanceof Error&&error.message==='authFailed');
  });
 }
});

test('existing account failure priority remains stable',async()=>{
 for(const [status,mode,expected] of [[401,'register','authInvalid'],[429,'login','authRateLimited'],[422,'register','authEmailTaken'],[422,'login','authPasswordInvalid']] as const) {
  await response({errors:{email:['invalid'],password:['invalid']}},status,async()=>{
   await assert.rejects(authenticateAccount(mode,input),new Error(expected));
  });
 }
});

test('null error envelopes preserve authentication and rate-limit guidance',async()=>{
 for(const [status,expected] of [[401,'authInvalid'],[429,'authRateLimited'],[422,'authFailed']] as const) {
  await response(null,status,async()=>{
   await assert.rejects(authenticateAccount('login',input),new Error(expected));
  });
 }
});
