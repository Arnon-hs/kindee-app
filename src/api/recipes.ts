import {apiResponse} from './transport';
import {publicCatalog,scalePublicRecipe} from '../demo-session';
export interface Nutrients { calories: number; protein_g: number; fat_g: number; carbs_g: number; price_thb: number | null }
export interface Nutrition { servings: number; total: Nutrients; per_serving: Nutrients; is_price_estimate: boolean; price?: {currency:string|null;status:'estimated'|'unknown';total:number|null;per_serving:number|null} }
export interface Recipe { market?:string; slug: string; title: string; description: string; servings: number; total_minutes: number; cleanup_minutes: number; nutrition: Nutrition; ingredients?: { name: string; grams: number }[] }
export interface Filters { max_calories?: number; min_protein_g?: number; max_price_thb?: number; max_total_minutes?: number; max_cleanup_minutes?: number }
async function json<T>(url: string, init?: RequestInit): Promise<T> {
  if(import.meta.env.VITE_PUBLIC_DEMO==='true') {
    const encodedSlug=url.split('?')[0]?.split('/')[4];
    if(init?.method==='POST') {
      if(!encodedSlug)throw new Error('recipeNotFound');
      const slug=decodeURIComponent(encodedSlug);
      return {data:scalePublicRecipe(slug,JSON.parse(String(init.body)).servings)} as T;
    }
    if(encodedSlug){const recipe=publicCatalog.find(r=>r.slug===decodeURIComponent(encodedSlug));if(!recipe)throw new Error('recipeNotFound');return {data:recipe} as T;}
    const market=new URL(url,'https://kindee.invalid').searchParams.get('market')??'TH';return {data:publicCatalog.filter(r=>(r as Recipe&{market?:string}).market===market)} as T;
  }
  const response = await apiResponse(url, init);
  if (!response.ok) throw new Error(`API request failed: ${response.status}`);
  return response.json() as Promise<T>;
}
export async function recommend(filters: Filters,market='TH'): Promise<Recipe[]> {
  const recipes = (await json<{data: Recipe[]}>(`/api/v1/recipes?market=${encodeURIComponent(market)}`)).data;
  return recipes.filter(r => {
    const n=r.nutrition.per_serving;
    return (filters.max_calories===undefined||n.calories<=filters.max_calories)
      && (filters.min_protein_g===undefined||n.protein_g>=filters.min_protein_g)
      && (filters.max_price_thb===undefined||(n.price_thb!==null&&n.price_thb<=filters.max_price_thb))
      && (filters.max_total_minutes===undefined||r.total_minutes<=filters.max_total_minutes)
      && (filters.max_cleanup_minutes===undefined||r.cleanup_minutes<=filters.max_cleanup_minutes);
  }).sort((a,b)=>a.cleanup_minutes-b.cleanup_minutes||a.total_minutes-b.total_minutes);
}
export async function detail(slug: string): Promise<Recipe> {
  try {return (await json<{data:Recipe}>(`/api/v1/recipes/${encodeURIComponent(slug)}`)).data;}
  catch(error) {const recipe=(await json<{data:Recipe[]}>('/api/v1/recipes?market=TH')).data.find(r=>r.slug===slug);if(!recipe)throw error;return recipe;}

}
export async function calculate(slug: string, servings: number): Promise<Nutrition> {
  return (await json<{ data: Nutrition }>(`/api/v1/recipes/${encodeURIComponent(slug)}/calculate`, {
    method: 'POST', headers: { 'Content-Type': 'application/json', Accept: 'application/json' }, body: JSON.stringify({ servings })
  })).data;
}
