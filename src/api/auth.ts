import {apiResponse} from './transport';
export interface Credentials {email:string;password:string;name?:string;password_confirmation?:string;market?:string}
export interface AccountSession {token:string;expires_at:string;user:{id:number;name:string;email_verified:boolean}}
export async function authenticateAccount(mode:'login'|'register',input:Credentials):Promise<AccountSession>{
 const response=await apiResponse('/api/v1/auth/'+mode,{method:'POST',cache:'no-store',headers:{Accept:'application/json','Content-Type':'application/json'},body:JSON.stringify(input)});
 if(!response.ok){
  const value=await response.json().catch(()=>({})) as {errors?:Record<string,string[]>};
  throw new Error(response.status===401?'authInvalid':response.status===429?'authRateLimited':value.errors?.email&&mode==='register'?'authEmailTaken':value.errors?.password?'authPasswordInvalid':'authFailed');
 }
 return (await response.json()).data as AccountSession;
}
