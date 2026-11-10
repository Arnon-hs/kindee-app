import {apiResponse} from './transport';
import {zPostAuthLoginResponse,zPostAuthRegisterResponse} from './generated/zod.gen';
import type {AccountSession} from './generated/types.gen';
export type {AccountSession} from './generated/types.gen';
export interface Credentials {email:string;password:string;name?:string;password_confirmation?:string;market?:string}
function authProblem(status:number,errors:Record<string,string[]>|undefined,mode:'login'|'register'):string {
 if(status===401)return 'authInvalid';
 if(status===429)return 'authRateLimited';
 if(errors?.email&&mode==='register')return 'authEmailTaken';
 if(errors?.password)return 'authPasswordInvalid';
 return 'authFailed';
}
export async function authenticateAccount(mode:'login'|'register',input:Credentials):Promise<AccountSession>{
 const response=await apiResponse('/api/v1/auth/'+mode,{method:'POST',cache:'no-store',headers:{Accept:'application/json','Content-Type':'application/json'},body:JSON.stringify(input)});
 if(!response.ok){
  const value=await response.json().catch(()=>({})) as {errors?:Record<string,string[]>};
  throw new Error(authProblem(response.status,value?.errors,mode));
 }
 const schema=mode==='login'?zPostAuthLoginResponse:zPostAuthRegisterResponse;
 const session=schema.safeParse(await response.json());
 if(!session.success)throw new Error('authFailed');
 return session.data.data;
}
