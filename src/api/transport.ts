import {Capacitor,CapacitorHttp} from '@capacitor/core';
export const isNative=Capacitor.isNativePlatform();
let endpoint='';
export function configureNativeApi(value:string){
 const url=new URL(value);
 const privateHost=/^(localhost|127\.0\.0\.1|10\.\d+\.\d+\.\d+|192\.168\.\d+\.\d+|172\.(1[6-9]|2\d|3[01])\.\d+\.\d+)$/.test(url.hostname);
 if(url.username||url.password||url.search||url.hash||url.pathname!=='/'||!(url.protocol==='https:'||(import.meta.env.VITE_LOCAL_API==='true'&&url.protocol==='http:'&&privateHost)))throw new Error('invalidApiUrl');
 endpoint=url.origin;
}
export async function apiResponse(path:string,init:RequestInit={}) {
 if(!isNative)return fetch(path,init);
 if(!endpoint)throw new Error('apiUrlRequired');
 const headers:Record<string,string>={};new Headers(init.headers).forEach((value,key)=>{headers[key]=value;});
 const response=await CapacitorHttp.request({url:endpoint+path,method:init.method??'GET',headers,data:init.body?JSON.parse(String(init.body)):undefined,responseType:'json'});
 return {ok:response.status>=200&&response.status<300,status:response.status,json:async()=>response.data};
}
