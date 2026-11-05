import {apiResponse} from './transport';
import type {HealthSummary} from '../health-types';
import type {Nutrients} from './recipes';
export interface FoodEntry {id:number;title:string;recipe_slug:string;servings:number|null;nutrition:Nutrients;eaten_at:string}
export interface DashboardMeal {slug:string;title:string;minutes:number;cleanup_minutes:number;nutrition:Nutrients}
export interface Dashboard {onboarding_completed?:boolean;health?:HealthSummary;date:string;timezone:string;market:{country_code:string;currency:string};nutrition:{consumed:Omit<Nutrients,'price_thb'>;goals:Omit<Nutrients,'price_thb'>};recommendations:DashboardMeal[];meal_prep:{status:string;recipe:DashboardMeal|null;portions:number;saved_plan:boolean};weight:{unit:'kg';logs:{id:number;kg:string;recorded_at:string}[]};food_entries:FoodEntry[];entry_count:number;provenance:string;ranking_version:string}
export async function personalRequest<T>(path:string,token:string,body?:object,method?:'GET'|'POST'|'PATCH'|'DELETE'|'PUT'):Promise<T> {
 const response=await apiResponse('/api/v1/'+path,{method:method??(body?'POST':'GET'),cache:'no-store',headers:{Authorization:'Bearer '+token,Accept:'application/json',...(body?{'Content-Type':'application/json'}:{})},body:body?JSON.stringify(body):undefined});
 if(!response.ok)throw new Error(response.status===401?'invalidToken':`API ${response.status}`);
 if(response.status===204)return undefined as T;
 return (await response.json()).data as T;
}
